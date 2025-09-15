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

package com.yupay.gangcomisiones.usecase.transaction.create;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.Transaction;
import com.yupay.gangcomisiones.model.TransactionStatus;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import com.yupay.gangcomisiones.usecase.AuditLogChecker;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link CreateTransactionController}, covering the "Create Transaction" flow
 * under success, cancellation, and error scenarios.
 * <br/>
 * This test class verifies:
 * <ul>
 *   <li>Successful creation when a logged-in user provides valid transaction input.</li>
 *   <li>Cancellation when the user dismisses the transaction form.</li>
 *   <li>Error handling when no user is authenticated.</li>
 *   <li>Error propagation from the persistence layer.</li>
 * </ul>
 * <div style="border: 1px solid black; padding: 1px;">
 *   <b>Execution note:</b> dvidal@infoyupay.com passed 4 tests in 1.972s at 2025-09-15 00:05 UTC-5.
 * </div>
 *
 * @author InfoYupay
 * @version 1.0
 */
class CreateTransactionControllerTest extends AbstractPostgreIntegrationTest {
    static final AtomicReference<CreateTransactionRequest> requestRef = new AtomicReference<>(null);
    CreateTransactionView view;

    /**
     * Prepares the test environment before each test execution.
     * <br/>
     * This method performs the following steps:
     * <ul>
     *     <li>Invokes {@link TestPersistedEntities#clean(EntityManagerFactory)} to reset the state of persisted entities.</li>
     *     <li>Uses the {@code EntityManagerFactory} retrieved from the application context ({@code ctx})
     *         to truncate all relevant database tables and clear any in-memory test entities.</li>
     * </ul>
     * <br/>
     * This setup ensures a consistent and isolated testing environment for each test case.
     */
    @BeforeEach
    void prepareTest() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Cleans up the test environment after each test execution.
     * <br/>
     * This method performs the following steps:
     * <ul>
     *     <li>Sets the {@code view} instance to {@code null} to release memory and reset the state.</li>
     *     <li>Checks if the {@link CreateTransactionView} is registered in the {@code viewRegistry}:
     *     <ol>
     *         <li>If registered, it unregisters the {@link CreateTransactionView} using
     *         {@code viewRegistry.unregister()} to ensure no references remain and avoid test interference. </li>
     *     </ol>
     *     </li>
     * </ul>
     * <br/>
     * Ensures the test environment is left in a clean state, avoiding side effects on subsequent test executions.
     */
    @AfterEach
    void cleanUp() {
        view = null;
        if (viewRegistry.isRegistered(CreateTransactionView.class)) {
            viewRegistry.unregister(CreateTransactionView.class);
        }
    }

