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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link Concept} entity to ensure compliance with
 * persistence constraints and business rules. This class extends
 * {@link AbstractPostgreIntegrationTest} to leverage pre-configured PostgreSQL
 * integration testing setup.
 * <p>
 * <br/>
 * The tests cover the following scenarios:
 * <ol>
 *   <li>Ensuring a valid concept can be persisted successfully.</li>
 *   <li>Failure cases for constraint violations, including:
 *     <ul>
 *       <li>Null or empty concept name.</li>
 *       <li>Null concept type.</li>
 *       <li>Null concept value.</li>
 *       <li>Invalid {@link ConceptType#FIXED} value (e.g., negative values).</li>
 *       <li>Invalid {@link ConceptType#RATE} value (e.g., a value greater than 1.0).</li>
 *       <li>Value overflow for DECIMAL(6,4).</li>
 *       <li>Null active flag.</li>
 *     </ul>
 *   </li>
 * </ol>
 * <p>
 * <br/>
 * Testing strategy:
 * <ul>
 *   <li>Before each test, database constraints are reset using a utility method.</li>
 *   <li>Utility methods are used for transaction management and exception expectations.</li>
 * </ul>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution note:</strong> tested by dvidal@infoyupay.com passed 10 in 1.385ms at 2025-09-25 22:23 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
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
        //Arrange
        var c = performInTransaction(em -> {
            var r = Concept.builder()
                    .name("Telephone Bill")
                    .type(ConceptType.FIXED)
                    .value(new BigDecimal("12.4567"))
                    .active(true)
                    .build();
            //Act
            em.persist(r);
            return r;
        });
        //Assert
        assertThat(c.getId())
                .describedAs("Concept ID must be positive and non-null, found {}", c.getId())
                .isNotNull()
                .isGreaterThan(0);
    }

    /**
     * Tests that a concept with null name fails.
     */
    @Test
    void persistConceptWithNullName_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "null, FIXED, 12.4567, t",
                em -> em.persist(Concept.builder()
                        .name(null)
                        .type(ConceptType.FIXED)
                        .value(new BigDecimal("12.4567"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a concept with empty name fails.
     */
    @Test
    void persistConceptWithEmptyName_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "chk_concept_name_nonempty",
                em -> em.persist(Concept.builder()
                        .name("")
                        .type(ConceptType.FIXED)
                        .value(new BigDecimal("12.4567"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a concept with null type fails.
     */
    @Test
    void persistConceptWithNullType_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "Telephone Bill, null, 12.4567, t",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(null)
                        .value(new BigDecimal("12.4567"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a concept with null value fails.
     */
    @Test
    void persistConceptWithNullValue_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "Telephone Bill, FIXED, null, t",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(ConceptType.FIXED)
                        .value(null)
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a {@link ConceptType#FIXED} concept with negative value fails
     * due a constraint check rejection.
     */
    @Test
    void persistConceptWithInvalidValueForFixed_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "chk_concept_value_valid",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(ConceptType.FIXED)
                        .value(new BigDecimal("-1.0"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a {@link ConceptType#RATE} concept with value > 1.0 fails
     * due a constraint check rejection.
     */
    @Test
    void persistConceptWithInvalidValueForRate_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "chk_concept_value_valid",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(ConceptType.RATE)
                        .value(new BigDecimal("1.5"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a concept with value > 99.9999 fails
     * due an overflow of DECIMAL(6,4).
     */
    @Test
    void persistConceptWithInvalidValueOverflow_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "10^2",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(ConceptType.RATE)
                        .value(new BigDecimal("123.4567"))
                        .active(true)
                        .build()));
    }

    /**
     * Tests that a {@link Concept} with null active fails due a not null directive in db.
     */
    @Test
    void persistConceptWithNullActive_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "Telephone Bill, FIXED, 5.0000, null",
                em -> em.persist(Concept.builder()
                        .name("Telephone Bill")
                        .type(ConceptType.FIXED)
                        .value(new BigDecimal("5.00"))
                        .active(null)
                        .build()));
    }


    /**
     * Tests the failure of persisting two {@link Concept} entities with identical names due to a database
     * constraint violation.
     * <br/>
     * This method attempts to persist two {@link Concept} objects with the same {@code name} value ("Telephone Bill")
     * but with different {@code type} and {@code value}.
     * <br/>
     * The operation is expected to result in a {@code SQLException}, triggered by the unique constraint violation
     * on the {@code name} column (likely enforced by the {@code uq_concept_name} constraint).
     * <br/>
     * <br/>
     * <b>Expected Behavior:</b>
     * <ul>
     *   <li>The operation fails during flush due to a unique constraint violation.</li>
     *   <li>A {@link SQLException} is thrown.</li>
     *   <li>The exception message contains the constraint name {@code uq_concept_name}.</li>
     * </ul>
     * <br/>
     * <b>Purpose:</b>
     * <ol>
     *   <li>Ensure duplication of names in {@link Concept} entities is prohibited.</li>
     *   <li>Validate the database handles unique constraint violations as expected.</li>
     *   <li>Automatically roll back the transaction to maintain data integrity.</li>
     * </ol>
     */
    @Test
    void persistConceptWithDuplicatedName_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "uq_concept_name",
                em -> {
                    em.persist(Concept.builder()
                            .name("Telephone Bill")
                            .type(ConceptType.FIXED)
                            .value(new BigDecimal("5.00"))
                            .active(true)
                            .build());
                    em.persist(Concept.builder()
                            .name("Telephone Bill")
                            .type(ConceptType.RATE)
                            .value(new BigDecimal("0.20"))
                            .active(true)
                            .build());
                });
    }
}
