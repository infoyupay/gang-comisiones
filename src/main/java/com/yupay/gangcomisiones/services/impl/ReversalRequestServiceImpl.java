/*
 * gang-comisiones
 * COPYLEFT 2025
 * Ingenieria Informatica Yupay SACS
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 *  with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.yupay.gangcomisiones.services.impl;

import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.services.ReversalRequestService;
import com.yupay.gangcomisiones.services.TransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of the ReversalRequestService interface.
 *
 * @param emf          the shared EntityManagerFactory object.
 * @param jdbcExecutor the shared single thread ExecutorService object.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record ReversalRequestServiceImpl(
        EntityManagerFactory emf,
        ExecutorService jdbcExecutor)
        implements ReversalRequestService, TransactionManager {
    /// {@inheritDoc}
    ///
    /// Execution plan:
    /// 1. Validate the transaction exists and the user is the owner.
    /// 2. Creates a reversal request with given message and persists.
    /// 3. Modify the transaction status to REVERSION_REQUESTED.
    /// 4. Creates and persist the AuditAction
    /// 5. Commits transaction, or rollback if an error occurs.
    @Override
    public CompletableFuture<ReversalRequest> createReversalRequest(long transactionId,
                                                                    long userId,
                                                                    String message) {
        return runInTransactionAsync(em -> {
            //Get transaction to revert from db
            var transaction = em.find(Transaction.class, transactionId);
            if (transaction == null) {
                throw Errors.TRANSACTION_NOT_FOUND.exception(transactionId);
            }
            //Check that the requesting user is the transaction owner (creator).
            if (transaction.getCashier().getId() != userId) {
                throw Errors.TRANSACTION_NOT_OWNED.exception(userId);
            }
            //Get user from db
            var user = em.find(User.class, userId);
            if (user == null) {
                throw Errors.USER_NOT_FOUND.exception(userId);
            }
            //Build request.
            var request = ReversalRequest
                    .builder()
                    .transaction(transaction)
                    .message(message)
                    .requestedBy(user)
                    .status(ReversalRequestStatus.PENDING)
                    .build();
            //persist request
            em.persist(request);
            //Change transaction status
            transaction.setStatus(TransactionStatus.REVERSION_REQUESTED);
            //Log auditory.
            AuditAction.REVERSAL_REQUEST_CREATE.log(em, request.getId());
            return request;
        });
    }

    @Override
    public CompletableFuture<Optional<ReversalRequest>> findRequestById(long requestId) {
        return runWithoutTransactionAsync(em -> Optional
                .ofNullable(em.find(ReversalRequest.class, requestId)));
    }

    @Override
    public CompletableFuture<Optional<ReversalRequest>> findRequestByTransaction(long transactionId) {
        return runWithoutTransactionAsync(em ->
                em.createQuery("SELECT r FROM ReversalRequest r WHERE r.transaction = :transaction",
                                ReversalRequest.class)
                        .setParameter("transaction",
                                em.find(Transaction.class, transactionId))
                        .getResultList()
                        .stream()
                        .findFirst());
    }

    @Override
    public CompletableFuture<List<ReversalRequest>> listRequestsBy(SearchCriteria criteria) {
        return runWithoutTransactionAsync(em -> {
            var cb = em.getCriteriaBuilder();
            var cq = cb.createQuery(ReversalRequest.class);
            var root = cq.from(ReversalRequest.class);
            cq.where(criteria.toCriteriaPredicate(root, cb));
            return em.createQuery(cq).getResultList();
        });
    }

    @Override
    public CompletableFuture<Void> resolveRequest(long requestId,
                                                  long userId,
                                                  String answer,
                                                  Resolution resolution) {
        return runVoidInTransactionAsync(em -> {
            //Check user exists and has enough privileges.
            var user = em.find(User.class, userId);
            if (user == null) {
                throw Errors.USER_NOT_FOUND.exception(userId);
            } else if (!user.getActive() || !user.getRole().isAtLeast(UserRole.ADMIN)) {
                throw Errors.USER_UNPRIVILEGED.exception(user.getRole());
            }
            //Modify request
            var i = em.createQuery("""
                            UPDATE ReversalRequest r
                            SET r.answer = :answer,
                            r.answerStamp = CURRENT_TIMESTAMP,
                            r.evaluatedBy = :evaluator,
                            r.status = :status
                            WHERE r.id = :requestId""")
                    .setParameter("answer", answer)
                    .setParameter("evaluator", user)
                    .setParameter("status", switch (resolution) {
                        case APPROVED -> ReversalRequestStatus.APPROVED;
                        case DENIED -> ReversalRequestStatus.REJECTED;
                        case null -> throw Errors.RESOLUTION_NOT_DEFINED.exception(null);
                    })
                    .setParameter("requestId", requestId)
                    .executeUpdate();
            if (i != 1) {
                throw Errors.UPDATED_MANY.exception(i);
            }
            //Retrieve updated request
            var request = em.find(ReversalRequest.class, requestId);
            //Set new transaction status.
            switch (resolution) {
                case DENIED -> request.getTransaction().setStatus(TransactionStatus.REGISTERED);
                case APPROVED -> request.getTransaction().setStatus(TransactionStatus.REVERSED);
            }
            //Audit log
            AuditAction.REVERSAL_REQUEST_RESOLVE.log(em, request.getId());
        });
    }

    /**
     * This enum represents various types of errors that can occur within the ReversalRequest scope.
     * Each error is associated with a specific description and a faulty field to provide
     * context on where and why the error occurred.
     *
     * @author InfoYupay SACS
     * @version 1.0
     */
    private enum Errors {
        /**
         * Error representing the scenario where a reversal request could not be found.
         * This is typically associated with the "ReversalRequest" entity and the field "ReversalRequest.id".
         * It is used to indicate that an operation involving a reversal request failed
         * because the specified request does not exist or could not be located.
         */
        REQUEST_NOT_FOUND("Reversal request not found", "ReversalRequest.id"),
        /**
         * Error representing the scenario where a transaction could not be found.
         * This is typically associated with the "Transaction" entity and the field "Transaction.id".
         * It is used to indicate that an operation involving a transaction failed
         * because the specified transaction does not exist or could not be located.
         */
        TRANSACTION_NOT_FOUND("Transaction not found", "Transaction.id"),
        /**
         * Error representing the scenario where a user could not be found.
         * This is typically associated with the "User" entity and the field "User.id".
         * It is used to indicate that an operation involving a user failed
         * because the specified user does not exist or could not be located.
         */
        USER_NOT_FOUND("User not found", "User.id"),
        /**
         * Error representing the scenario where a user does not have the required privileges
         * to perform a specific action. This is typically associated with the "User" entity
         * and the field "User.role". It is used to indicate that an operation failed
         * due to insufficient user permissions.
         */
        USER_UNPRIVILEGED("User have no privileges to perform action.", "User.role"),
        /**
         * Error representing the scenario where a transaction is not owned by the requesting user.
         * This is typically associated with the "Transaction" entity and the field "Transaction.cashier".
         * It indicates that an operation involving a transaction failed due to ownership restrictions.
         */
        TRANSACTION_NOT_OWNED("Transaction not owned by requesting user", "Transaction.cashier"),
        /**
         * Error representing the scenario where the resolution value for a reversal request
         * is not defined. This error is typically associated with the "ReversalRequest" entity
         * and the field "ReversalRequest.status". It is used to indicate that an operation
         * involving a reversal request failed because the resolution status was not provided.
         */
        RESOLUTION_NOT_DEFINED("Request resolution value cannot be null", "ReversalRequest.status"),
        /**
         * Represents an error condition where the count of modified entities does not equal one.
         * This error typically occurs when an operation that expects a single update affects multiple
         * or no entities, indicating an inconsistency or unintended result.
         * <br/>
         * The associated `faultyField` for this error is "updated", allowing for identification
         * or debugging of the specific context where the error originated.
         */
        UPDATED_MANY("Modified entities count is different from 1", "updated");

        private final String description;
        private final String faultyField;

        /**
         * Constructs an Errors instance with a specified description and the name of the faulty field.
         *
         * @param description the description of the error
         * @param faultyField the name of the field that caused the error
         */
        @Contract(pure = true)
        Errors(String description, String faultyField) {
            this.description = description;
            this.faultyField = faultyField;
        }

        /**
         * Creates a {@link PersistenceServicesException} with a detailed error message that includes
         * the description, the faulty field, and the provided faulty value.
         *
         * @param faultyValue the value of the field that caused the error
         * @return a new instance of {@link PersistenceServicesException} with a formatted error message
         * containing the description, the faulty field, and the faulty value
         */
        @Contract("_ -> new")
        public @NotNull PersistenceServicesException exception(Object faultyValue) {
            return new PersistenceServicesException("%s because %s = %s".formatted(description, faultyField, faultyValue));
        }
    }
}
