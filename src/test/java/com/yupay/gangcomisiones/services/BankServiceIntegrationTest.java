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
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.createAndLogAdminUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

/**
 * Integration tests for the {@code BankService} functionality.
 * <br/>
 * These tests verify the behavior of the service in a system-integrated environment
 * with a real database and application context. They ensure correct persistence, data integrity,
 * and user-privilege conditions in various scenarios related to bank creation and updates.
 * <br/>
 * The following core functionalities are tested:
 * <ul>
 *   <li>Creation of a bank with automatic ID assignment and default active status.</li>
 *   <li>Privilege-based access control for bank creation and updates.</li>
 *   <li>Validation of not-null constraints on input fields.</li>
 * </ul>
 *
 * <br/>
 * <b>Test List:</b>
 * <ol>
 *   <li>
 *     <b>testCreateBank_AssignsIdAndActiveTrue:</b>
 *     Verifies that a new bank is persisted with an auto-generated positive ID and has an active
 *     status set to {@code true} by default. Additionally, ensures that the newly created bank
 *     appears in the results of {@code listAllBanks} and {@code listAllActiveBanks}.
 *   </li>
 *   <li>
 *     <b>testCreateBank_UnprivilegedUserFails:</b>
 *     Ensures that attempting to create a bank without admin privileges fails with a
 *     {@code PersistenceServicesException} caused by an {@code AppSecurityException}.
 *   </li>
 *   <li>
 *     <b>testUpdateBank_UnprivilegedUserFails:</b>
 *     Ensures that an unprivileged user attempting to update a bank also fails with the same
 *     exception and cause as above.
 *   </li>
 *   <li>
 *     <b>testCreateBank_NullNameFails:</b>
 *     Validates that attempting to create a bank with a {@code null} name violates a not-null
 *     constraint, resulting in a {@code PersistenceException}.
 *   </li>
 *   <li>
 *     <b>testUpdateBank_NullFieldsFail:</b>
 *     Ensures that updating a bank with {@code null} values for either the name or active flag
 *     violates not-null constraints and throws an appropriate {@code SQLException}.
 *   </li>
 * </ol>
 *
 * <br/>
 * <b>Test Setup:</b>
 * <ul>
 *   <li>Before each test, the environment is cleaned to remove persisted entities and reset the database state.</li>
 *   <li>The {@code BankService} instance is initialized from the application context for use in the tests.</li>
 * </ul>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 5 tests in 1.482s at 2025-09-28 22:24 UTC-5.
 * </div>
 */
class BankServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    /**
     * Sets up the test environment before each test method execution.
     * <br/>
     * This method resets the database state by truncating all relevant tables,
     * ensuring no persisted entities interfere with subsequent tests. It achieves
     * this through the {@link TestPersistedEntities#clean(EntityManagerFactory)} method.
     * <br/>
     * Additionally, it initializes the {@code BankService} instance used in the tests
     * by accessing it through the application context.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        ctx.getUserSession().setCurrentUser(null);
    }


    /**
     * Verifies that a bank is persisted with an auto-generated id and active=true by default.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testCreateBank_AssignsIdAndActiveTrue() throws Exception {
        var bankService = ctx.getBankService();

        runInTransaction(em
                -> createAndLogAdminUser(ctx, em));

        var bank = bankService.createBank("Interbank").get();

        // then
        assertThat(bank.getId())
                .as("Bank id must be assigned by DB sequence")
                .isNotNull()
                .as("Bank id must be positive")
                .isGreaterThan(0);
        assertThat(bank.getActive())
                .as("Bank must be active after creation")
                .isTrue();

        // and it should appear in listAll
        var all = bankService.listAllBanks().get();
        assertThat(all)
                .as("Persisted bank with id %d should be listed in all banks.")
                .anyMatch(b -> b.getId().equals(bank.getId()));

        // and listAllActive
        var active = bankService.listAllActiveBanks().get();
        assertThat(active)
                .as("Persisted bank with id %d should be listed in all active banks.")
                .anyMatch(b -> b.getId().equals(bank.getId()));
    }

    /**
     * Verifies that trying to create a bank when the logged-in user is
     * not an admin fails with a {@link PersistenceServicesException}.
     */
    @Test
    void testCreateBank_UnprivilegedUserFails() {
        runInTransaction(em -> UserSessionHelpers.createAndLogCashierUser(ctx, em));

        var ex = catchException(ctx.getBankService().createBank("One Bank")::get);

        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "has not privileges to perform an operation");
    }

    /**
     * Verifies that trying to update a bank when the logged-in user is
     * not an admin fails with a {@link PersistenceServicesException}.
     */
    @Test
    void testUpdateBank_UnprivilegedUserFails() {
        runInTransaction(em -> UserSessionHelpers.createAndLogCashierUser(ctx, em));

        var ex = catchException(ctx.getBankService()
                .updateBank(1, "One Bank", false)::get);

        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "has not privileges to perform an operation");
    }

    /**
     * Verifies that creating a bank with a null name fails with a {@link PersistenceException}.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testCreateBank_NullNameFails() {
        runInTransaction(em->createAndLogAdminUser(ctx, em));
        var ex = catchException(ctx.getBankService().createBank(null)::get);
        assertExpectedCause(SQLException.class).assertCauseWithMessage(ex, "null");
    }

    /**
     * Verifies that updating a bank with a null name or null active flag fails due to not-null constraints.
     *
     * @throws Exception if an error occurs during the test.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testUpdateBank_NullFieldsFail() throws Exception {
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var bankService = ctx.getBankService();
        var bank = bankService.createBank("BCP").get();

        var assertion = assertExpectedCause(SQLException.class);
        // null name
        var ex1 = catchException(
                () -> bankService.updateBank(bank.getId(), null, true).get());
        assertion.assertCauseWithMessage(ex1, "null");

        // null active
        var ex2 = catchException(
                () -> bankService.updateBank(bank.getId(), "BCP", null).get());
        assertion.assertCauseWithMessage(ex2, "null");
    }
}
