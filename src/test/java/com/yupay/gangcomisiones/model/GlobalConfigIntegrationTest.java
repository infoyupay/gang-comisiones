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


import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The integration test for GlobalConfiguration.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class GlobalConfigIntegrationTest extends AbstractPostgreIntegrationTest {

    private EntityManager em;

    /**
     * Initialize the EntityManager.
     */
    @BeforeEach
    void setup() {
        em = ctx.getEntityManagerFactory().createEntityManager();
    }

    /**
     * Close the EntityManager.
     */
    @AfterEach
    void cleanup() {
        if (em != null) {
            em.close();
        }
    }

    /**
     * Build a valid GlobalConfig object builder.
     *
     * @return the builder.
     */
    private GlobalConfig.GlobalConfigBuilder buildValidConfig() {
        return GlobalConfig
                .builder()
                .id((short) 1)
                .ruc("12345678901")
                .legalName("Empresa Legal SAC")
                .businessName("Empresa Comercial")
                .address("Av. Principal 123")
                .updatedBy(User.forTest(1L))
                .updatedFrom("mylaptop");
    }

    /**
     * Tests that persisting GlobalConfig with id!=1 fails.
     */
    @Test
    void persistConfig_withIdNotOne_shouldFail() {
        var gc = buildValidConfig().id((short) 2).build();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        var ex = assertThrows(RuntimeException.class, () -> {
            em.persist(gc);
            em.flush();
        });
        assertTrue(
                ex instanceof PersistenceException ||
                        ex instanceof IllegalStateException,
                "Expected a persistence-related failure but got: " + ex
        );
        tx.rollback();
    }

    /**
     * Tests that persisting GlobalConfig with invalid RUC length fails.
     */
    @Test
    void persistConfig_withInvalidRucLength_shouldFail() {
        GlobalConfig gc = buildValidConfig().ruc("123").build();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        var ex = assertThrows(RuntimeException.class, () -> {
            em.persist(gc);
            em.flush();
        });
        assertTrue(
                ex instanceof PersistenceException ||
                        ex instanceof IllegalStateException,
                "Expected a persistence-related failure but got: " + ex
        );
        tx.rollback();
    }

    /**
     * Tests that persisting GlobalConfig with null mandatory fields fails.
     */
    @Test
    void persistConfig_withNullMandatoryFields_shouldFail() {
        String[] nullFields = {"legalName", "businessName", "address", "updatedBy", "updatedFrom"};

        for (String field : nullFields) {
            GlobalConfig gc = buildValidConfig().build();
            switch (field) {
                case "legalName" -> gc.setLegalName(null);
                case "businessName" -> gc.setBusinessName(null);
                case "address" -> gc.setAddress(null);
                case "updatedBy" -> gc.setUpdatedBy(null);
                case "updatedFrom" -> gc.setUpdatedFrom(null);
            }

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            var ex = assertThrows(RuntimeException.class, () -> {
                em.persist(gc);
                em.flush();
            }, "Expected failure when " + field + " is null");
            assertTrue(
                    ex instanceof PersistenceException ||
                            ex instanceof IllegalStateException,
                    "Expected a persistence-related failure but got: " + ex
            );
            tx.rollback();
        }
    }

    /**
     * Tests that persisting GlobalConfig with mandatory fields null, fails.
     *
     * @param field the field name to test.
     */
    @ParameterizedTest
    @ValueSource(strings = {"legalName", "businessName", "address", "updatedBy", "updatedFrom"})
    void persistConfig_withNullMandatoryField_shouldFail(String field) {
        GlobalConfig gc = buildValidConfig().build();

        switch (field) {
            case "legalName" -> gc.setLegalName(null);
            case "businessName" -> gc.setBusinessName(null);
            case "address" -> gc.setAddress(null);
            case "updatedBy" -> gc.setUpdatedBy(null);
            case "updatedFrom" -> gc.setUpdatedFrom(null);
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        var ex = assertThrows(RuntimeException.class, () -> {
            em.persist(gc);
            em.flush();
        }, "Expected failure when " + field + " is null");
        assertTrue(
                ex instanceof PersistenceException ||
                        ex instanceof IllegalStateException,
                "Expected a persistence-related failure but got: " + ex
        );
        tx.rollback();
    }
}
