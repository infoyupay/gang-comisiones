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
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for {@link Concept} entity.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConceptIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Shared entity manager.
     */
    private EntityManager em;

    /**
     * Cleans the table before each test.
     */
    @BeforeEach
    void setUp() {
        em = ctx.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("TRUNCATE TABLE public.concept CASCADE").executeUpdate();
        em.getTransaction().commit();
    }

    /**
     * Rolls back any pending transaction and closes entity manager after each test.
     */
    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.close();
    }

    /**
     * Tests the persistence of a valid concept is commited ok.
     */
    @Test
    void persistValidConcept_shouldPass() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        em.persist(c);
        em.flush();
        em.getTransaction().commit();

        assertNotNull(c.getId());
    }

    /**
     * Tests that a concept with null name fails.
     */
    @Test
    void persistConceptWithNullName_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name(null)
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a concept with empty name fails.
     */
    @Test
    void persistConceptWithEmptyName_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a concept with null type fails.
     */
    @Test
    void persistConceptWithNullType_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(null)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a concept with null value fails.
     */
    @Test
    void persistConceptWithNullValue_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(null)
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a {@link ConceptType#FIXED} concept with negative value fails
     * due a constraint check rejection.
     */
    @Test
    void persistConceptWithInvalidValueForFixed_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("-1.0"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a {@link ConceptType#RATE} concept with value > 1.0 fails
     * due a constraint check rejection.
     */
    @Test
    void persistConceptWithInvalidValueForRate_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.RATE)
                .value(new BigDecimal("1.5"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a concept with value > 99.9999 fails
     * due an overflow of DECIMAL(6,4).
     */
    @Test
    void persistConceptWithInvalidValueOverflow_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.RATE)
                .value(new BigDecimal("123.4567"))
                .active(true)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a {@link Concept} with null active fails due a not null directive in db.
     */
    @Test
    void persistConceptWithNullActive_shouldFail() {
        em.getTransaction().begin();
        Concept c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("23.4567"))
                .active(null)
                .build();
        em.persist(c);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }
}
