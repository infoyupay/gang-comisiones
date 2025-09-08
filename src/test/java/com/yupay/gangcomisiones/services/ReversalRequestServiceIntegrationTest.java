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
import com.yupay.gangcomisiones.model.*;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ReversalRequestService operations.
 * Covers creation and resolution flows, including role-based authorization
 * and expected transaction state transitions.
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

    /// Validates that creating a reversal request:
    /// - Persists the request and assigns an id.
    /// - Updates the associated transaction status to REVERSION_REQUESTED.
    /// @throws Exception when test catstrophically fails.
    @Test
    void testCreateReversalRequest_SetsStatusAndPersists() throws Exception {
        // given: admin creates prerequisites; cashier creates transaction and request
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("Bank RR").get();
        Concept concept = conceptService.createConcept("Electricity", ConceptType.FIXED, new BigDecimal("1.2500")).get();

        User cashier = userService.createUser("cashier.rev.create", UserRole.CASHIER, "password").get();
        ctx.getUserSession().setCurrentUser(cashier);

        Transaction tx = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("20.00"))
                .commission(new BigDecimal("2.00"))
                .status(TransactionStatus.REGISTERED)
                .build();
        transactionService.createTransaction(tx).get();

        // when: create reversal request
        ReversalRequest req = reversalRequestService
                .createReversalRequest(tx.getId(), cashier.getId(), "mistyped amount")
                .get();

        // then: id assigned and transaction moved to REVERSION_REQUESTED
        assertNotNull(req.getId(), "ReversalRequest id must be assigned by DB sequence");
        assertTrue(req.getId() > 0, "ReversalRequest id must be positive");

        Transaction refreshed = transactionService.findTransactionById(tx.getId()).get().orElseThrow();
        assertEquals(TransactionStatus.REVERSION_REQUESTED, refreshed.getStatus(),
                "Transaction must move to REVERSION_REQUESTED after creating a reversal request");
    }

    /// Ensures that resolving a reversal request:
    /// - Fails for CASHIER (insufficient privileges).
    /// - Succeeds for ADMIN and sets transaction to REVERSED when APPROVED.
    /// - Succeeds for ROOT and sets transaction to REGISTERED when DENIED.
    /// @throws Exception when test catstrophically fails.
    @Test
    void testResolveRequest_RoleEnforcedAndStatusTransitions() throws Exception {
        // given: prerequisites and a PENDING request created by cashier
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("Bank Resolve").get();
        Concept concept = conceptService.createConcept("Water", ConceptType.FIXED, new BigDecimal("0.5000")).get();

        User cashier = userService.createUser("cashier.rev.resolve", UserRole.CASHIER, "password").get();
        ctx.getUserSession().setCurrentUser(cashier);

        Transaction tx = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("15.00"))
                .commission(new BigDecimal("1.50"))
                .status(TransactionStatus.REGISTERED)
                .build();
        transactionService.createTransaction(tx).get();
        ReversalRequest req = reversalRequestService
                .createReversalRequest(tx.getId(), cashier.getId(), "client cancel")
                .get();

        // when/then: resolving as CASHIER should fail
        ExecutionException ex = assertThrows(ExecutionException.class, () ->
                reversalRequestService.resolveRequest(req.getId(), cashier.getId(), "cannot", ReversalRequestService.Resolution.DENIED).get());
        assertInstanceOf(AppSecurityException.class, ex.getCause(), "Expected PersistenceServicesException due to unprivileged role");

        // and: resolving as ADMIN APPROVED should set transaction to REVERSED
        User admin = userService.createUser("admin.resolver", UserRole.ADMIN, "password").get();
        ctx.getUserSession().setCurrentUser(admin);
        reversalRequestService.resolveRequest(req.getId(), admin.getId(), "ok", ReversalRequestService.Resolution.APPROVED).get();
        Transaction afterApproved = transactionService.findTransactionById(tx.getId()).get().orElseThrow();
        assertEquals(TransactionStatus.REVERSED, afterApproved.getStatus(),
                "Transaction must move to REVERSED after approval");

        // and: for a new request, resolving as ROOT DENIED should set transaction to REGISTERED
        User root = userService.createUser("root.resolver", UserRole.ROOT, "password").get();
        // create another tx and request by cashier
        ctx.getUserSession().setCurrentUser(cashier);
        Transaction tx2 = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("30.00"))
                .commission(new BigDecimal("3.00"))
                .status(TransactionStatus.REGISTERED)
                .build();
        transactionService.createTransaction(tx2).get();
        ReversalRequest req2 = reversalRequestService
                .createReversalRequest(tx2.getId(), cashier.getId(), "other reason")
                .get();

        ctx.getUserSession().setCurrentUser(root);
        reversalRequestService.resolveRequest(req2.getId(), root.getId(), "no", ReversalRequestService.Resolution.DENIED).get();
        Transaction afterDenied = transactionService.findTransactionById(tx2.getId()).get().orElseThrow();
        assertEquals(TransactionStatus.REGISTERED, afterDenied.getStatus(),
                "Transaction must return to REGISTERED after denial");
    }
}
