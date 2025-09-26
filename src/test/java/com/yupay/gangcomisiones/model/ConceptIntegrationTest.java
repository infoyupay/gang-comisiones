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
import com.yupay.gangcomisiones.assertions.CauseAssertions;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test for {@link Concept} entity.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConceptIntegrationTest extends AbstractPostgreIntegrationTest {

    /**
     * Cleans the table before each test.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests the persistence of a valid concept is commited ok.
     */
    @Test
    void persistValidConcept_shouldPass() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            em.persist(c);
            em.flush();
            et.commit();
        } catch (Exception e) {
            if (et != null && et.isActive()) et.rollback();
            fail(e);
        }
        Assertions.assertThat(c.getId())
                .as("The Concept ID must not be null.")
                .isNotNull();
    }

    /**
     * Tests that a concept with null name fails.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithNullName_shouldFail() {
        var c = Concept.builder()
                .name(null)
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                            .assertCauseWithMessage(
                                    catchThrowableOfType(PersistenceException.class, em::flush),
                                    "null, FIXED, 12.4567, t");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a concept with empty name fails.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithEmptyName_shouldFail() {
        var c = Concept.builder()
                .name("")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "chk_concept_name_nonempty");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a concept with null type fails.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithNullType_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(null)
                .value(new BigDecimal("12.4567"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "Telephone Bill, null, 12.4567, t");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a concept with null value fails.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithNullValue_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(null)
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "Telephone Bill, FIXED, null, t");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a {@link ConceptType#FIXED} concept with negative value fails
     * due a constraint check rejection.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithInvalidValueForFixed_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("-1.0"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "chk_concept_value_valid");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a {@link ConceptType#RATE} concept with value > 1.0 fails
     * due a constraint check rejection.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithInvalidValueForRate_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.RATE)
                .value(new BigDecimal("1.5"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "chk_concept_value_valid");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a concept with value > 99.9999 fails
     * due an overflow of DECIMAL(6,4).
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithInvalidValueOverflow_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.RATE)
                .value(new BigDecimal("123.4567"))
                .active(true)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "10^2");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }

    /**
     * Tests that a {@link Concept} with null active fails due a not null directive in db.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void persistConceptWithNullActive_shouldFail() {
        var c = Concept.builder()
                .name("Telephone Bill")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("12.4567"))
                .active(null)
                .build();
        EntityTransaction et = null;
        try(var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et= em.getTransaction();
            et.begin();
            em.persist(c);
            CauseAssertions.assertExpectedCause(PersistenceException.class)
                    .assertCauseWithMessage(
                            catchThrowableOfType(PersistenceException.class, em::flush),
                            "Telephone Bill, FIXED, 12.4567, null");
            et.rollback();
        } catch (RuntimeException e) {
            if (et != null && et.isActive()) et.rollback();
            Assertions.fail(e);
        }
    }
}
