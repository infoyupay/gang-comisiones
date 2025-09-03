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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link ReversalRequest} entity.
 * <br/>
 * dvidal executed and passed 8 tests in 1.83 ms
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class ReversalRequestIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Clean all created values from memory and truncate all database tables.
     */
    @BeforeEach
    void cleanTables() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests the persistence of a valid {@code ReversalRequest} entity into the database.
     * <br/>
     * This method verifies that when a valid {@code ReversalRequest} is created and persisted,
     * the following conditions are met:
     * - The entity is successfully saved in the database.
     * - The generated identifier ({@code id}) is not null after persistence.
     * - The timestamp ({@code requestStamp}) is automatically generated and is not null after persistence.
     * <br/>
     * The test executes the following steps:
     * <ol>
     * <li>
     *     Opens an {@code EntityManager} and begins a transaction.
     * </li>
     * <li>
     *     Creates a {@code ReversalRequest} entity with its required fields populated:
     *     <ul>
     *           <li>
     *     {@code evaluatedBy}, {@code message}, {@code requestedBy}, {@code status}, and {@code transaction}.
     *          </li>
     *     </ul>
     * </li>
     * <li> Persists the {@code ReversalRequest} entity and commits the transaction.</li>
     * <li> Refreshes the entity to ensure it is loaded with database-generated values.</li>
     * <li> Asserts that the primary key ({@code id}) and request timestamp ({@code requestStamp})
     * of the entity are not null.</li>
     * </ol>
     * <br/>
     * The test relies on supporting methods from the {@code TestPersistedEntities} class to create
     * and persist related entities such as {@code User} and {@code Transaction}, which are necessary
     * for fulfilling the foreign key constraints of the {@code ReversalRequest} entity.
     * <br/>
     * Expected behavior:
     * <ul>
     * <li> The test validates the correct persistence of all required fields in the database.</li>
     * <li> Ensures the proper functioning of the transactional workflow for a {@code ReversalRequest}.</li>
     * </ul>
     * <br/>
     *
     * @throws RuntimeException if any error occurs during transaction operations.
     * @implNote The database used for this test should have proper configuration for the {@code ReversalRequest} table
     * and related entities, including sequences and default values.
     * <br/>
     */
    @Test
    void persistValidReversalRequest() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();

            var rr = ReversalRequest
                    .builder()
                    .evaluatedBy(TestPersistedEntities.persistUser(em))
                    .message("Please, do it for me.")
                    .requestedBy(TestPersistedEntities.persistUser(em))
                    .status(ReversalRequestStatus.PENDING)
                    .transaction(TestPersistedEntities.persistTransaction(em))
                    .build();
            em.persist(rr);
            et.commit();
            em.refresh(rr);

            assertNotNull(rr.getId());
            assertNotNull(rr.getRequestStamp());
        }
    }

    /**
     * Tests the null constraint validation for specific fields of the {@code ReversalRequest} entity.
     * Each test case corresponds to a specific field of the {@code ReversalRequest} entity. The test ensures that
     * a {@code ReversalRequest} fails to commit when any of the required fields are set to null, thus verifying
     * the database constraints.
     *
     * @param field the field of the {@code ReversalRequest} entity to be tested for null constraint validation.
     *              Possible values are defined in the {@code Field} enum: {@code TRANSACTION}, {@code MESSAGE},
     *              {@code REQUESTED_BY}, and {@code STATUS}. Each value represents a field that is critical
     *              for the entity's validity and must not be null.
     */
    @ParameterizedTest
    @EnumSource(value = Field.class)
    void nullConstraints(@NotNull Field field) {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();

            var rr = ReversalRequest.builder()
                    .evaluatedBy(TestPersistedEntities.persistUser(em))
                    .message("Please, do it for me.")
                    .requestedBy(TestPersistedEntities.persistUser(em))
                    .status(ReversalRequestStatus.PENDING)
                    .transaction(TestPersistedEntities.persistTransaction(em))
                    .build();

            switch (field) {
                case TRANSACTION -> rr.setTransaction(null);
                case MESSAGE -> rr.setMessage(null);
                case REQUESTED_BY -> rr.setRequestedBy(null);
                case STATUS -> rr.setStatus(null);
            }

            em.persist(rr);
            expectCommitFailure(et);
        }
    }

    /**
     * Tests the one-to-one transaction constraint enforcement for the {@code ReversalRequest} entity.
     * <br/>
     * This method ensures that the database enforces a one-to-one relationship constraint on the
     * {@code Transaction} field of the {@code ReversalRequest} entity. Specifically, it validates that
     * only one {@code ReversalRequest} can be associated with a specific {@code Transaction}.
     * <br/>
     * The test performs the following steps:
     * <ul>
     * <li> Opens an {@code EntityManager} and begins a database transaction.</li>
     * <li> Creates two {@code ReversalRequest} entities using unique data, but associates both entities
     * with separate automatically generated {@code Transaction} instances via the {@code TestPersistedEntities} class.</li>
     * <li> Persists both {@code ReversalRequest} entities into the database within the same transaction.</li>
     * <li> Attempts to commit the transaction and expects the database to throw a constraint violation
     * exception due to the one-to-one relationship constraint on the {@code Transaction} field.</li>
     * </ul>
     * <br/>
     * Expected behavior:
     * <ul>
     * <li> The transaction fails to commit, demonstrating enforcement of the one-to-one constraint.</li>
     * <li> The failure is explicitly caught and logged within the test to verify correct behavior.</li>
     * </ul>
     * <br/>
     * This test depends on an active and properly configured database schema where the {@code ReversalRequest}
     * entity has the necessary one-to-one constraint defined on the {@code Transaction} field.
     *
     * @throws RuntimeException if any error occurs during the persistence or transaction operations.
     * @implNote Ensure that the {@code Transaction} and related entities, as persisted in this test,
     * are correctly configured with a one-to-one mapping constraint enforced by the database schema.
     */
    @Test
    void oneToOneTransactionConstraint() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();

            var rr1 = ReversalRequest
                    .builder()
                    .evaluatedBy(TestPersistedEntities.persistUser(em))
                    .message("Please, do it for me 1.")
                    .requestedBy(TestPersistedEntities.persistUser(em))
                    .status(ReversalRequestStatus.PENDING)
                    .transaction(TestPersistedEntities.persistTransaction(em))
                    .build();
            em.persist(rr1);

            var rr2 = ReversalRequest
                    .builder()
                    .evaluatedBy(TestPersistedEntities.persistUser(em))
                    .message("Please, do it for me 2.")
                    .requestedBy(TestPersistedEntities.persistUser(em))
                    .status(ReversalRequestStatus.PENDING)
                    .transaction(TestPersistedEntities.persistTransaction(em))
                    .build();
            em.persist(rr2);

            expectCommitFailure(et);
        }
    }

    /**
     * Verifies that optional fields in the {@code ReversalRequest} entity can be null when the entity
     * is persisted in the database.
     * <br/>
     * This test performs the following steps:
     * <ul>
     * <li> Opens an {@code EntityManager} and begins a transaction.</li>
     * <li> Creates a {@code ReversalRequest} entity with required fields populated:
     *   {@code message}, {@code requestedBy}, {@code status}, and {@code transaction}.</li>
     * <li> Ensures that optional fields such as {@code answer}, {@code answerStamp}, and {@code evaluatedBy}
     *   are not initialized (i.e., left null).</li>
     * <li> Persists the entity and commits the transaction.</li>
     * <li> Refreshes the persisted entity to retrieve any changes from the database.</li>
     * <li> Asserts that the optional fields remain null after persistence.</li>
     * </ul>
     * <br/>
     * Expected behavior:
     * <ul>
     * <li> The {@code ReversalRequest} entity is successfully persisted even if the optional fields
     *   are null.</li>
     * <li> The test ensures that the database allows null values for optional fields in this entity.</li>
     * </ul>
     * <br/>
     * This test uses entities and helper methods from the {@code TestPersistedEntities} class to
     * create required related entities for the {@code ReversalRequest} instance, such as {@code User}
     * and {@code Transaction}.
     *
     * @throws RuntimeException if any error occurs during the persistence or transaction operations.
     * @implNote Ensure that the database schema allows null values for optional fields and does not
     * enforce constraints for non-mandatory attributes.
     */
    @Test
    void optionalFieldsCanBeNull() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();


            var rr = ReversalRequest
                    .builder()
                    .message("Please, do it for me 1.")
                    .requestedBy(TestPersistedEntities.persistUser(em))
                    .status(ReversalRequestStatus.PENDING)
                    .transaction(TestPersistedEntities.persistTransaction(em))
                    .build();

            em.persist(rr);
            et.commit();
            em.refresh(rr);

            assertNull(rr.getAnswer());
            assertNull(rr.getAnswerStamp());
            assertNull(rr.getEvaluatedBy());
        }
    }

    /**
     * Tests the `equals` and `hashCode` methods of the `ReversalRequest` entity.
     * <br/>
     * This test verifies the following properties of the `equals` and `hashCode` contract:
     * <ol>
     * <li> Reflexivity: An object must be equal to itself.</li>
     * <li> Symmetry: Two objects that are equal must have the same hash code.</li>
     * <li> Consistency: Two objects that are not equal must not have the same hash code.</li>
     * </ol>
     * <br/>
     * Test operations:
     * <ul>
     * <li>Verifies that two `ReversalRequest` objects with the same `id` are considered equal.</li>
     * <li>Ensures that `ReversalRequest` instances with different `id` fields are not equal.</li>
     * <li>Checks the `hashCode` consistency for equivalent objects.</li>
     * <li>Validates that a `ReversalRequest` instance is always equal to itself.</li>
     * </ul>
     * <br/>
     * This method ensures the proper implementation of the `equals` and `hashCode` methods for the `ReversalRequest` entity,
     * which is critical for correctly handling operations that rely on equality checks, such as in collections or persistence contexts.
     */
    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsAndHashCode() {
        var rr1 = ReversalRequest.builder().id(1L).build();
        var rr2 = ReversalRequest.builder().id(1L).build();

        assertEquals(rr1, rr2);
        assertEquals(rr1.hashCode(), rr2.hashCode());
        assertEquals(rr1, rr1); // reflexivity

        rr2 = ReversalRequest.builder().id(2L).build();
        assertNotEquals(rr1, rr2);

        rr1 = new ReversalRequest();
        rr2 = new ReversalRequest();
        assertNotEquals(rr1, rr2);
    }

    /**
     * Enum representing the key fields of the `ReversalRequest` entity required for validation and processing within
     * database tests. Each value corresponds to a critical property that must adhere to specific constraints or
     * participate in defined relationships.
     * <br/>
     * Enum Constants:
     * <ul>
     * <li> {@link Field#TRANSACTION}: Represents the transactional context or identifier associated with
     * the `ReversalRequest`.</li>
     * <li> {@link Field#MESSAGE}: Corresponds to the message or reason associated with
     * the reversal request, often reflecting
     *   user-provided or system-generated descriptions.</li>
     * <li> {@link Field#REQUESTED_BY}: Denotes the identification of the user or system entity
     * that initiated the reversal request.</li>
     * <li> {@link Field#STATUS}: Reflects the current state or stage of the
     * reversal request within its lifecycle.</li>
     * </ul>
     */
    enum Field {
        /**
         * Represents the transaction identifier or context associated with a `ReversalRequest`.
         * This field is critical for uniquely identifying and validating the transactional data
         * within the context of database operations and tests.
         */
        TRANSACTION,
        /**
         * Corresponds to the message or reason associated with the `ReversalRequest`.
         * This field typically describes user-provided comments or system-generated
         * explanations regarding the reversal request.
         */
        MESSAGE,
        /**
         * Represents the identifier of the user or entity that initiated the reversal request.
         * This field is used to track the origin of the request for validation and processing purposes
         * in database operations and tests.
         */
        REQUESTED_BY,
        /**
         * Reflects the current state or stage of the reversal request within its lifecycle.
         * This field is used to ascertain the progress, completion, or failure of the corresponding
         * `ReversalRequest` entity during its validation and processing within database operations.
         */
        STATUS
    }
}
