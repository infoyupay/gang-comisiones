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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TransactionManager interface provides methods for managing JPA transactions.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface TransactionManager {
    /**
     * Returns the EntityManagerFactory used by this TransactionManager.
     *
     * @return the EntityManagerFactory.
     */
    EntityManagerFactory emf();

    /**
     * Executes a specified transactional operation within a newly created {@link EntityTransaction}.
     * Automatically starts and commits the transaction. If an exception occurs, the transaction
     * is rolled back.
     *
     * @param <T>             the type of the result produced by the transactional operation
     * @param transactionBody a {@link Function} which takes the
     *                        {@link EntityManager} as parameters and defines the logic to execute
     *                        within the transaction
     * @return the result of the transactional operation
     * @throws RuntimeException if any runtime exception occurs during the transaction process;
     *                          the transaction is rolled back in such cases
     */
    @SuppressWarnings("resource")
    default <T> T runInTransaction(Function<EntityManager, T> transactionBody) {
        EntityTransaction et = null;
        try (var em = emf().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            var r = transactionBody.apply(em);
            et.commit();
            return r;
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) {
                try {
                    et.rollback();
                } catch (Exception ex) {
                    e.addSuppressed(ex);
                }
            }
            throw e;
        }
    }

    /**
     * Executes a specified transactional operation asynchronously within a newly created {@link EntityTransaction}.
     * Automatically starts and commits the transaction. If an exception occurs, the transaction is rolled back.
     * This method delegates to the synchronous {@code runInTransaction} method and executes it in a separate thread.
     *
     * @param <T>             the type of the result produced by the transactional operation
     * @param transactionBody a {@link Function} which takes the {@link EntityManager}
     *                        as parameter and defines the logic to execute within the transaction
     * @param executorService the executor service to use in the future.
     * @return a {@link CompletableFuture} that completes with the result of the transactional operation
     * @throws RuntimeException if any runtime exception occurs during the transaction process;
     *                          the transaction is rolled back in such cases
     */
    default <T> CompletableFuture<T> runInTransactionAsync(Function<EntityManager, T> transactionBody,
                                                           @NotNull ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> runInTransaction(transactionBody), executorService);
    }

    /**
     * Executes a specified transactional operation within a newly created {@link EntityTransaction}.
     * Automatically starts and commits the transaction. If an exception occurs during execution,
     * the transaction is rolled back.
     *
     * @param transactionBody a {@link Consumer} which takes the
     *                        {@link EntityManager} as parameter and defines the logic to execute
     *                        within the transaction. The operation does not return a result.
     * @throws RuntimeException if any runtime exception occurs during the transaction process;
     *                          the transaction is rolled back in such cases.
     */
    default void runVoidInTransaction(Consumer<EntityManager> transactionBody) {
        runInTransaction((em) -> {
            transactionBody.accept(em);
            return null;
        });
    }

    /**
     * Executes a specified transactional operation asynchronously within a newly created {@link EntityTransaction}.
     * Automatically starts and commits the transaction. If an exception occurs during execution,
     * the transaction is rolled back. The operation is executed in a separate thread.
     *
     * @param transactionBody a {@link Consumer} which takes the {@link EntityManager}
     *                        as parameter and defines the logic to execute within the transaction. The operation does
     *                        not return a result.
     * @param executorService the executor service to use in the future.
     * @return a {@link CompletableFuture} that completes when the transactional operation is finished.
     * If an exception occurs during execution, the transaction is rolled back, and the returned
     * {@link CompletableFuture} completes exceptionally.
     */
    default CompletableFuture<Void> runVoidInTransactionAsync(Consumer<EntityManager> transactionBody,
                                                              @NotNull ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> runVoidInTransaction(transactionBody), executorService);
    }

}
