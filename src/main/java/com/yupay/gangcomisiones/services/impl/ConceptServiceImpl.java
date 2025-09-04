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
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.ConceptService;
import com.yupay.gangcomisiones.services.TransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of {@link ConceptService}.
 *
 * @param emf          the entity manager factory to use in the service instance.
 * @param jdbcExecutor the executor service to use for executing JDBC operations.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record ConceptServiceImpl(@NotNull EntityManagerFactory emf,
                                 @NotNull ExecutorService jdbcExecutor)
        implements ConceptService, TransactionManager {

    /**
     * Lists all concepts.
     *
     * @return a future with all concepts.
     */
    @Contract(" -> new")
    @Override
    public @NotNull CompletableFuture<List<Concept>> listAllConcepts() {
        return runWithoutTransactionAsync(em -> em
                .createQuery("SELECT c FROM Concept c", Concept.class)
                .getResultList());
    }

    /**
     * Lists all active concepts.
     *
     * @return a future with active concepts.
     */
    @Contract(" -> new")
    @Override
    public @NotNull CompletableFuture<List<Concept>> listAllActiveConcepts() {
        return runWithoutTransactionAsync(em ->
                em.createQuery("SELECT c FROM Concept c WHERE c.active = true", Concept.class)
                        .getResultList());
    }

    /**
     * Creates a concept ensuring admin privileges and auditing the operation.
     */
    @Contract("_, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Concept> createConcept(@NotNull String name,
                                                             @NotNull ConceptType type,
                                                             @NotNull BigDecimal value) {
        return runInTransactionAsync(em -> {
            ensureAdminUser(em);
            var concept = Concept.builder()
                    .name(name)
                    .type(type)
                    .value(value)
                    .active(true)
                    .build();
            em.persist(concept);
            AuditAction.CONCEPT_CREATE.log(em, concept.getId());
            return concept;
        });
    }

    /**
     * Updates a concept ensuring admin privileges and auditing the operation.
     */
    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Void> updateConcept(long id,
                                                          @NotNull String name,
                                                          @NotNull ConceptType type,
                                                          @NotNull BigDecimal value,
                                                          @NotNull Boolean active) {
        return runVoidInTransactionAsync(em -> {
            ensureAdminUser(em);
            var ref = em.getReference(Concept.class, id);
            ref.setName(name);
            ref.setType(type);
            ref.setValue(value);
            ref.setActive(active);
            AuditAction.CONCEPT_UPDATE.log(em, id);
        });
    }

    /**
     * Ensures that the current user has administrative privileges.
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
