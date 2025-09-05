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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.exceptions.AppSecurityException;
import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.AuditLog;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ConceptService} operations.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class ConceptServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    private ConceptService conceptService;

    /**
     * Sets up the test environment before each test execution.
     * This method performs the following steps:
     * 1. Cleans up previously persisted entities and truncates all database tables
     * to ensure a clean state for testing.
     * 2. Initializes the {@code ConceptService} instance for use within the test cases.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        conceptService = ctx.getConceptService();
    }

    /// Verifies that the `createConcept` method in `ConceptService` correctly assigns an ID,
    /// marks the concept as active by default, and generates an audit log entry for the operation.
    /// The test performs the following validations:
    /// 1. Confirms that a unique identifier is assigned to the created Concept by the database
    ///    and that the ID is positive.
    /// 2. Ensures that the `active` field of the Concept is set to `true` upon creation.
    /// 3. Checks that the created Concept instance is included in the list of all Concepts as well as
    ///    in the list of active Concepts retrieved by the service.
    /// 4. Verifies that an audit log entry is created in the `AuditLog` database table for the new Concept,
    ///    ensuring proper tracking of changes to entity state.
    /// This test requires an admin user to be created and logged into the application session
    /// before the `createConcept` method is executed.
    ///
    /// @throws Exception if an unexpected error occurs during the execution of the test.
    @Test
    void testCreateConcept_AssignsIdActiveTrueAndAudited() throws Exception {
        // given
        UserSessionHelpers.createAndLogAdminUser();

        // when
        Concept c = conceptService.createConcept("Telephone Bill", ConceptType.FIXED, new BigDecimal("10.0000")).get();

        // then
        assertNotNull(c.getId(), "Concept id must be assigned by DB sequence");
        assertTrue(c.getId() > 0, "Concept id must be positive");
        assertEquals(Boolean.TRUE, c.getActive(), "Concept must be active after creation");

        // and must be listed in all and active
        List<Concept> all = conceptService.listAllConcepts().get();
        assertTrue(all.stream().anyMatch(x -> x.getId().equals(c.getId())));
        List<Concept> active = conceptService.listAllActiveConcepts().get();
        assertTrue(active.stream().anyMatch(x -> x.getId().equals(c.getId())));

        // and an audit log with entityId must exist
        try (EntityManager em = ctx.getEntityManagerFactory().createEntityManager()) {
            List<AuditLog> logs = em.createQuery("SELECT a FROM AuditLog a WHERE a.entityId = :id", AuditLog.class)
                    .setParameter("id", c.getId())
                    .getResultList();
            assertFalse(logs.isEmpty(), "AuditLog should contain at least one entry for created concept");
        }
    }

    /// Verifies that an unprivileged user, such as a user with the "cashier" role,
    /// is unable to create a new concept using the `ConceptService`.
    /// This test ensures the security and access control mechanisms of the system
    /// are enforced for the `createConcept` operation.
    /// The test performs the following steps:
    /// 1. Creates and logs in a cashier user by invoking `createAndLogCashierUser`.
    /// 2. Attempts to create a new concept with the type `FIXED` and a specific value using the
    ///    `createConcept` method of `ConceptService`.
    /// 3. Validates that an `ExecutionException` is thrown, indicating that the unprivileged user
    ///    is not authorized to perform the operation.
    /// 4. Confirms that the cause of the exception is a `PersistenceServicesException`,
    ///    verifying the specific error type related to unauthorized access.
    /// Ensures the system correctly prevents unauthorized concept creation actions
    /// and provides meaningful feedback when such actions are attempted.
    @Test
    void testCreateConcept_UnprivilegedUserFails() {
        UserSessionHelpers.createAndLogCashierUser();

        var ex = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept("Water Service", ConceptType.FIXED, new BigDecimal("2.0000")).get());
        assertInstanceOf(AppSecurityException.class, ex.getCause());
    }

    /// Tests that an unprivileged user, such as a user with the "cashier" role, is unable to update an existing concept
    /// using the `updateConcept` method of `ConceptService`.
    /// This test ensures that the access control mechanisms of the system correctly prevent unauthorized updates
    /// and produce appropriate exceptions when such actions are attempted.
    /// Test steps include:
    /// 1. Creating and logging in a cashier user by invoking `createAndLogCashierUser()`, thereby simulating an unprivileged user.
    /// 2. Attempting to update a concept with specified parameters, which include an ID, name, type, value, and active status.
    /// 3. Asserting that an `ExecutionException` is thrown due to lack of permissions, ensuring the update operation fails.
    /// 4. Confirming that the cause of the exception is a `PersistenceServicesException`, verifying the unauthorized access scenario.
    /// This test helps verify role-based access control for the `updateConcept` operation.
    @Test
    void testUpdateConcept_UnprivilegedUserFails() {
        UserSessionHelpers.createAndLogCashierUser();

        var ex = assertThrows(ExecutionException.class,
                () -> conceptService.updateConcept(1L, "Water Service", ConceptType.FIXED, new BigDecimal("2.0000"), true).get());
        assertInstanceOf(AppSecurityException.class, ex.getCause());
    }

    /// Verifies that the `createConcept` method in `ConceptService` fails when any of the required fields are null.
    /// This ensures proper validation and handling of null inputs during concept creation.
    /// Test steps include:
    /// 1. Creates and logs an admin user by invoking `createAndLogAdminUser()`.
    /// 2. Attempts to create a new concept using `createConcept` with various null field combinations.
    ///    - Case 1: Null `name` with valid `type` and `value`.
    ///    - Case 2: Null `type` with valid `name` and `value`.
    ///    - Case 3: Null `value` with valid `name` and `type`.
    /// 3. Validates that an `ExecutionException` is thrown in each case, indicating the creation operation fails.
    /// 4. Confirms that the root cause of the exception is a `PersistenceException`, verifying proper error handling.
    /// This test ensures the system enforces null checks and prevents invalid `createConcept` operations.
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testCreateConcept_NullFieldsFail() {
        UserSessionHelpers.createAndLogAdminUser();

        // null name
        ExecutionException ex1 = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept(null, ConceptType.FIXED, new BigDecimal("1.0000")).get());
        assertInstanceOf(PersistenceException.class, ex1.getCause());

        // null type
        ExecutionException ex2 = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept("X", null, new BigDecimal("1.0000")).get());
        assertInstanceOf(PersistenceException.class, ex2.getCause());

        // null value
        ExecutionException ex3 = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept("X", ConceptType.FIXED, null).get());
        assertInstanceOf(PersistenceException.class, ex3.getCause());
    }

    /// Verifies that the `updateConcept` method in `ConceptService` fails when
    /// any of the required fields are null. This test ensures proper validation
    /// and handling of null inputs during concept updates.
    /// Test steps include:
    /// 1. Creating and logging in an admin user by invoking `createAndLogAdminUser`.
    /// 2. Creating a valid concept using `createConcept` with specified parameters.
    /// 3. Attempting to update the created concept using `updateConcept` with various
    ///    null field combinations:
    ///    - Case 1: Null `name` with valid `type`, `value`, and `active` status.
    ///    - Case 2: Null `type` with valid `name`, `value`, and `active` status.
    ///    - Case 3: Null `value` with valid `name`, `type`, and `active` status.
    ///    - Case 4: Null `active` status with valid `name`, `type`, and `value`.
    /// 4. Validating that an `ExecutionException` is thrown in each case, indicating
    ///    a failure of the update operation.
    /// 5. Asserting that the root cause of the exception is a `PersistenceException`,
    ///    verifying proper error handling and validation.
    /// This test ensures the system enforces null checks and prevents invalid
    /// `updateConcept` operations, maintaining data integrity.
    ///
    /// @throws Exception if an unexpected error occurs during the execution of the test.
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testUpdateConcept_NullFieldsFail() throws Exception {
        UserSessionHelpers.createAndLogAdminUser();
        Concept c = conceptService.createConcept("Internet", ConceptType.RATE, new BigDecimal("0.1000")).get();

        // null name
        ExecutionException ex1 = assertThrows(ExecutionException.class,
                () -> conceptService.updateConcept(c.getId(), null, ConceptType.RATE, new BigDecimal("0.0500"), true).get());
        assertInstanceOf(PersistenceException.class, ex1.getCause());

        // null type
        ExecutionException ex2 = assertThrows(ExecutionException.class,
                () -> conceptService.updateConcept(c.getId(), "Internet", null, new BigDecimal("0.0500"), true).get());
        assertInstanceOf(PersistenceException.class, ex2.getCause());

        // null value
        ExecutionException ex3 = assertThrows(ExecutionException.class,
                () -> conceptService.updateConcept(c.getId(), "Internet", ConceptType.RATE, null, true).get());
        assertInstanceOf(PersistenceException.class, ex3.getCause());

        // null active
        ExecutionException ex4 = assertThrows(ExecutionException.class,
                () -> conceptService.updateConcept(c.getId(), "Internet", ConceptType.RATE, new BigDecimal("0.0500"), null).get());
        assertInstanceOf(PersistenceException.class, ex4.getCause());
    }

    /// Verifies that the `createConcept` method in `ConceptService` correctly enforces
    /// constraints during the creation of concepts with certain values.
    /// This test ensures that:
    /// 1. A `FIXED` type concept exceeding a precision of 99.9999 fails to be created.
    /// 2. A `RATE` type concept with a value above 1.0000 is not allowed.
    /// Test steps include:
    /// - An admin user is created and set as the current user by invoking `createAndLogAdminUser()`.
    /// - Attempting to create a `FIXED` type concept with a value of 100.0000, which exceeds
    ///   the allowed maximum. An `ExecutionException` is asserted, and the root cause
    ///   is validated to be a `PersistenceException`.
    /// - Attempting to create a `RATE` type concept with a value of 1.0001, which is beyond
    ///   the permitted threshold. An `ExecutionException` is asserted, and the root cause
    ///   is validated to be a `PersistenceException`.
    /// This test ensures that invalid data is rejected and appropriate constraints are
    /// enforced in the `ConceptService` implementation.
    @Test
    void testCreateConcept_CheckConstraints() {
        UserSessionHelpers.createAndLogAdminUser();

        // FIXED exceeding precision (should be max 99.9999)
        ExecutionException ex1 = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept("FixedTooLarge", ConceptType.FIXED, new BigDecimal("100.0000")).get());
        assertInstanceOf(PersistenceException.class, ex1.getCause());

        // RATE above 1.0000
        ExecutionException ex2 = assertThrows(ExecutionException.class,
                () -> conceptService.createConcept("RateTooHigh", ConceptType.RATE, new BigDecimal("1.0001")).get());
        assertInstanceOf(PersistenceException.class, ex2.getCause());
    }
}
