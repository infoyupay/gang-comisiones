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

package com.yupay.gangcomisiones.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Facility to centralize test entities creation and persist.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class TestPersistedEntities {
    private static final AtomicReference<User> USER = new AtomicReference<>(null);
    private static final AtomicReference<Concept> CONCEPT = new AtomicReference<>(null);
    private static final AtomicReference<Bank> BANK = new AtomicReference<>(null);
    private static final AtomicReference<Transaction> TRANSACTION = new AtomicReference<>(null);
    private static Logger LOG;

    /**
     * Inner logger.
     *
     * @return the logger.
     */
    private static Logger getLog() {
        if (LOG == null) {
            LOG = LoggerFactory.getLogger(TestPersistedEntities.class);
        }
        return LOG;
    }

    /**
     * Cleans all created values from memory and truncates all database tables.
     *
     * @param emf the entity manager factory.
     */
    public static void clean(EntityManagerFactory emf) {
        USER.set(null);
        CONCEPT.set(null);
        BANK.set(null);
        TRANSACTION.set(null);
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE reversal_request CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE transaction CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE concept CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE bank CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE global_config CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE audit_Log CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE \"user\" CASCADE").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(TestPersistedEntities.class).error("Cannot truncate tables.", e);
        }
    }

    /**
     * Creates and persists a valid test user.
     *
     * @param em the entity manager.
     * @return the created user.
     */
    public static User persistUser(@NotNull EntityManager em) {
        if (USER.get() == null) {
            var r = User.builder()
                    .active(true)
                    .password("password")
                    .role(UserRole.ROOT)
                    .username("rootUser")
                    .build();
            em.persist(r);
            USER.set(r);
        }
        return USER.get();
    }

    /**
     * Creates and persists a valid test concept.
     *
     * @param em the entity manager.
     * @return the created concept.
     */
    public static Concept persistConcept(@NotNull EntityManager em) {
        if (CONCEPT.get() == null) {
            var r = Concept.builder()
                    .active(true)
                    .name("Test Concept")
                    .type(ConceptType.FIXED)
                    .value(new BigDecimal("10.0000"))
                    .build();
            em.persist(r);
            CONCEPT.set(r);
        }
        return CONCEPT.get();
    }

    /**
     * Creates and persists a valid test bank.
     *
     * @param em the entity manager.
     * @return the created bank.
     */
    public static Bank persistBank(@NotNull EntityManager em) {
        if (BANK.get() == null) {
            var r = Bank.builder()
                    .active(true)
                    .name("Test Bank")
                    .build();
            em.persist(r);
            BANK.set(r);
        }
        return BANK.get();
    }

    /**
     * Creates a valid test transaction persisting dependents entities.
     *
     * @param em the entity manager.
     * @return the created transaction.
     */
    public static Transaction buildValidTansaction(EntityManager em) {
        return Transaction.builder()
                .bank(persistBank(em))
                .concept(persistConcept(em))
                .cashier(persistUser(em))
                .amount(new BigDecimal("10.0000"))
                .commission(new BigDecimal("1.0000"))
                .status(TransactionStatus.REGISTERED)
                .build();
    }

    /**
     * Creates and persists a valid test transaction.
     *
     * @param em the entity manager.
     * @return the created transaction.
     */
    public static Transaction persistTransaction(EntityManager em) {
        if (TRANSACTION.get() == null) {
            var r = buildValidTansaction(em);
            em.persist(r);
            TRANSACTION.set(r);
        }
        return TRANSACTION.get();
    }

}
