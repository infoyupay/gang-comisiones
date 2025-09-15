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

package com.yupay.gangcomisiones.usecase.transaction.reversal.request;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.services.ReversalRequestService;
import com.yupay.gangcomisiones.services.TransactionService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.AuditLogChecker;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Integration/orchestration tests for {@link RequestReversionController} covering the
 * "Request Reversion" use case. These tests validate controller responsibilities:
 * <ul>
 *   <li>User authentication and privilege checks (minimum CASHIER) against DB using {@link UserService}.</li>
 *   <li>Prompting the user for a textual reason via {@link RequestReversionView}.</li>
 *   <li>Delegating persistence effects to {@link ReversalRequestService} without touching state or audit directly.</li>
 *   <li>Mapping service exceptions to Spanish user messages.</li>
 * </ul>
 * <br/>
 * <div style="border: solid black 1px; padding: 2px">
 *     <strong>Tested by:</strong> dvidal@infoyupay.com, passed 7 tests in 2.271s at 2025-09-15 12:55 UTC-5
 * </div>
 *
 * @author InfoYupay
 * @version 1.0
 */
class RequestReversionControllerTest extends AbstractPostgreIntegrationTest {

    private RequestReversionView view;

    private UserService userService;
    private TransactionService transactionService;
    private ReversalRequestService reversalRequestService;

    /**
     * Prepares the test environment and initializes dependencies before each test execution.
     * <br/>
     * This setup method ensures that the following operations are performed:
     * <ul>
     *   <li>Clears persisted entities and resets all database tables using the {@code TestPersistedEntities.clean} method.</li>
     *   <li>Initializes the required services such as:
     *     <ul>
     *       <li>{@code userService}</li>
     *       <li>{@code transactionService}</li>
     *       <li>{@code reversalRequestService}</li>
     *     </ul>
     *   </li>
     *   <li>Creates a mocked instance of the {@code RequestReversionView} class.</li>
     *   <li>Registers the mocked view instance in the view registry to support test interactions.</li>
     *   <li>Stubs message presentation for the mocked view using the {@code TestViews.stubMessagePresenter} method.</li>
     * </ul>
     * <br/>
     * This ensures a consistent and isolated state for each test execution.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        userService = ctx.getUserService();
        transactionService = ctx.getTransactionService();
        reversalRequestService = ctx.getReversalRequestService();

        view = mock(RequestReversionView.class);
        TestViews.stubMessagePresenter(view);
        viewRegistry.registerInstance(RequestReversionView.class, view);
    }

    /**
     * Cleans up or resets the test environment after the execution of each test.
     * <br/>
     * This method performs the following operations:
     * <ul>
     *     <li>Checks if the {@code RequestReversionView} is registered in the view registry. If it is registered,
     *         unregisters it to ensure no residual state remains for subsequent tests.</li>
     *     <li>Sets the {@code view} reference to {@code null}, ensuring it is not retained beyond the current test's lifecycle.</li>
     * </ul>
     * <br/>
     * This cleanup process ensures that no unexpected interactions or state from a previous test affect the execution
     * of the next test.
     */
    @AfterEach
    void tearDown() {
        if (viewRegistry.isRegistered(RequestReversionView.class)) {
            viewRegistry.unregister(RequestReversionView.class);
        }
        view = null;
    }


    /*==================*
     * Error scenarios. *
     *==================*/

