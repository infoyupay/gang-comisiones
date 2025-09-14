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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.services.TransactionManager;
import com.yupay.gangcomisiones.services.TransactionService;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * The default implementation of Transaction Service, backed up
 * by JPA and JDBC for PostgreSQL.
 *
 * @param emf          the entity manager factory object.
 * @param jdbcExecutor the executor service object. It's recommended to be a single thread
 *                     to avoid race conditions with the Jdbc Connection.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record TransactionServiceImpl(EntityManagerFactory emf,
                                     ExecutorService jdbcExecutor) implements
        TransactionService, TransactionManager {
    @Override
    public CompletableFuture<Optional<Transaction>> findTransactionById(long id) {
        return runWithoutTransactionAsync(em -> Optional.ofNullable(em.find(Transaction.class, id)));
    }

    @Override
    public CompletableFuture<List<Transaction>> searchTransactionsBy(SearchCriteria criteria) {
        return runWithoutTransactionAsync(em -> {
            var cb = em.getCriteriaBuilder();
            var qry = cb.createQuery(Transaction.class);
            var root = qry.from(Transaction.class);
            qry.select(root).where(criteria.toCriteriaPredicate(root, cb));
            return em.createQuery(qry).getResultList();
        });
    }

    @Override
    public CompletableFuture<Transaction> createTransaction(@NotNull CreateTransactionRequest request) {
        Objects.requireNonNull(request, "request is null in create transaction service");
        return runInTransactionAsync(em -> {
            //Validate:
            assertValidateCreateRequest(request);

            //2. Validate against real-time data, that user have enough privileges. If so, retrieved user is used.
            //The check privileges will fail if priveleges didn't meet requirements or user was no more active.
            var freshUser = AppContext.getInstance().getUserService()
                    .checkPrivilegesOrException(
                            em,
                            request.getCashierId(),
                            UserRole.CASHIER);
            var r = Transaction.builder()
                    .bank(em.getReference(Bank.class, request.getBankId()))
                    .concept(em.getReference(Concept.class, request.getConceptId()))
                    .cashier(freshUser)
                    .amount(request.getAmount())
                    .commission(request.getConceptType()
                            .computeCommission(request.getAmount(), request.getConceptCommissionValue()))
                    .status(TransactionStatus.REGISTERED)
                    .build();
            em.persist(r);
            AuditAction.TRANSACTION_CREATE.log(em, r.getId());
            return r;
        });
    }

    /**
     * Validates the properties of the {@link CreateTransactionRequest} to ensure they meet the
     * required constraints for creating a new transaction. Throws {@link PersistenceServicesException}
     * if any validation condition fails.
     * <br/>
     * Validation steps include:
     * <ul>
     *   <li>Ensuring the bank ID, cashier ID, and concept ID are valid (greater than 0).</li>
     *   <li>Verifying that the monetary values, such as amount and concept commission value,
     *       are non-null and greater than 0.</li>
     * </ul>
     *
     * @param request the {@link CreateTransactionRequest} object containing transaction details
     *                including IDs and monetary values.
     *                <ol>
     *                   <li><strong>Bank ID</strong>: Must be a positive number (&gt; 0).</li>
     *                   <li><strong>Cashier ID</strong>: Must be a positive number (&gt; 0).</li>
     *                   <li><strong>Concept ID</strong>: Must be a positive number (&gt; 0).</li>
     *                   <li><strong>Amount</strong>: Must be non-null and greater than 0.</li>
     *                   <li><strong>Concept Commission Value</strong>: Must be non-null and greater than 0.</li>
     *                </ol>
     * @throws PersistenceServicesException if:
     *         <ul>
     *           <li>Any of the IDs (bank ID, cashier ID, concept ID) are invalid (less than or equal to 0).</li>
     *           <li>Any of the monetary values (amount or concept commission value) are null or less than or equal to 0.</li>
     *         </ul>
     */
    private void assertValidateCreateRequest(@NotNull CreateTransactionRequest request) {
        //1. Ids
        if (LongStream.of(request.getBankId(), request.getCashierId(), request.getConceptId())
                .anyMatch(id -> id <= 0)) {
            throw new PersistenceServicesException("Request must contain valid bank, cashier and concept ids.");
        }
        //2. Amount not null:
        if (Stream.of(request.getAmount(), request.getConceptCommissionValue())
                .anyMatch(Objects::isNull)) {
            throw new PersistenceServicesException("Request amounts cannot be null.");
        }
        //3. Commision value check:
        if(!request.getConceptType().validateCommissionValue(request.getConceptCommissionValue())){
            throw new PersistenceServicesException("Request concept commission value must be greater than 0 and reasonable.");
        }
        //4. Amount check:
        if (request.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new PersistenceServicesException("Request amount must be greater than 0.");
        }
    }

}
