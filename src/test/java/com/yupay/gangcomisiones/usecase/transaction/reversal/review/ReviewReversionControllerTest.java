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

package com.yupay.gangcomisiones.usecase.transaction.reversal.review;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.services.ReversalRequestService;
import com.yupay.gangcomisiones.services.TransactionService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit/integration tests for {@link ReviewReversionController} orchestration.
 * Focuses on controller responsibilities without duplicating domain logic or auditing.
 *<br/>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution note:</strong> dvidal@infoyupay.com passed 7 test in 7.185s at 2025-09-17 11:30 UTC-5.
 * </div>
 * @author InfoYupay SACS
 * @version 1.0
 */
class ReviewReversionControllerTest extends AbstractPostgreIntegrationTest {

    private ReviewReversionView view;

    private UserService userService;
    private TransactionService transactionService;
    private ReversalRequestService reversalRequestService;
    private CountDownLatch listLoadingLatch;

    /**
     * Sets up the test environment by initializing necessary components and dependencies before each test case.
     * <br/>
     * This method is annotated with {@code @BeforeEach} to ensure it runs before every test method in the class.
     * It performs the following actions:
     *
     * <ol>
     *   <li>Initializes a {@code CountDownLatch} instance to synchronize testing of asynchronous operations.</li>
     *   <li>Cleans persisted entities from the application's database by invoking {@code TestPersistedEntities.clean}.</li>
     *   <li>Initializes service dependencies, including {@code userService}, {@code transactionService},
     *       and {@code reversalRequestService} using the application context.</li>
     *   <li>Mocks the {@code ReviewReversionView} and stubs its behavior using utility classes
     *       {@code TestViews.stubBoardView} and {@code TestViews.stubSecondaryView}.</li>
     *   <li>Configures the {@code view} behavior by stubbing methods, including setting up the initial criteria logic.</li>
     *   <li>Registers the mocked {@code ReviewReversionView} instance with the {@code viewRegistry}.</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        listLoadingLatch = new CountDownLatch(1);
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        userService = ctx.getUserService();
        transactionService = ctx.getTransactionService();
        reversalRequestService = ctx.getReversalRequestService();

        view = mock(ReviewReversionView.class);
        TestViews.stubBoardView(view, r -> "Req{id=%d, tx=%d, status=%s}"
                        .formatted(r.getId(), r.getTransaction().getId(), r.getStatus()),
                listLoadingLatch);
        TestViews.stubSecondaryView(view);
        when(view.initialCriteria()).thenCallRealMethod();
        viewRegistry.registerInstance(ReviewReversionView.class, view);
    }

    /**
     * Cleans up resources and resets state after each test execution.
     * <br/>
     * This method is annotated with {@code @AfterEach}, indicating that it is executed after each test method in the test class.
     * <br/><br/>
     * The {@code tearDown} method performs the following operations:
     * <ol>
     *   <li>Checks if the {@code ReviewReversionView} is registered in the {@code viewRegistry}. If it is registered, it unregisters it.</li>
     *   <li>Sets the {@code view} variable to {@code null}.</li>
     *   <li>Resets {@code listLoadingLatch} to {@code null}.</li>
     * </ol>
     * <br/>
     * This ensures that each test runs with a clean state, avoiding side effects between tests.
     */
    @AfterEach
    void tearDown() {
        if (viewRegistry.isRegistered(ReviewReversionView.class)) {
            viewRegistry.unregister(ReviewReversionView.class);
        }
        view = null;
        listLoadingLatch =null;
    }

    /**
     * Tests the behavior of the use case when initiated by an admin user, ensuring that:
     * <ul>
     *   <li>A pending request is loaded and displayed.</li>
     *   <li>The status of the loaded request remains pending after the process starts.</li>
     *   <li>The corresponding view methods are invoked to show the loaded data and display the board.</li>
     * </ul>
     *
     * <br/>
     *
     * <b>Steps:</b>
     * <ol>
     *   <li>Create a pending request initiated by a regular cashier user.</li>
     *   <li>Set an admin user in the session to facilitate privileged actions.</li>
     *   <li>Initiate the use case using the admin user's session and ensure the list is loaded within the expected time.</li>
     *   <li>Verify the appropriate view is displayed and the list of requests is shown.</li>
     *   <li>Assert that the status of the reversal request remains in the PENDING state until further resolution.</li>
     * </ol>
     *
     * <br/>
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the list loading latch.
     */
    @Test
    void givenAdminUser_whenStartUseCase_thenLoadsPendingAndShowsBoard() throws InterruptedException {
        // given: create a pending request
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        ctx.getUserSession().setCurrentUser(cashier);
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake 7864e6").join();
        // and: admin in session
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        var controller = new ReviewReversionController(ctx);

        // when
        controller.startUseCase();
        assertThat(listLoadingLatch.await(5, TimeUnit.SECONDS))
                .withFailMessage(()->"Timeout waiting for list loading in 5 seconds.")
                .isTrue();
        // then
        verify(view, atLeastOnce()).showView();
        verify(view, atLeastOnce()).showList(any());
        // still pending before resolution
        var refreshed = reversalRequestService.findRequestById(req.getId()).join().orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(ReversalRequestStatus.PENDING);
    }

