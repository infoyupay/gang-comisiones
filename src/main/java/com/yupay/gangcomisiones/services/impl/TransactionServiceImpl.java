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
import com.yupay.gangcomisiones.model.Transaction;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.TransactionManager;
import com.yupay.gangcomisiones.services.TransactionService;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
    public CompletableFuture<Void> createTransaction(Transaction transaction) {
        return runVoidInTransactionAsync(em -> {
            var cashier = transaction.getCashier();
            if (cashier == null) {
                throw new PersistenceServicesException("Transaction's Cashier is null.");
            }
            AppContext.getInstance().getUserService().checkPrivilegesOrException(
                    em,
                    cashier.getId(),
                    UserRole.CASHIER);
            em.persist(transaction);
            AuditAction.TRANSACTION_CREATE.log(em, transaction.getId());
        });
    }

}
