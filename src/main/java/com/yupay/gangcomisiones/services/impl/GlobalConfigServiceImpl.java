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
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.GlobalConfigService;
import com.yupay.gangcomisiones.services.TransactionManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Default implementation of {@link GlobalConfigService} that uses JPA for persistence
 * and an {@link ExecutorService} for asynchronous execution.
 * <br/>
 * Responsibilities include:
 * <ul>
 *   <li>Fetching the single {@link GlobalConfig} record from the database.</li>
 *   <li>Persisting updates to the {@link GlobalConfig} in a transactional context.</li>
 *   <li>Completing returned {@link CompletableFuture} instances upon operation completion.</li>
 * </ul>
 *
 * @param emf          the JPA {@link EntityManagerFactory} used to create entity managers for database operations.
 * @param jdbcExecutor the {@link ExecutorService} used to run blocking persistence work asynchronously.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record GlobalConfigServiceImpl(EntityManagerFactory emf,
                                      ExecutorService jdbcExecutor)
        implements GlobalConfigService, TransactionManager {
    @Override
    public CompletableFuture<Optional<GlobalConfig>> fetchGlobalConfig() {
        return runWithoutTransactionAsync(em -> Optional
                .ofNullable(em.find(GlobalConfig.class, (short) 1)));
    }

    @Override
    public CompletableFuture<Void> updateGlobalConfig(GlobalConfig config) {
        return runVoidInTransactionAsync(em -> {
            var currentUser = AppContext.getInstance().getUserSession().getCurrentUser();
            AppContext.getInstance().getUserService().checkPrivilegesOrException(em, currentUser.getId(), UserRole.ROOT);
            em.merge(config);
            AuditAction.GLOBAL_CONFIG_UPDATE.log(em, 1);
        });
    }
}
