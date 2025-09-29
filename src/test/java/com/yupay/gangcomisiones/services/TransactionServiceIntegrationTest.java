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
import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.Transaction;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.sql.SQLException;

import static com.yupay.gangcomisiones.assertions.AuditLogAssertion.withContext;
import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.model.TestPersistedEntities.persistCashierUser;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.createAndLogAdminUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

/**
 * Integration test class for {@code TransactionService}, validating its behavior alongside related services
 * {@code BankService}, {@code ConceptService}, and {@code UserService} in a PostgreSQL-based environment.
 * <br/>
 * <br/>
 * This test class ensures the correctness of transaction-related operations, focusing on factors such as:
 * <ul>
 *   <li>Data persistence and integrity across services.</li>
 *   <li>Audit log generation for transactional activities.</li>
 *   <li>Enforcement of business rules, such as requiring valid input fields and user authentication.</li>
 * </ul>
 * <br/>
 * Inherits functionality from {@link AbstractPostgreIntegrationTest}.
 * <br/>
 * <br/>
 * Responsibilities of this test class include:
 * <ul>
 *   <li>Setting up and cleaning up the environment for each test.</li>
 *   <li>Testing transaction creation scenarios, such as successful creation, user authentication checks, and input validation.</li>
 *   <li>Validating interactions with associated entities like banks, concepts, and users.</li>
 * </ul>
 * <br/>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 7 tests in 2.366s at 2025-09-29 13:46 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class TransactionServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    private TransactionService transactionService;
    private BankService bankService;
    private ConceptService conceptService;
    private UserService userService;

    /// Sets up the required environment for each test case in the test class.
    /// This method is executed before each test method, ensuring a consistent state.
    /// Responsibilities:
    /// - Cleans up persisted test data in the database by utilizing the `TestPersistedEntities.clean` method.
    /// - Initializes the service instances required for the tests, including:
    ///   - `TransactionService` via the application context.
    ///   - `BankService` via the application context.
    ///   - `ConceptService` via the application context.
    ///   - `UserService` via the application context.
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        transactionService = ctx.getTransactionService();
        bankService = ctx.getBankService();
        conceptService = ctx.getConceptService();
        userService = ctx.getUserService();
    }

    /**
     * Cleans up resources and resets the application state after each test case execution.
     * <br/>
     * This method ensures that the test environment is properly restored to its initial state
     * by resetting dependencies and clearing the user session.
     * <br/>
     * Responsibilities:
     * <ul>
     *   <li>Sets the {@code transactionService} field to {@code null}.</li>
     *   <li>Sets the {@code bankService} field to {@code null}.</li>
     *   <li>Sets the {@code conceptService} field to {@code null}.</li>
     *   <li>Sets the {@code userService} field to {@code null}.</li>
     *   <li>Clears the currently logged-in user in the user session by calling
     *       {@code ctx.getUserSession().setCurrentUser(null)}.</li>
     * </ul>
     * <br/>
     * This method is executed after each test to ensure no residual state affects subsequent tests.
     */
    @AfterEach
    void cleanUp() {
        transactionService = null;
        bankService = null;
        conceptService = null;
        userService = null;
        ctx.getUserSession().setCurrentUser(null);
    }

    /**
     * Tests that a transaction is successfully created and a corresponding audit log is generated.
     * <br/>
     * This test ensures that the full flow of creating a transaction as a valid cashier user is executed successfully,
     * and verifies that appropriate data is persisted and audited.
     * <br/>
     * <p>
     * Test Steps:
     * <ol>
     *   <li>Set up the environment:
     *       <ul>
     *           <li>Create an administrator user and log in to set up prerequisites.</li>
     *           <li>Create and persist a cashier user.</li>
     *       </ul>
     *   </li>
     *   <li>Create the necessary entities:
     *       <ul>
     *           <li>A bank entity using the {@code bankService}.</li>
     *           <li>A concept entity with type, name, and commission value using the {@code conceptService}.</li>
     *       </ul>
     *   </li>
     *   <li>Log in as the created cashier user.</li>
     *   <li>Prepare a {@code CreateTransactionRequest} with the following valid transaction data:
     *       <ul>
     *           <li>Transaction amount.</li>
     *           <li>Bank ID and Concept ID for the transaction.</li>
     *           <li>Concept type and commission value.</li>
     *           <li>Cashier user ID.</li>
     *       </ul>
     *   </li>
     *   <li>Invoke the {@code createTransaction} method in {@code transactionService} to create the transaction.</li>
     *   <li>Verify the results:
     *       <ul>
     *           <li>Ensure the transaction ID is assigned (not null and positive).</li>
     *           <li>Validate that the concept name in the transaction matches the original concept's name.</li>
     *           <li>Confirm the existence of an audit log tied to the created transaction ID.</li>
     *       </ul>
     *   </li>
     * </ol>
     * <br/>
     * <p>
     * Expected Outcome:
     * <ul>
     *   <li>A transaction is successfully created with the specified details.</li>
     *   <li>Audit log entry is generated with the correct transaction ID.</li>
     * </ul>
     *
     * @throws Exception If any unexpected errors occur during the test execution.
     */
    @Test
    void testCreateTransaction_SuccessAndAudited() throws Exception {
        // given: create prerequisites as admin, then login as cashier to create the transaction
        runInTransaction(em -> {
            createAndLogAdminUser(ctx, em);
            persistCashierUser(em);
        });
        var bank = bankService.createBank("Interbank").get();
        var concept = conceptService
                .createConcept("Telephone", ConceptType.FIXED, new BigDecimal("1.0000")).get();

        var cashier = TestPersistedEntities.USER.get(UserRole.CASHIER);
        ctx.getUserSession().setCurrentUser(cashier);

        var request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("10.00"))
                .bankId(bank.getId())
                .conceptId(concept.getId())
                .conceptCommissionValue(concept.getValue())
                .cashierId(cashier.getId())
                .conceptType(concept.getType())
                .build();

        // when
        var tx = transactionService.createTransaction(request).get();

        // then: id assigned
        assertThat(tx.getId())
                .as("A positive Transaction ID must be assigned by DB sequence.")
                .isNotNull()
                .isPositive();
        assertThat(tx.getConceptName())
                .as("Concept name snapshot should be equals to concept name.")
                .isEqualTo(concept.getName());

        // and an audit log with entityId must exist
        withContext(ctx).assertHasLog(tx, Transaction::getId);
    }

    /**
     * Ensures that transaction creation fails when no user is logged in.
     * <br/>
     * This test verifies that the system enforces the requirement of having a valid logged-in user
     * to perform transaction-related operations, such as creating a transaction.
     * <br/>
     * Test Steps:
     * <ol>
     *   <li>Set up prerequisite data, including:
     *       <ul>
     *           <li>Create and log in an administrator user to set up required entities.</li>
     *           <li>Create entities such as a bank, concept, and cashier user.</li>
     *           <li>Log out any currently logged-in user to simulate a non-authenticated scenario.</li>
     *       </ul>
     *   </li>
     *   <li>Prepare a {@code CreateTransactionRequest} containing valid transaction details:
     *       <ul>
     *           <li>Bank information.</li>
     *           <li>Concept details, including commission value and type.</li>
     *           <li>Cashier user ID and transaction amount.</li>
     *       </ul>
     *   </li>
     *   <li>Attempt to execute the transaction creation using the service.</li>
     *   <li>Capture the exception thrown by the service and assert:
     *       <ul>
     *           <li>The exception is an {@link SQLException}.</li>
     *           <li>The cause of the exception indicates a missing user session or null actor.</li>
     *       </ul>
     *   </li>
     * </ol>
     * <br/>
     * Expected Outcome:
     * <ul>
     *   <li>Transaction creation fails when no user is logged in.</li>
     *   <li>An appropriate exception is raised, matching the expected error type and message.</li>
     * </ul>
     *
     * @throws Exception If an error occurs during the execution of the test.
     */
    @Test
    void testCreateTransaction_FailsWhenNoUserLogged() throws Exception {
        // given: create prerequisites but ensure no user is logged at the moment of creation
        runInTransaction(em -> {
            createAndLogAdminUser(ctx, em);
            TestPersistedEntities.persistCashierUser(em);
        });
        var bank = bankService.createBank("ACME").get();
        var concept = conceptService.createConcept("Internet", ConceptType.RATE, new BigDecimal("0.1000")).get();
        var cashier = TestPersistedEntities.USER.get(UserRole.CASHIER);
        // logout so that AuditLogger has no actor
        ctx.getUserSession().logout();

        var request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("50.00"))
                .bankId(bank.getId())
                .conceptId(concept.getId())
                .conceptCommissionValue(concept.getValue())
                .cashierId(cashier.getId())
                .conceptType(concept.getType())
                .build();

        // when/then

        var ex = catchException(() -> transactionService.createTransaction(request).get());
        assertExpectedCause(SQLException.class)
                .assertCauseWithMessage(ex, "null");
    }

    /**
     * Validates that transaction creation fails when one of the required fields in the request is null.
     * <br/>
     * This test ensures that:
     * <ul>
     *   <li>Each required field in the {@code CreateTransactionRequest} is tested for {@code null} values.</li>
     *   <li>The service correctly throws an exception when a required field is null, preventing invalid transactions.</li>
     * </ul>
     * <br/>
     * Test Steps:
     * <ol>
     *   <li>Create and log in an administrator user to set up entities (bank, concept, and cashier).</li>
     *   <li>Log in as the cashier and attempt to create a transaction with one required field intentionally set to {@code null}.</li>
     *   <li>Verify that the service throws a {@link SQLException} with an error message indicating a null value.</li>
     * </ol>
     * <br/>
     * Expected Outcome:
     * <ul>
     *   <li>The service fails the transaction creation when any required field is null.</li>
     *   <li>An appropriate exception is raised with the correct error message.</li>
     * </ul>
     *
     * @param nullField The field in the {@code CreateTransactionRequest} that is set to null for this test iteration.
     *                  <ul>
     *                      <li>Corresponds to the {@link NullField} enumeration values:</li>
     *                      <li>{@code BANK}: {@code bankId} is null.</li>
     *                      <li>{@code CONCEPT}: {@code conceptId} is null.</li>
     *                      <li>{@code CASHIER}: {@code cashierId} is null.</li>
     *                      <li>{@code AMOUNT}: {@code amount} is null.</li>
     *                      <li>{@code COMMISSION}: {@code conceptCommissionValue} is null.</li>
     *                  </ul>
     *                  The parameter is supplied automatically by the {@link ParameterizedTest} annotation.
     * @throws Exception If an unexpected error occurs during the test execution.
     */
    @ParameterizedTest
    @EnumSource(value = NullField.class)
    void testCreateTransaction_NullFieldsFail(@NotNull NullField nullField) throws Exception {
        // given: valid prerequisites and logged-in cashier to avoid failing due to missing audit user
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var bank = bankService.createBank("Nacional").get();
        var concept = conceptService.createConcept("Water", ConceptType.FIXED, new BigDecimal("2.0000")).get();
        var cashier = userService.createUser("cashier.nulls", UserRole.CASHIER, "password").get();
        ctx.getUserSession().setCurrentUser(cashier);

        var builder = CreateTransactionRequest.builder()
                .amount(new BigDecimal("15.00"))
                .bankId(bank.getId())
                .conceptId(concept.getId())
                .conceptCommissionValue(concept.getValue())
                .cashierId(cashier.getId())
                .conceptType(concept.getType());
        var rq = (switch (nullField) {
            case BANK -> builder.bankId(0);
            case CONCEPT -> builder.conceptId(0);
            case CASHIER -> builder.cashierId(0);
            case AMOUNT -> builder.amount(null);
            case COMMISSION -> builder.conceptCommissionValue(null);
        }).build();

        var ex = catchException(() -> transactionService.createTransaction(rq).get());
        assertExpectedCause(PersistenceServicesException.class)
                .assertCauseWithMessage(ex, "Request");
    }

    public enum NullField {
        BANK,
        CONCEPT,
        CASHIER,
        AMOUNT,
        COMMISSION
    }
}