    /**
     * Ensures that no audit log entries exist in the database.
     * <br/><br/>
     * This method performs the following steps:
     * <ul>
     *     <li>Invokes {@link AuditLogChecker#checkAnyAuditLogExists(EntityManagerFactory)} using the
     *     {@code EntityManagerFactory} obtained from the application context ({@code ctx}).</li>
     *     <li>Asserts that {@code checkAnyAuditLogExists} returns {@code false}, indicating the database is free of audit log entries.</li>
     * </ul>
     * <br/>
     * If any audit log entry is found, the assertion will fail with the message "No audit log should exist".
     */
    private void assertNoAuditLog() {
        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory()))
                .as("No audit log should exist")
                .isFalse();
    }

    /*==============*
     * Happy paths. *
     *==============*/

    /**
     * Tests the functionality of creating a transaction in the use case under the condition that:
     * <ul>
     *     <li>A user is logged in.</li>
     *     <li>The transaction request provided is valid.</li>
     * </ul>
     * <br/>
     * This test validates that:
     * <ol>
     *     <li>The "create transaction" form is displayed using the {@link FormMode#CREATE} mode.</li>
     *     <li>A new transaction is successfully created with the appropriate field values that match the input request.</li>
     *     <li>The resulting transaction has:
     *         <ul>
     *             <li>A registered {@link TransactionStatus}.</li>
     *             <li>A valid cashier, bank, and commission.</li>
     *         </ul>
     *     </li>
     *     <li>No error message is displayed to the user.</li>
     *     <li>A success notification is shown confirming the creation of the transaction.</li>
     *     <li>An audit log entry is created for the transaction in the database.</li>
     * </ol>
     * <br/>
     * Verifications in this test include:
     * <ul>
     *     <li>Explicit calls to the test view to ensure minimal UI feedback requirements are met.</li>
     *     <li>Assertions that no error messages are triggered when the operation is successful.</li>
     *     <li>Assertions to confirm the transaction's data integrity in the response object.</li>
     *     <li>Validation of the existence of an audit log entry for the created transaction.</li>
     * </ul>
     */
    @Test
    void givenLoggedInUserAndValidRequest_whenStartUseCase_thenTransactionCreated() {
        // Arrange
        var transaction = TestPersistedEntities
                .performInTransaction(ctx, TestPersistedEntities::buildValidTansaction);

        ctx.getUserSession().setCurrentUser(TestPersistedEntities.USER.get());

        var sampleRequest = CreateTransactionRequest.builder()
                .amount(transaction.getAmount())
                .bankId(transaction.getBank().getId())
                .cashierId(transaction.getCashier().getId())
                .conceptCommissionValue(transaction.getConcept().getValue())
                .conceptId(transaction.getConcept().getId())
                .conceptType(transaction.getConcept().getType())
                .build();
        requestRef.set(sampleRequest);

        view = TestViews.transactionView(FormMode.CREATE, requestRef);
        viewRegistry.registerInstance(CreateTransactionView.class, view);

        var controller = new CreateTransactionController(ctx);

        // Act
        var result = controller.startUseCase().join();

        // Assert
        verify(view).showUserForm(FormMode.CREATE);
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        //noinspection DataFlowIssue
        assertThat(result.value()).isNotNull()
                .extracting(Transaction::getAmount, Transaction::getStatus, Transaction::getCashier,
                        Transaction::getBank, Transaction::getCommission)
                .containsExactly(sampleRequest.getAmount(), TransactionStatus.REGISTERED, transaction.getCashier(),
                        transaction.getBank(), transaction.getCommission());

        verify(view, never()).showError(anyString());
        verify(view, atLeastOnce()).showSuccess(contains("Se creó la transacción Nro."));

        assertThat(AuditLogChecker.checkAuditLogExists(
                result.value().getId(), transaction.getCashier(), ctx.getEntityManagerFactory()))
                .isTrue();
    }

    /**
     * Tests the behavior of the "start use case" process when a logged-in user cancels the operation.
     * <br/><br/>
     * This test performs the following steps:
     * <ol>
     *     <li>Sets up a logged-in user session by persisting and assigning a cashier user.</li>
     *     <li>Configures the view to use the {@link FormMode#CREATE} mode for initiating the transaction.</li>
     *     <li>Registers the view instance {@link CreateTransactionView} in the view registry.</li>
     *     <li>Invokes the {@code startUseCase} method on the controller to simulate the use case logic.</li>
     * </ol>
     * <br/>
     * The test validates the following outcomes:
     * <ul>
     *     <li>The {@code startUseCase} method returns a {@link UseCaseResultType#CANCEL} result.</li>
     *     <li>The response value from {@code startUseCase} is {@code null}, indicating no transaction was created.</li>
     *     <li>The "create transaction" form is displayed as expected in {@link FormMode#CREATE} mode.</li>
     *     <li>No error messages are shown to the user.</li>
     *     <li>No success messages are displayed, as the operation was canceled.</li>
     *     <li>No audit log entries are created in the database.</li>
     * </ul>
     * <br/>
     * Ensures that the system handles cancellation gracefully without any unintended side effects.
     */
    @Test
    void givenLoggedInUserAndCancel_whenStartUseCase_thenCancelled() {
        // Arrange
        var cashier = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);
        requestRef.set(null);

        view = TestViews.transactionView(FormMode.CREATE, requestRef);
        viewRegistry.registerInstance(CreateTransactionView.class, view);

        var controller = new CreateTransactionController(ctx);

        // Act
        var result = controller.startUseCase().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();

        verify(view).showUserForm(FormMode.CREATE);
        verify(view, never()).showError(anyString());
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /*===============*
     * Unhappy paths *
     *===============*/

    /**
     * Tests the behavior of the "start use case" process when no user is logged in.
     * <br/>
     * This test validates the system's response to an unauthenticated user attempting
     * to initiate a transaction creation process.
     * <br/><br/>
     * The following steps are performed:
     * <ol>
     *     <li>Sets up the test environment by ensuring the current user is {@code null}
     *         in the session context, simulating an unauthenticated state.</li>
     *     <li>Prepares a sample {@link CreateTransactionRequest} with basic valid data,
     *         to simulate a transaction creation attempt.</li>
     *     <li>Registers a test view instance for {@link CreateTransactionView} in the
     *         view registry to monitor interactions.</li>
     *     <li>Invokes the {@code startUseCase} method on the {@link CreateTransactionController}
     *         to simulate the use case process.</li>
     * </ol>
     * <br/>
     * The test verifies the following:
     * <ul>
     *     <li>The {@code startUseCase} method returns a
     *         {@link UseCaseResultType#ERROR} result, indicating that the operation
     *         was blocked.</li>
     *     <li>The response value from {@code startUseCase} is {@code null} to indicate
     *         no meaningful operation result due to the lack of authentication.</li>
     *     <li>No user form is shown to the unauthenticated user.</li>
     *     <li>An appropriate error message is displayed to notify the user that they
     *         need to log in to proceed, validated by verifying the error message content.</li>
     *     <li>No success message is displayed, as the operation fails early.</li>
     *     <li>No audit log entries are created in the database, ensuring no logged
     *         information for an unauthorized attempt.</li>
     * </ul>
     * <br/>
     * This test ensures that unauthorized access is blocked and provides appropriate
     * feedback to the user without unintended side effects.
     */
    @Test
    void givenNoUser_whenStartUseCase_thenErrorAndNoFormShown() {
        // Arrange
        ctx.getUserSession().setCurrentUser(null);

        var sampleRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.TEN)
                .build();
        requestRef.set(sampleRequest);

        view = TestViews.transactionView(FormMode.CREATE, requestRef);
        viewRegistry.registerInstance(CreateTransactionView.class, view);

        var controller = new CreateTransactionController(ctx);

        // Act
        var result = controller.startUseCase().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view, never()).showUserForm(any());
        verify(view).showError(contains("debes iniciar sesión"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Tests the behavior of the "start use case" process when a persistence error occurs.
     * <br/>
     * <br/>
     * This test aims to validate the system's handling of persistence-related failures during
     * the transaction creation process. It ensures that appropriate errors are shown to the user
     * and that no unintended actions (e.g., success messages or audit logs) are triggered.
     * <br/>
     * <br/>
     * The test performs the following steps:
     * <ol>
     *     <li>Sets up a logged-in user session by creating and assigning a cashier user using
     *         {@link TestPersistedEntities#performInTransaction(AppContext, Function)}.</li>
     *     <li>Prepares an invalid {@link CreateTransactionRequest} with a negative amount
     *         to trigger a persistence layer error.</li>
     *     <li>Initializes the transaction creation view with {@link FormMode#CREATE} mode.</li>
     *     <li>Registers the view instance {@link CreateTransactionView} in the view registry.</li>
     *     <li>Invokes the {@code startUseCase} method on the {@link CreateTransactionController}
     *         to execute the use case.</li>
     * </ol>
     * <br/>
     * The test validates the following outcomes:
     * <ul>
     *     <li>The {@code startUseCase} method returns a {@link UseCaseResultType#ERROR} result.</li>
     *     <li>The resulting value from the {@code startUseCase} is {@code null}, indicating
     *         no successful transaction creation.</li>
     *     <li>The "create transaction" form is displayed in {@link FormMode#CREATE} mode.</li>
     *     <li>An appropriate error message is displayed to notify the user of the persistence error.</li>
     *     <li>No success message is shown to the user since the operation failed.</li>
     *     <li>No audit log entries are created in the database, ensuring no invalid persistence
     *         data is logged.</li>
     * </ul>
     * <br/>
     * This test ensures the system correctly handles persistence errors by notifying the user
     * and refraining from recording invalid entries or showing unintended success messages.
     */
    @Test
    void givenPersistenceError_whenStartUseCase_thenErrorShown() {
        // Arrange
        var cashier = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);

        var invalidRequest = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(-50)) // provoca error en capa de persistencia
                .build();
        requestRef.set(invalidRequest);

        view = TestViews.transactionView(FormMode.CREATE, requestRef);
        viewRegistry.registerInstance(CreateTransactionView.class, view);

        var controller = new CreateTransactionController(ctx);

        // Act
        var result = controller.startUseCase().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view).showUserForm(FormMode.CREATE);
        verify(view).showError(contains("Error al momento de registrar la transacción"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }
}
