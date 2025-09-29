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
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.TransactionStatus;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

/**
 * Integration tests for ReversalRequestService operations.
 * Covers creation and resolution flows, including role-based authorization
 * and expected transaction state transitions.
 * <br/>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 2 tests in 1.664s at 2025-09-29 12:47 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class ReversalRequestServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    private ReversalRequestService reversalRequestService;
    private TransactionService transactionService;
    private BankService bankService;
    private ConceptService conceptService;
    private UserService userService;

    /**
     * Prepare services and clean persisted data before each test.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        reversalRequestService = ctx.getReversalRequestService();
        transactionService = ctx.getTransactionService();
        bankService = ctx.getBankService();
        conceptService = ctx.getConceptService();
        userService = ctx.getUserService();
    }

    /**
     * Cleans up resources and resets the state of services and user context after each test execution.
     * <br/>
     * This method ensures proper isolation between test cases by:
     * <ul>
     *   <li>Releasing references to the following service objects, setting them to {@code null}:
     *   <ul>
     *     <li>{@code reversalRequestService}</li>
     *     <li>{@code transactionService}</li>
     *     <li>{@code bankService}</li>
     *     <li>{@code conceptService}</li>
     *     <li>{@code userService}</li>
     *   </ul>
     *   </li>
     *   <li>Resetting the currently logged-in user in the application context to {@code null}, effectively logging out any user.</li>
     * </ul>
     * <br/>
     * Invoked automatically as part of the test lifecycle by the {@code @AfterEach} annotation,
     * ensuring the consistent cleanup of test-related resources.
     */
    @AfterEach
    void cleanUp() {
        reversalRequestService = null;
        transactionService = null;
        bankService = null;
        conceptService = null;
        userService = null;
        ctx.getUserSession().setCurrentUser(null);
    }

    /// Validates that creating a reversal request:
    /// - Persists the request and assigns an id.
    /// - Updates the associated transaction status to REVERSION_REQUESTED.
    ///
    /// @throws Exception when test catstrophically fails.
    @Test
    void testCreateReversalRequest_SetsStatusAndPersists() throws Exception {
        // given: admin creates prerequisites; cashier creates transaction and request
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var bank = bankService.createBank("Bank RR").get();
        var concept = conceptService
                .createConcept("Electricity", ConceptType.FIXED, new BigDecimal("1.2500")).get();

        runInTransaction(em -> UserSessionHelpers.createAndLogCashierUser(ctx, em));

        var request = CreateTransactionRequest
                .builder()
                .amount(new BigDecimal("20.00"))
                .bankId(bank.getId())
                .cashierId(ctx.getUserSession().getCurrentUser().getId())
                .conceptCommissionValue(concept.getValue())
                .conceptId(concept.getId())
                .conceptType(concept.getType())
                .build();
        var tx = transactionService.createTransaction(request).get();

        // when: create reversal request
        var req = reversalRequestService
                .createReversalRequest(tx.getId(), ctx.getUserSession().getCurrentUser().getId(), "mistyped amount")
                .get();

        // then: id assigned and transaction moved to REVERSION_REQUESTED
        assertThat(req.getId())
                .as("Reversal request Assigned ID must be non-null, and positive.")
                .isNotNull()
                .isPositive();

        var refreshed = transactionService.findTransactionById(tx.getId()).get();
        assertThat(refreshed)
                .as("Transaction must move to REVERSION_REQUESTED after creating a reversal request.")
                .hasValueSatisfying(x ->
                        assertThat(x.getStatus())
                                .isEqualTo(TransactionStatus.REVERSION_REQUESTED));
    }

    /// Ensures that resolving a reversal request:
    /// - Fails for CASHIER (insufficient privileges).
    /// - Succeeds for ADMIN and sets transaction to REVERSED when APPROVED.
    /// - Succeeds for ROOT and sets transaction to REGISTERED when DENIED.
    ///
    /// @throws Exception when test catstrophically fails.
    @Test
    void testResolveRequest_RoleEnforcedAndStatusTransitions() throws Exception {
        // Arrange: prerequisites and a PENDING request created by cashier
        runInTransaction(em -> createAndLogAdminUser(ctx, em));
        var bank = bankService.createBank("Bank Resolve").get();
        var concept = conceptService
                .createConcept("Water", ConceptType.FIXED, new BigDecimal("0.5000")).get();

        runInTransaction(em -> createAndLogCashierUser(ctx, em));

        var request = CreateTransactionRequest.builder()
                .amount(new BigDecimal("15.00"))
                .bankId(bank.getId())
                .conceptId(concept.getId())
                .conceptCommissionValue(concept.getValue())
                .cashierId(ctx.getUserSession().getCurrentUser().getId())
                .conceptType(concept.getType())
                .build();

        var tx = transactionService.createTransaction(request).get();

        var req = reversalRequestService
                .createReversalRequest(tx.getId(), tx.getCashier().getId(), "client cancel")
                .get();
        var cashier = TestPersistedEntities.USER.get(UserRole.CASHIER);
        ctx.getUserSession().setCurrentUser(cashier);

        // Act: resolving as CASHIER should fail
        var ex = catchException(() ->
                reversalRequestService.resolveRequest(
                        req.getId(),
                        tx.getCashier().getId(),
                        "cannot",
                        ReversalRequestService.Resolution.DENIED).get());
        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "as not privileges to perform an operation");

        // and: resolving as ADMIN APPROVED should set transaction to REVERSED
        var admin = TestPersistedEntities.USER.get(UserRole.ADMIN);
        ctx.getUserSession().setCurrentUser(admin);
        reversalRequestService.resolveRequest(req.getId(), admin.getId(), "ok", ReversalRequestService.Resolution.APPROVED).get();

        assertThat(transactionService.findTransactionById(tx.getId()).get())
                .as("Transaction must move to REVERSED after approval.")
                .hasValueSatisfying(t -> assertThat(t.getStatus()).isEqualTo(TransactionStatus.REVERSED));

        // and: for a new request, resolving as ROOT DENIED should set transaction to REGISTERED
        runInTransaction(em -> createAndLogRootUser(ctx, em));
        // create another tx and request by cashier
        ctx.getUserSession().setCurrentUser(cashier);
        var request2 = CreateTransactionRequest.builder()
                .amount(new BigDecimal("30.00"))
                .bankId(bank.getId())
                .conceptId(concept.getId())
                .conceptCommissionValue(concept.getValue())
                .cashierId(cashier.getId())
                .conceptType(concept.getType())
                .build();

        var tx2 = transactionService.createTransaction(request2).get();
        var req2 = reversalRequestService
                .createReversalRequest(tx2.getId(), cashier.getId(), "other reason")
                .get();

        var root = TestPersistedEntities.USER.get(UserRole.ROOT);
        ctx.getUserSession().setCurrentUser(root);
        reversalRequestService.resolveRequest(req2.getId(), root.getId(), "no", ReversalRequestService.Resolution.DENIED).get();
        var afterDenied = transactionService.findTransactionById(tx2.getId()).get();
        assertThat(afterDenied)
                .as("Transaction must return to REGISTERED after denial.")
                .hasValueSatisfying(t -> assertThat(t.getStatus()).isEqualTo(TransactionStatus.REGISTERED));
    }
}
