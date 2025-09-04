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
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.BankService;
import com.yupay.gangcomisiones.services.TransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link BankService}.
 *
 * @param emf          the entity manager factory to use in the service instance.
 * @param jdbcExecutor the executor service to use for executing JDBC operations.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record BankServiceImpl(@NotNull EntityManagerFactory emf,
                              @NotNull ExecutorService jdbcExecutor)
        implements BankService, TransactionManager {

    @Contract(" -> new")
    @Override
    public @NotNull CompletableFuture<List<Bank>> listAllBanks() {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                return em.createQuery("SELECT b FROM Bank b", Bank.class)
                        .getResultList();
            }
        }, jdbcExecutor);
    }


    @Contract(" -> new")
    @Override
    public @NotNull CompletableFuture<List<Bank>> listAllActiveBanks() {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                return em.createQuery("SELECT b FROM Bank b WHERE b.active = true", Bank.class)
                        .getResultList();
            }
        }, jdbcExecutor);
    }


    @Contract("_ -> new")
    @Override
    public @NotNull CompletableFuture<Bank> createBank(@NotNull String name) {
        return runInTransactionAsync(em -> {
            ensureAdminUser(em);
            var bank = Bank.builder()
                    .name(name)
                    .active(true)
                    .build();
            em.persist(bank);
            AuditAction.BANK_CREATE.log(em, bank.getId());
            return bank;
        });
    }


    @Contract("_, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Void> updateBank(int id,
                                                       @NotNull String name,
                                                       @NotNull Boolean active) {
        return runVoidInTransactionAsync(em -> {
            ensureAdminUser(em);
            var ref = em.getReference(Bank.class, id);
            ref.setName(name);
            ref.setActive(active);
            AuditAction.BANK_UPDATE.log(em, id);
        });
    }

    /**
     * Ensures that the current user has administrative privileges.
     * <br/>
     * This method retrieves the current user's session and validates their role.
     * If the user is not logged in, has a null role, or does not possess at least
     * administrative-level privileges, an exception is thrown.
     *
     * @param em The EntityManager to use for database operations.
     * @throws PersistenceServicesException if the current user does not have at least ADMIN privileges.
     */
    private void ensureAdminUser(EntityManager em) {
        User current = AppContext.getInstance().getUserSession().getCurrentUser();
        if (!AppContext.getInstance().getUserService().contrastUserPrivileges(em, current.getId(), UserRole.ADMIN)) {
            throw new PersistenceServicesException("At least ADMIN privileges are required to run this operation.");
        }
    }
}