    /**
     * Tests that when an administrator user attempts to resolve a reversal request and cancels the operation,
     * no changes are made to the request or transaction, and appropriate feedback is displayed.
     * <br/>
     * <br/>
     * <b>Test Case Description:</b>
     * <ul>
     *   <li>A transaction is initially persisted with a cashier.</li>
     *   <li>A reversal request related to the transaction is created and remains in a pending state.</li>
     *   <li>An administrator user is set as the current user in the session.</li>
     *   <li>A cancellation is simulated by returning an empty {@code Optional} from the resolve dialog.</li>
     *   <li>The selected reversal request is then attempted to be resolved.</li>
     * </ul>
     * <br/>
     * <b>Expected Behaviors:</b>
     * <ol>
     *   <li>The result is {@code null} with a use-case result type of {@code CANCEL}.</li>
     *   <li>A success message indicating the cancellation is displayed via the view.</li>
     *   <li>The transaction related to the request remains in its original "Reversion Requested" status.</li>
     *   <li>The reversal request remains in its original "Pending" status.</li>
     * </ol>
     */
    @Test
    void givenAdminUser_whenCancelOnResolve_thenNoChangesAndInfoShown() {
        // given
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake").join();
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        when(view.showResolveDialog(any(ReversalRequest.class))).thenReturn(Optional.empty());
        var controller = new ReviewReversionController(ctx);

        // when
        var result = controller.resolveSelected(req).join();

        // then
        assertThat(result)
                .extracting(Result::value)
                .isNull();
        assertThat(result)
                .extracting(Result::result)
                .isEqualTo(UseCaseResultType.CANCEL);
        verify(view, atLeastOnce()).showSuccess(contains("Operación cancelada"));
        var refreshedTx = transactionService.findTransactionById(tx.getId()).join().orElseThrow();
        assertThat(refreshedTx.getStatus()).isEqualTo(TransactionStatus.REVERSION_REQUESTED);
        var refreshedReq = reversalRequestService.findRequestById(req.getId()).join().orElseThrow();
        assertThat(refreshedReq.getStatus()).isEqualTo(ReversalRequestStatus.PENDING);
    }

    /**
     * Test method to verify behavior when an admin user attempts to resolve a reversal request
     * with an approved resolution but provides a blank justification. This test ensures the
     * action is canceled, and appropriate status updates and user feedback are provided.
     * <br/>
     *
     * Behavior Verified:
     * <ul>
     *   <li>The resolution process is canceled when a blank justification is supplied.</li>
     *   <li>An informative success message is displayed to indicate the cancellation of the operation.</li>
     *   <li>The status of the reversal request remains as "pending" after the resolution attempt.</li>
     * </ul>
     * <br/>
     *
     * Preconditions:
     * <ol>
     *   <li>A transaction is created and persisted in the database.</li>
     *   <li>A cashier creates a reversal request for the transaction with a justification (e.g., "mistake").</li>
     *   <li>An admin user is set as the current session user to resolve the request.</li>
     *   <li>The system mock for the view is configured to simulate the resolution dialog
     *       with an approved resolution and a blank justification supplied.</li>
     * </ol>
     * <br/>
     *
     * Expected Results:
     * <ul>
     *   <li>The resolution result's {@code result} is {@code UseCaseResultType.CANCEL}.</li>
     *   <li>The resolution result's {@code value} is {@code null}.</li>
     *   <li>The reversal request's final status remains "pending".</li>
     *   <li>A success message is displayed to inform the user about the cancellation of the resolution process.</li>
     * </ul>
     */
    @Test
    void givenAdminUserAndBlankJustification_whenResolve_thenCancelled() {
        // given
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake").join();
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        when(view.showResolveDialog(any(ReversalRequest.class)))
                .thenReturn(Optional.of(new ReviewReversionView.ResolutionInput(
                        ReviewReversionView.ResolutionInput.Resolution.APPROVED, "  \t  ")));
        var controller = new ReviewReversionController(ctx);

        // when
        var result = controller.resolveSelected(req).join();

        // then
        assertThat(result).extracting(Result::result).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result).extracting(Result::value).isNull();

