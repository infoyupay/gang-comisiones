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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.model.ReversalRequest;
import com.yupay.gangcomisiones.model.ReversalRequestStatus;
import com.yupay.gangcomisiones.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing reversal requests.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ReversalRequestService {
    /**
     * Creates a reversal request for a specified transaction. This method is used
     * to initiate a request to reverse a transaction, providing a justification message
     * and associating the request to the initiating user.
     *
     * @param transactionId the unique identifier of the transaction to be reversed
     * @param userId        the unique identifier of the user making the reversal request
     * @param message       the justification message for the reversal request
     * @return a CompletableFuture that will complete with the created ReversalRequest object
     * when the operation is successful
     */
    CompletableFuture<ReversalRequest> createReversalRequest(long transactionId,
                                                             long userId,
                                                             String message);

    /**
     * Retrieves a reversal request based on its unique identifier.
     * This method returns a CompletableFuture that will complete with an Optional
     * containing the ReversalRequest if found, or an empty Optional otherwise.
     *
     * @param requestId the unique identifier of the reversal request to be retrieved
     * @return a CompletableFuture containing an Optional with the ReversalRequest if found,
     * or an empty Optional if no request matches the given identifier
     */
    CompletableFuture<Optional<ReversalRequest>> findRequestById(long requestId);

    /**
     * Retrieves a reversal request associated with a specified transaction.
     * This method searches for a reversal request using the unique identifier
     * of the associated transaction and returns a CompletableFuture that resolves
     * to an Optional containing the matching ReversalRequest, or an empty Optional
     * if no match is found.
     *
     * @param transactionId the unique identifier of the transaction associated with the reversal request
     * @return a CompletableFuture containing an Optional with the ReversalRequest if found,
     * or an empty Optional if no request matches the given transaction identifier
     */
    CompletableFuture<Optional<ReversalRequest>> findRequestByTransaction(long transactionId);

    /**
     * Retrieves a list of reversal requests based on the specified search criteria.
     * This method returns a CompletableFuture that completes with a list of
     * ReversalRequest objects matching the given criteria.
     *
     * @param criteria the search criteria used to filter the reversal requests
     * @return a CompletableFuture containing a list of matching ReversalRequest objects
     */
    CompletableFuture<List<ReversalRequest>> listRequestsBy(SearchCriteria criteria);

    /**
     * Resolves a reversal request by providing a resolution and an explanation.
     * This method updates the status of the request based on the given resolution
     * and logs the provided answer for audit purposes.
     *
     * @param requestId  the unique identifier of the reversal request to be resolved
     * @param userId     the unique identifier of the user performing the resolution
     * @param answer     the explanation or justification for the resolution
     * @param resolution the resolution decision, either APPROVED or DENIED
     * @return a CompletableFuture that completes when the resolution operation is finalized
     */
    CompletableFuture<Void> resolveRequest(long requestId,
                                           long userId,
                                           String answer,
                                           Resolution resolution);

    /// Represents the resolution of a reversal request in the system.
    /// This enum is used to determine whether a reversal request has been approved or denied.
    /// The possible values are:
    /// - DENIED: Indicates that the reversal request was not approved.
    /// - APPROVED: Indicates that the reversal request was approved.
    enum Resolution {
        /**
         * Indicates that a reversal request has been denied.
         * This constant is used within the {@code Resolution} enum to represent
         * the rejection of a reversal request in the system.
         */
        DENIED,
        /**
         * Indicates that a reversal request has been approved.
         * This constant is used within the {@code Resolution} enum to represent
         * the acceptance of a reversal request in the system.
         */
        APPROVED
    }

    /// Represents a set of criteria for searching and filtering [ReversalRequest] entities.
    /// This record encapsulates optional filters that can be combined to refine queries:
    /// - Requestor (requestedBy) and/or evaluator (evaluatedBy)
    /// - Request dates range (requestedFrom/requestedUntil)
    /// - Answer dates range (answeredFrom/answeredUntil)
    /// - Request status (status)
    /// Fields are optional; if a field is null, the filter is not applied. If you want to filter
    /// for those requests without answer, send a mock User with null id in evaluatedBy.
    ///
    /// @param requestedBy    the user who realized the request. If null, it's ignoed.
    /// @param requestedFrom  date (inclusive) from when created requests are searched.
    ///                                            If null, no limit is applied.
    /// @param requestedUntil date (inclusive) until when created requests are searched.
    ///                                            If null, nu upper limit is applied.
    /// @param evaluatedBy    user who evaluated the request. If null, it doesn't filter by evaluator,
    ///                                             if a mock user with null id is passed, it shall be interpreted as "not evaluated"
    ///                                             (evaluatedBy IS NULL).
    /// @param answeredFrom   date (inclusive) from when registered answers are searched.
    ///                                             If null, no lower limit is applied.
    /// @param answeredUntil  date (inclusive) until when registered answers are searched.
    ///                                             If null, no upper limit is applied.
    /// @param status         request status (vg, PENDING, APPROVED, DENIED). If null, filter is not applied.
    @Builder
    record SearchCriteria(User requestedBy,
                          LocalDate requestedFrom,
                          LocalDate requestedUntil,
                          User evaluatedBy,
                          LocalDate answeredFrom,
                          LocalDate answeredUntil,
                          ReversalRequestStatus status) {
        /**
         * Converts the current search criteria into a JPA {@link Predicate}
         * to be used with the {@link CriteriaBuilder}.
         * This predicate is applied to the provided {@link Root} of the {@code ReversalRequest}
         * entity for query filtering.
         * The filter conditions are based on the non-null fields of the class
         * (e.g., {@code requestedBy}, {@code requestedFrom},
         * {@code requestedUntil}, {@code evaluatedBy}, {@code answeredFrom}, {@code answeredUntil}, and {@code status}).
         *
         * @param root the {@link Root} of the {@code ReversalRequest} entity, representing the base entity to apply the criteria predicates.
         * @param cb   the {@link CriteriaBuilder} used to construct query predicates in a type-safe manner.
         * @return a {@link Predicate} representing the query criteria, or {@code null} if no criteria are provided.
         */
        public @Nullable Predicate toCriteriaPredicate(Root<ReversalRequest> root, CriteriaBuilder cb) {
            var ls = new ArrayList<Predicate>();
            if (requestedBy != null) {
                ls.add(cb.equal(root.get("requestedBy"), requestedBy));
            }
            if (requestedFrom != null) {
                ls.add(cb.greaterThanOrEqualTo(root.get("request_stamp"), requestedFrom
                        .atStartOfDay()
                        .atOffset(ZoneOffset.of("-5"))));
            }
            if (requestedUntil != null) {
                ls.add(cb.lessThanOrEqualTo(root.get("request_stamp"), requestedUntil
                        .atTime(LocalTime.MAX)
                        .atOffset(ZoneOffset.of("-5"))));
            }
            if (evaluatedBy != null) {
                if (evaluatedBy.getId() == null) {
                    ls.add(cb.isNull(root.get("evaluatedBy")));
                } else {
                    ls.add(cb.equal(root.get("evaluatedBy"), evaluatedBy));
                }
            }
            if (answeredFrom != null) {
                ls.add(cb.greaterThanOrEqualTo(root.get("answer_stamp"), answeredFrom
                        .atStartOfDay()
                        .atOffset(ZoneOffset.of("-5"))));
            }
            if (answeredUntil != null) {
                ls.add(cb.lessThanOrEqualTo(root.get("answer_stamp"), answeredUntil
                        .atTime(LocalTime.MAX)
                        .atOffset(ZoneOffset.of("-5"))));
            }
            if (status != null) {
                ls.add(cb.equal(root.get("status"), status));
            }
            return ls.isEmpty() ? null : cb.and(ls.toArray(Predicate[]::new));
        }
    }
}
