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
import com.yupay.gangcomisiones.assertions.AuditLogAssertion;
import com.yupay.gangcomisiones.exceptions.AppSecurityException;
import com.yupay.gangcomisiones.model.AuditLog;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.assertions.ServiceAssertions.assertListed;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.createAndLogAdminUser;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.createAndLogCashierUser;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration test suite for validating the functionality, security, and data integrity
 * of the {@code ConceptService} implementation within the application.
 * <br/>
 * This test class primarily ensures that the `ConceptService` operations perform as expected
 * in a real PostgreSQL database environment, including enforcing role-based access controls,
 * data constraints, and input validation.
 * <br/>
 * <b>Features tested:</b>
 * <ul>
 *     <li>Validation of entity creation and default field values.</li>
 *     <li>Access control for operations based on user roles (e.g., admin vs cashier).</li>
 *     <li>Validation and rejection of invalid inputs (e.g., null fields).</li>
 *     <li>Enforcement of constraints for specific concept types and value ranges.</li>
 * </ul>
 * <br/>
 * <b>Super Class:</b>
 * <ul>
 *     <li>{@code com.yupay.gangcomisiones.AbstractPostgreIntegrationTest} - Provides base setup and teardown methods for
 *     PostgreSQL integration tests, ensuring a clean state for database operations during test execution.</li>
 * </ul>
 * <br/>
 * <b>Test Methods:</b>
 * <ol>
 *     <li>
 *         {@code setUp()}:
 *         <ul>
 *             <li>Cleans and truncates all database tables to ensure a consistent and clean test environment.</li>
 *             <li>Initializes the {@code ConceptService} instance for use within test cases.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testCreateConcept_AssignsIdActiveTrueAndAudited()}:
 *         <ul>
 *             <li>Ensures the `createConcept` method correctly assigns a unique positive ID, sets the `active` field to true,
 *                 and generates an audit log entry.</li>
 *             <li>Validates these behaviors for new concept creation using an admin user.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testCreateConcept_UnprivilegedUserFails()}:
 *         <ul>
 *             <li>Confirms unauthorized users (e.g., cashier role) cannot create a new concept using the `createConcept` method.</li>
 *             <li>Asserts that an `ExecutionException` is thrown with a root cause of `PersistenceServicesException`
 *                 in such cases.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testUpdateConcept_UnprivilegedUserFails()}:
 *         <ul>
 *             <li>Verifies that users with insufficient privileges (e.g., cashier role) cannot update an existing concept
 *                 using the `updateConcept` method.</li>
 *             <li>Ensures proper exception handling and root cause tracking for unauthorized update attempts.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testCreateConcept_NullFieldsFail()}:
 *         <ul>
 *             <li>Tests that the `createConcept` method fails with null required fields (e.g., `name`, `type`, `value`).</li>
 *             <li>Ensures an `ExecutionException` is thrown and validates the root cause as `PersistenceException`
 *                 for each invalid input case.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testUpdateConcept_NullFieldsFail()}:
 *         <ul>
 *             <li>Ensures attempts to update a concept with null fields (e.g., `name`, `type`, `value`, `active`)
 *                 are rejected.</li>
 *             <li>Asserts that an `ExecutionException` is raised with proper validation of the underlying
 *                 `PersistenceException` cause.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@code testCreateConcept_CheckConstraints()}:
 *         <ul>
 *             <li>Ensures the `createConcept` method enforces constraints for certain concept types.</li>
 *             <li>Verifies that invalid data for `FIXED` and `RATE` types (e.g., exceeding precision/scale or thresholds)
 *                 is rejected with meaningful exceptions.</li>
 *         </ul>
 *     </li>
 * </ol>
 * <br/>
 * <b>Note:</b><br/>
 * All test methods depend on specific user roles (e.g., admin or cashier) being correctly set up to simulate
 * access control scenarios. Ensure preconditions are met for accurate test results.
 * <br/>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 6 tests in 2.118s at 2025-09-29 00:15 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class ConceptServiceIntegrationTest extends AbstractPostgreIntegrationTest {

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
        ctx.getUserSession().setCurrentUser(null);
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
        runInTransaction(em -> createAndLogAdminUser(ctx, em));

        // when
        var conceptService = ctx.getConceptService();
        var c = conceptService
                .createConcept("Telephone Bill", ConceptType.FIXED, new BigDecimal("10.0000")).get();

        // then
        assertThat(c.getId())
                .as("Concept id must be assigned by DB sequence")
                .isNotNull()
                .as("Concept id must be positive")
                .isPositive();
        assertThat(c.getActive())
                .as("Concept must be active after creation")
                .isTrue();

        // and must be listed in all and active
        assertListed(conceptService::listAllConcepts, c, "listAll");
        assertListed(conceptService::listAllActiveConcepts, c, "listAllActive");

        // and an audit log with entityId must exist
        AuditLogAssertion.withContext(ctx).assertHasLog(c, Concept::getId);
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var logs = em.createQuery("SELECT a FROM AuditLog a WHERE a.entityId = :id", AuditLog.class)
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
        runInTransaction(em -> createAndLogCashierUser(ctx, em));
        var ex = catchThrowable(
                ctx.getConceptService()
                        .createConcept("Water Service", ConceptType.FIXED, new BigDecimal("2.0000"))::get);
        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "has not privileges to perform an operation");
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
        runInTransaction(em -> createAndLogCashierUser(ctx, em));
        var ex = catchThrowable(
                ctx.getConceptService()
                        .updateConcept(1L,
                                "Water Service",
                                ConceptType.FIXED,
                                new BigDecimal("2.0000"),
                                false)::get);
        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "has not privileges to perform an operation");
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
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var conceptService = ctx.getConceptService();

        // null name
        var ex1 = catchThrowable(
                conceptService.createConcept(null, ConceptType.FIXED, new BigDecimal("1.0000"))::get);

        // null type
        var ex2 = catchThrowable(
                conceptService.createConcept("X", null, new BigDecimal("1.0000"))::get);

        // null value
        var ex3 = catchThrowable(
                conceptService.createConcept("X", ConceptType.FIXED, null)::get);

        assertExpectedCause(SQLException.class).assertCausesWithMessage("null", ex1, ex2, ex3);
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
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var conceptService = ctx.getConceptService();
        var c = conceptService
                .createConcept("Internet", ConceptType.RATE, new BigDecimal("0.1000")).get();

        // null name
        var ex1 = catchException(
                conceptService
                        .updateConcept(c.getId(),
                                null,
                                ConceptType.RATE,
                                new BigDecimal("0.0500"),
                                true)
                        ::get);

        // null type
        var ex2 = catchException(
                conceptService
                        .updateConcept(c.getId(),
                                "Internet",
                                null,
                                new BigDecimal("0.0500"),
                                true)
                        ::get);

        // null value
        var ex3 = catchException(
                conceptService
                        .updateConcept(c.getId(), "Internet", ConceptType.RATE, null, true)
                        ::get);

        // null active
        var ex4 = catchException(
                conceptService
                        .updateConcept(c.getId(),
                                "Internet",
                                ConceptType.RATE,
                                new BigDecimal("0.0500"),
                                null)
                        ::get);

        assertExpectedCause(SQLException.class).assertCausesWithMessage("null", ex1, ex2, ex3, ex4);
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
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var conceptService = ctx.getConceptService();

        // FIXED exceeding precision (should be max 99.9999)
        var ex1 = catchException(
                conceptService
                        .createConcept("FixedTooLarge", ConceptType.FIXED, new BigDecimal("100.0000"))::get);
        assertExpectedCause(SQLException.class).assertCauseWithMessage(ex1, "10^2");

        // RATE above 1.0000
        var ex2 = catchException(
                conceptService.createConcept("RateTooHigh", ConceptType.RATE, new BigDecimal("1.0001"))::get);
        assertExpectedCause(SQLException.class).assertCauseWithMessage(ex2, "chk_concept_value_valid");
    }
}