    /**
     * Validates the behavior of the system when attempting to start a use case without an authenticated user.
     * <br/>
     * This test verifies the following:
     * <ul>
     *   <li>If no user is logged in (i.e., the current user is set to {@code null}), the use case fails with an error.</li>
     *   <li>The result of the operation is of type {@code ERROR}.</li>
     *   <li>No forms, such as a reason input dialog, are displayed to the user.</li>
     *   <li>An error message prompting the user to log in is shown.</li>
     *   <li>No success message is displayed.</li>
     * </ul>
     * <br/>
     * Test Steps:
     * <ol>
     *   <li>Set the current user in the session to {@code null} to simulate no authenticated user.</li>
     *   <li>Initialize the {@code RequestReversionController} instance.</li>
     *   <li>Invoke the {@code startUseCase} method with a sample request ID.</li>
     *   <li>Verify the result and behavior of the method:
     *       <ul>
     *         <li>Result type is {@code ERROR}.</li>
     *         <li>Result value is {@code null}.</li>
     *         <li>No reason dialog is displayed.</li>
     *         <li>Appropriate error message indicating the need to log in is shown.</li>
     *         <li>No success message is presented.</li>
     *       </ul>
     *   </li>
     * </ol>
     */
    @Test
    void givenNoUser_whenStartUseCase_thenErrorAndNoFormShown() {
        // given
        ctx.getUserSession().setCurrentUser(null);
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(123L).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view, never()).showReasonDialog();
        verify(view).showError(contains("iniciar sesión"));
        verify(view, never()).showSuccess(anyString());
    }

    /**
     * Validates the behavior of the system when a cashier user cancels the operation during the use case execution.
     * <br/>
     * This test ensures that the correct flow and behavior occur when the user opts to cancel the process.
     * <br/><br/>
     * Test Steps:
     * <ol>
     *   <li>Set up a cashier user in the current session.</li>
     *   <li>Simulate the cancellation by returning an empty {@code Optional} from the reason dialog.</li>
     *   <li>Initialize a {@code RequestReversionController} instance with the required context.</li>
     *   <li>Invoke the {@code startUseCase} method with a transaction ID and await the result.</li>
     * </ol>
     * <br/>
     * Expected Behavior:
     * <ul>
     *   <li>The use case result is of type {@code CANCEL}.</li>
     *   <li>The result value is {@code null} because no further actions are performed on cancellation.</li>
     *   <li>The reason dialog is displayed at least once to prompt the user for input.</li>
     *   <li>An informational message indicating the operation was canceled is shown.</li>
     *   <li>No error messages are displayed during this process.</li>
     * </ul>
     * <br/>
     * This ensures that the system properly handles cancellation scenarios initiated by the user and provides appropriate feedback.
     *
     */
    @Test
    void givenCashierUserAndCancel_whenStartUseCase_thenCancelled() {
        // given
        User cashier = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);
        when(view.showReasonDialog()).thenReturn(Optional.empty());
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(999L).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();
        verify(view).showReasonDialog();
        verify(view, atLeastOnce()).showInfo(contains("Operación cancelada"));
        verify(view, never()).showError(anyString());
    }

    /**
     * Validates the system's behavior when a cashier user provides a blank or whitespace-only reason
     * during the execution of a use case.
     * <br/><br/>
     * Test Steps:
     * <ol>
     *   <li>Set up a cashier user in the current session.</li>
     *   <li>Simulate a blank or whitespace-only input by returning a blank value from the reason dialog.</li>
     *   <li>Initialize a {@code RequestReversionController} instance with the required context.</li>
     *   <li>Invoke the {@code startUseCase} method with a transaction ID and await the result.</li>
     * </ol>
     * <br/>
     * Expected Behavior:
     * <ul>
     *   <li>The use case result is of type {@code CANCEL}.</li>
     *   <li>The result value is {@code null} since no further actions are performed with a blank reason.</li>
     *   <li>The reason dialog is displayed at least once to prompt the user for input.</li>
     *   <li>An informational message indicating the operation was canceled is shown.</li>
     *   <li>No error messages are displayed during the process.</li>
     * </ul>
     * <br/>
     * This ensures that the system properly handles scenarios where users attempt to proceed with blank input
     * and provides clear feedback about the cancellation of the operation.
     *
     */
    @Test
    void givenCashierUserAndBlankReason_whenStartUseCase_thenCancelled() {
        // given
        User cashier = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);
        when(view.showReasonDialog()).thenReturn(Optional.of("   \t   "));
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(1L).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();
        verify(view).showReasonDialog();
        verify(view, atLeastOnce()).showInfo(contains("Operación cancelada"));
        verify(view, never()).showError(anyString());
    }

    /**
     * Validates the behavior when a cashier user attempts to start a use case with a transaction
     * belonging to another user.
     * <br/><br/>
     * This test ensures that the correct error is shown, and the transaction status remains unchanged.
     * <br/><br/>
     * Test Steps:
     * <ol>
     *   <li>Persist a {@code User} representing a cashier (owner B) in the database.</li>
     *   <li>Create a transaction associated with a different user (owner A).</li>
     *   <li>Set the cashier user (owner B) as the current user in the session.</li>
     *   <li>Simulate a request for transaction reversal by providing a reason via the UI dialog.</li>
     *   <li>Initialize a {@code RequestReversionController} instance with the current application context.</li>
     *   <li>Invoke the {@code startUseCase} method with the transaction ID and await the result.</li>
     * </ol>
     * <br/>
     * Expected Behavior:
     * <ul>
     *   <li>The use case result is of type {@code ERROR} due to a transaction ownership conflict.</li>
     *   <li>The reason dialog is displayed to the user during the test.</li>
     *   <li>An appropriate error message indicating the ownership conflict ("no le pertenece") is shown.</li>
     *   <li>The transaction's status remains unchanged (e.g., in {@code REGISTERED} state).</li>
     * </ul>
     * <br/>
     * This test validates the system's ability to reject use case operations for unauthorized users
     * attempting to modify transactions that do not belong to them.
     *
     */
    @Test
    void givenCashierUserAndOtherUsersTransaction_whenStartUseCase_thenOwnershipError() {

        //noinspection MissingJavadoc
        record InnerDTO(User ownerB, Transaction trx) {
        }
        var objects = TestPersistedEntities.performInTransaction(ctx, em -> {
            var ownerB = User.builder()
                    .username("cashier.ownerB")
                    .role(UserRole.CASHIER)
                    .password("1234password")
                    .active(true)
                    .build();
            em.persist(ownerB);
            // given: owner A creates transaction
            var transaction = TestPersistedEntities.persistTransaction(em);
            return new InnerDTO(ownerB, transaction);
        });


        // and: cashier B in session tries to request reversal
        ctx.getUserSession().setCurrentUser(objects.ownerB);
        when(view.showReasonDialog()).thenReturn(Optional.of("Not mine"));
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(objects.trx.getId()).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        verify(view).showReasonDialog();
        verify(view).showError(contains("no le pertenece"));

        // and: status remains REGISTERED
        Transaction refreshed = transactionService.findTransactionById(objects.trx.getId()).join().orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(TransactionStatus.REGISTERED);
    }

    /**
     * Test method to verify the behavior of the system when attempting
     * to start a use case for a transaction that is in a non-registered state.
     * <br/>
     * This test uses a transaction with a specific status and a cashier user
     * to validate the system's response to invalid scenarios.
     * <br/>
     * Steps performed by the test:
     * <ol>
     *   <li>Create a transaction with a status of {@code REVERSED} and assign it to a cashier user.</li>
     *   <li>Set up a mock reason dialog to simulate user input.</li>
     *   <li>Invoke the use case via the controller.</li>
     * </ol>
     * <br/>
     * Expected outcomes:
     * <ul>
     *   <li>The use case results in an error response, indicating improper conditions for its execution.</li>
     *   <li>The reason dialog is displayed and an error message is shown with details about the invalid transaction state.</li>
     *   <li>The transaction status remains unchanged after the operation.</li>
     * </ul>
     * <br/>
     * This test ensures that the system correctly identifies and handles transactions that are not eligible
     * for the requested operation, maintaining the integrity of data and enforcing suitable business rules.
     *
     */
    @Test
    void givenCashierUserAndNonRegisteredTransaction_whenStartUseCase_thenStatusError() {
        // given
        Transaction tx = TestPersistedEntities.performInTransaction(ctx, em -> {
            var r = TestPersistedEntities.buildValidTansaction(em);
            // force different status
            r.setStatus(TransactionStatus.REVERSED);
            em.persist(r);
            return r;
        });
        ctx.getUserSession().setCurrentUser(tx.getCashier());


        when(view.showReasonDialog()).thenReturn(Optional.of("valid reason"));
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(tx.getId()).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        verify(view).showReasonDialog();
        verify(view).showError(contains("estado válido"));
        // and: keep status
        Transaction refreshed = transactionService.findTransactionById(tx.getId()).join().orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(TransactionStatus.REVERSED);
    }

    /**
     * Test case to verify the behavior of the reversion use case when invoked by a cashier user with a valid reason.
     * <br/>
     * This test ensures that the request for a transaction reversion is properly delegated, the reason is validated and
     * accepted, and a success message is displayed. Additionally, the transaction status and reversion request are
     * correctly updated in the system, and an audit log entry is created.
     *
     * <h4>Test Scenario</h4>
     * <ol>
     *   <li>A transaction and its corresponding cashier user are set up in the system.</li>
     *   <li>The cashier user is logged in, and the reversion reason is provided via a dialog.</li>
     *   <li>The controller's `startUseCase` method is called with the transaction ID.</li>
     * </ol>
     *
     * <h4>Assertions</h4>
     * <ul>
     *   <li>Ensure the `UseCaseResultType` is set to {@code OK}, indicating successful operation.</li>
     *   <li>Verify that the `reason dialog` is displayed to the user.</li>
     *   <li>Ensure the success message is displayed at least once, confirming a successful reversion registration.</li>
     *   <li>Validate that the transaction status is updated to {@code REVERSION_REQUESTED}.</li>
     *   <li>Check that a corresponding reversion request is created and linked to the transaction.</li>
     *   <li>Verify that an audit log entry exists for the reversion request, associated with the cashier user.</li>
     * </ul>
     *
     * <h4>Exceptions</h4>
     * <ul>
     *   <li>{@code Exception}: General exceptions that may occur during session management or transaction processing.</li>
     * </ul>
     *
     * <h4>Test Dependencies</h4>
     * <ul>
     *   <li>A pre-configured transaction entity with an associated cashier user.</li>
     *   <li>Services and controllers configured in the system context for transaction management and reversion handling.</li>
     *   <li>A working implementation of the `view` and its methods for user interaction.</li>
     * </ul>
     */
    @Test
    void givenCashierUserAndValidReason_whenStartUseCase_thenDelegatesAndShowsSuccess() {
        // given
        Transaction tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        User cashier = tx.getCashier();
        ctx.getUserSession().setCurrentUser(cashier);
        when(view.showReasonDialog()).thenReturn(Optional.of("mistyped amount"));
        var controller = new RequestReversionController(ctx);

        // when
        var result = controller.startUseCase(tx.getId()).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        assertThat(result.value()).isNotNull();
        verify(view).showReasonDialog();
        verify(view, atLeastOnce()).showSuccess(contains("Solicitud de reversión registrada"));

        // and: tx status updated and request exists
        Transaction refreshed = transactionService.findTransactionById(tx.getId()).join().orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(TransactionStatus.REVERSION_REQUESTED);

        var createdReq = reversalRequestService.findRequestByTransaction(tx.getId()).join().orElseThrow();
        assertThat(createdReq.getId()).isEqualTo(result.value().getId());
        assertThat(AuditLogChecker.checkAuditLogExists(createdReq.getId(), cashier, ctx.getEntityManagerFactory()))
                .isTrue();
    }

    /**
     * Test case to verify that an error is shown when a persistence-related issue occurs during the execution
     * of a use case initiated with an invalid or non-existent transaction ID.
     * <br/>
     * <br/>
     * <b>Details:</b>
     * <ul>
     *     <li>Sets up a cashier user in the current user session.</li>
     *     <li>Mocks the behavior of the {@code view} to provide a pre-defined reason for a dialog interaction.</li>
     *     <li>Initiates the {@code startUseCase} process with an invalid transaction ID to simulate a persistence error.</li>
     * </ul>
     * <br/>
     * <b>Expected Outcome:</b>
     * <ol>
     *     <li>The use case execution result type should be {@code ERROR}.</li>
     *     <li>Interactions with the {@code view} should include:
     *         <ul>
     *             <li>Displaying a reason dialog.</li>
     *             <li>Showing an appropriate error message related to the missing transaction.</li>
     *         </ul>
     *     </li>
     * </ol>
     * <br/>
     *
     * @throws Exception if any unexpected exception is encountered during the test execution.
     */
    @Test
    void givenPersistenceError_whenStartUseCase_thenErrorShown() throws Exception {
        // given
        User cashier = userService.createUser("cashier.err", UserRole.CASHIER, "1234password").get();
        ctx.getUserSession().setCurrentUser(cashier);
        when(view.showReasonDialog()).thenReturn(Optional.of("anything"));
        var controller = new RequestReversionController(ctx);

        // when: use a non-existent transaction id to force service error
        var result = controller.startUseCase(987654321L).join();

        // then
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        verify(view).showReasonDialog();
        verify(view).showError(contains("No se encontró la transacción"));
    }
}