        verify(view, atLeastOnce()).showSuccess(contains("Operación cancelada"));
        // unchanged
        var refreshed = reversalRequestService.findRequestById(req.getId()).join().orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(ReversalRequestStatus.PENDING);
    }

    /**
     * Test method to verify the system behavior when a reversal request
     * is resolved with an approved resolution. Specifically, this test ensures
     * that the transaction is successfully reversed, and the reversal request
     * is updated accordingly.
     * <br/>
     *
     * Behavior Verified:
     * <ul>
     *   <li>The transaction associated with the reversal request has its status
     *       updated to "reversed" upon resolution.</li>
     *   <li>The reversal request's status is updated to "approved".</li>
     *   <li>The reversal request contains updated fields such as the admin who
     *       resolved the request, and the resolution timestamp.</li>
     *   <li>A success message is displayed to notify about the resolution.</li>
     * </ul>
     * <br/>
     *
     * Preconditions:
     * <ol>
     *   <li>A transaction is created and persisted in the database.</li>
     *   <li>A cashier creates a reversal request for the transaction
     *       with a justification (e.g., "mistake").</li>
     *   <li>An admin user is set as the current session user to resolve
     *       the request.</li>
     *   <li>The system mock for the view is configured to simulate the resolution
     *       dialog with an approved resolution and a justification supplied.</li>
     * </ol>
     * <br/>
     *
     * Expected Results:
     * <ul>
     *   <li>The reversal request's status is "approved" after resolution.</li>
     *   <li>The reversal request includes updated fields such as the resolver
     *       (admin) and the resolution timestamp.</li>
     *   <li>The associated transaction's status is updated to "reversed".</li>
     *   <li>A success message is displayed to confirm that the resolution is complete.</li>
     * </ul>
     */
    @Test
    void givenApprovedResolution_whenResolve_thenTxReversed() {
        // given
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake").join();
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        when(view.showResolveDialog(any(ReversalRequest.class)))
                .thenReturn(Optional.of(new ReviewReversionView.ResolutionInput(
                        ReviewReversionView.ResolutionInput.Resolution.APPROVED, "ya puedes ejecutar tus gracias")));
        var controller = new ReviewReversionController(ctx);

        // when
        controller.resolveSelected(req).join();

        // then
        verify(view, atLeastOnce()).showSuccess(contains("resuelta"));
        var refreshedTx = transactionService.findTransactionById(tx.getId()).join().orElseThrow();
        assertThat(refreshedTx.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        var refreshedReq = reversalRequestService.findRequestById(req.getId()).join().orElseThrow();
        assertThat(refreshedReq.getStatus()).isEqualTo(ReversalRequestStatus.APPROVED);
        assertThat(refreshedReq.getEvaluatedBy()).isEqualTo(admin);
        assertThat(refreshedReq.getAnswerStamp()).isNotNull();
    }

    /**
     * Test method to verify behavior when a reversal request is resolved with a rejected resolution.
     * Specifically, this test ensures that the transaction remains in the "registered" state and
     * the reversal request's status is updated to "rejected".
     * <br/>
     * Behavior Verified:
     * <ul>
     *   <li>The system updates the status of the reversal request to "rejected" upon resolution.</li>
     *   <li>The transaction associated with the reversal request remains in the "registered" state.</li>
     *   <li>The resolution input dialog is displayed to capture the rejection justification.</li>
     * </ul>
     * <br/>
     * Preconditions:
     * <ol>
     *   <li>A transaction is created and persisted in the database.</li>
     *   <li>A cash-handling user creates a reversal request for the transaction with a justification
     *       (e.g., "mistake").</li>
     *   <li>An admin user is set as the current session user to resolve the request.</li>
     *   <li>The system mock for the view is configured to simulate the resolution dialog with a rejected
     *       resolution and a justification supplied.</li>
     * </ol>
     * <br/>
     * Expected Results:
     * <ul>
     *   <li>The resolution result is not {@code null} and corresponds to the reversal request being
     *       rejected.</li>
     *   <li>The reversal request's final status is "rejected" after resolution.</li>
     *   <li>The transaction associated with the reversal request maintains its "registered" status, and
     *       no changes are applied to it.</li>
     * </ul>
     */
    @Test
    void givenRejectedResolution_whenResolve_thenTxRegistered() {
        // given
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake").join();
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        when(view.showResolveDialog(any(ReversalRequest.class)))
                .thenReturn(Optional.of(new ReviewReversionView.ResolutionInput(
                        ReviewReversionView.ResolutionInput.Resolution.REJECTED, "no tienes suficientes razones para pedirlo")));
        var controller = new ReviewReversionController(ctx);

        // when
        var result = controller.resolveSelected(req).join();
        assertThat(result).isNotNull()
                .extracting(Result::value)
                .extracting(reversalRequest ->
                        reversalRequest != null ? reversalRequest.getStatus() : null)
                .isEqualTo(ReversalRequestStatus.REJECTED);

        // then
        var refreshedTx = transactionService.findTransactionById(tx.getId()).join().orElseThrow();
        assertThat(refreshedTx.getStatus()).isEqualTo(TransactionStatus.REGISTERED);
        var refreshedReq = reversalRequestService.findRequestById(req.getId()).join().orElseThrow();
        assertThat(refreshedReq.getStatus()).isEqualTo(ReversalRequestStatus.REJECTED);
    }

    /**
     * Test method to verify behavior when a user with invalid privileges attempts to initiate
     * the review reversion use case. Specifically, this test ensures that the board view is
     * closed, and an appropriate error message is displayed.
     * <br/>
     * Behavior Verified:
     * <ul>
     *   <li>An error message is displayed to indicate insufficient privileges.</li>
     *   <li>The view is closed to restrict further actions by the user.</li>
     * </ul>
     * <br/>
     * Preconditions:
     * <ol>
     *   <li>A user with the role of {@code CASHIER} is created and set as the current session user.</li>
     *   <li>The {@code ReviewReversionController} is initialized with the current context.</li>
     * </ol>
     * <br/>
     * Expected Results:
     * <ul>
     *   <li>The view component's {@code showError} method is invoked with a message about lack of privileges.</li>
     *   <li>The view component's {@code closeView} method is invoked at least once to close the interface.</li>
     * </ul>
     * <br/>
     * @throws Exception if an unexpected error occurs during the test execution.
     */
    @Test
    void givenInvalidPrivileges_whenStartUseCase_thenClosesBoard() throws Exception {
        // given: cashier in session
        var cashier = userService.createUser("cashier.tmp", UserRole.CASHIER, "1234pass").get();
        ctx.getUserSession().setCurrentUser(cashier);
        var controller = new ReviewReversionController(ctx);

        // when
        controller.startUseCase();

        // then
        verify(view, atLeastOnce()).showError(contains("privilegios"));
        verify(view, atLeastOnce()).closeView();
    }

    /**
     * Test method to ensure that attempting to resolve a reversal request in a non-pending state
     * results in an error message being displayed and prevents any further actions from proceeding.
     * This test covers the scenario where the request's status has been changed to "approved" prior
     * to the resolution attempt.
     * <br/>
     * Behavior Verified:
     * <ul>
     *   <li>The system checks if the reversal request is not pending anymore.</li>
     *   <li>The operation does not proceed if the status of the request is not "pending".</li>
     *   <li>An error message is displayed to inform about the improper state of the request.</li>
     *   <li>No changes are applied to the transaction in the resolved state.</li>
     *   <li>Relevant presentation layer component (view) is notified to display the error message.</li>
     * </ul>
     * <br/>
     * Preconditions:
     * <ol>
     *   <li>A transaction has been persisted.</li>
     *   <li>A reversal request for the transaction has been created with the "pending" status.</li>
     *   <li>An admin user is set as the current session user.</li>
     *   <li>The reversal request status is manually updated to "approved" to simulate a resolved case.</li>
     * </ol>
     * <br/>
     * Expected Results:
     * <ul>
     *   <li>The result's {@code value} is {@code null}.</li>
     *   <li>The result's {@code result} is {@code UseCaseResultType.ERROR}.</li>
     *   <li>The error message displayed contains information that the request is no longer pending.</li>
     * </ul>
     */
    @Test
    void givenNotPendingAnymore_whenResolve_thenShowsErrorAndDoesNotProceed() {
        // given
        var tx = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistTransaction);
        var cashier = tx.getCashier();
        var req = reversalRequestService.createReversalRequest(tx.getId(), cashier.getId(), "mistake").join();
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        // simulate already resolved by changing status directly
        TestPersistedEntities.performInTransaction(ctx, em -> {
            var managed = em.find(ReversalRequest.class, req.getId());
            managed.setStatus(ReversalRequestStatus.APPROVED);
            return null;
        });
        var controller = new ReviewReversionController(ctx);

        // when
        var result = controller.resolveSelected(req).join();

        // then
        assertThat(result).extracting(Result::value).isNull();
        assertThat(result).extracting(Result::result).isEqualTo(UseCaseResultType.ERROR);
        verify(view, atLeastOnce()).showError(contains("ya no está pendiente"));
    }
}
