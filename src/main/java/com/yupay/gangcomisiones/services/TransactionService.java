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

import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for managing transactions within a financial system.
 * Provides functionality to create, retrieve, and search for transactions asynchronously.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface TransactionService {
    /**
     * Asynchronously retrieves a transaction by its unique identifier.
     *
     * @param id the unique identifier of the transaction to retrieve
     * @return a CompletableFuture containing an Optional with the Transaction if found,
     * or an empty Optional if no transaction exists with the provided identifier
     */
    CompletableFuture<Optional<Transaction>> findTransactionById(long id);

    /**
     * Searches for a list of transactions based on specified search criteria.
     * This method performs an asynchronous operation to retrieve the transactions
     * that match the provided criteria.
     *
     * @param criteria the search criteria used to filter transactions, containing fields
     *                 such as bank, user, concept, date range, and status
     * @return a CompletableFuture containing a list of transactions that match the search criteria;
     * if no transactions are found, an empty list is returned
     */
    CompletableFuture<List<Transaction>> searchTransactionsBy(SearchCriteria criteria);

    /**
     * Asynchronously creates and persists a new transaction in the system.
     *
     * @param request the request object containing the details of the transaction to be created
     * @return a CompletableFuture representing the asynchronous execution of the operation;
     * it completes when the transaction is successfully created or an exception is thrown
     */
    CompletableFuture<Transaction> createTransaction(@NotNull CreateTransactionRequest request);

    /**
     * Represents the search criteria used to filter transactions in the system.
     * This class provides various fields that allow for querying transactions based on
     * bank, user, concept, date range, and transaction status.
     * It serves as a data structure for passing filtering options into search-related methods.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SearchCriteria {
        /**
         * Represents a specific bank associated with the search criteria.
         * This field references the {@link Bank} entity.
         * The {@code bank} object provides context related to the financial institution
         * involved in the search criteria.
         */
        private Bank bank;
        /**
         * Represents a specific concept associated with the search criteria.
         * This field references the {@link Concept} entity.
         * The {@code concept} object provides context related to the transaction type
         * involved in the search criteria.
         */
        private Concept concept;
        /**
         * Represents a specific user associated with the search criteria.
         * This field references the {@link User} entity.
         * The {@code user} object provides context related to the individual involved
         * in the search or associated with the specified criteria.
         */
        private User user;
        /**
         * Represents the inclusive starting date for a date range used in search criteria.
         * This field defines the lower boundary for filtering records based on their date.
         */
        private LocalDate dateFrom;
        /**
         * Represents the inclusive ending date for a date range used in search criteria.
         * This field defines the upper boundary for filtering records based on their date.
         */
        private LocalDate dateUntil;
        /**
         * Represents the status of a transaction associated with the search criteria.
         * This field uses {@link TransactionStatus} to indicate the current state
         * of the transaction within the system (e.g., registered, reversion requested, or reversed).
         * It provides context to filter or identify transactions based on their lifecycle state.
         */
        private TransactionStatus status;

        /**
         * Converts the search criteria defined in the class to a {@link Predicate}
         * used for querying the database via Criteria API.
         *
         * @param root the root type in the Criteria query representing the {@link Transaction}
         *             entity.
         * @param cb   the {@link CriteriaBuilder} used for creating predicates and
         *             building query expressions.
         * @return a {@link Predicate} combining the conditions based on the
         * search criteria fields. Returns null if no criteria are specified.
         */
        public Predicate toCriteriaPredicate(Root<Transaction> root, CriteriaBuilder cb) {
            var ls = new ArrayList<Predicate>();
            if (bank != null) {
                ls.add(cb.equal(root.get("bank"), bank));
            }
            if (concept != null) {
                ls.add(cb.equal(root.get("concept"), concept));
            }
            if (user != null) {
                ls.add(cb.equal(root.get("cashier"), user));
            }
            if (dateFrom != null) {
                ls.add(cb.greaterThanOrEqualTo(root.get("moment"), dateFrom
                        .atStartOfDay()
                        .atOffset(ZoneOffset.of("-5"))));
            }
            if (dateUntil != null) {
                ls.add(cb.lessThanOrEqualTo(root.get("moment"), dateUntil
                        .atTime(LocalTime.MAX)
                        .atOffset(ZoneOffset.of("-5"))));
            }
            return ls.isEmpty() ? null : cb.and(ls);
        }
    }

}
