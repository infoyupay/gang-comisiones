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
import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import com.yupay.gangcomisiones.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/// Integration tests for [TransactionService] operations.
/// Tests cover:
/// - Successful insert and AuditLog creation for the generated entity id.
/// - Failure when no user is logged in (AppContext.getUserSession()).
/// - Failure when persisting null values in required fields (bank, concept, cashier, amount, commission, status).
///   Note: field `moment` is DB-managed (DEFAULT CURRENT_TIMESTAMP, insertable=false) and not settable, thus
///   cannot be forced to null at insert time; it is validated indirectly by DB default (see model TransactionIntegrationTest).
///
/// @author InfoYupay SACS
/// @version 1.0
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

    /// Validates the successful creation of a transaction and verifies that it is properly audited.
    /// This test ensures that:
    /// 1. A transaction is created successfully when the prerequisites are met.
    /// 2. The created transaction is saved with a valid database-generated identifier.
    /// 3. An associated audit log entry is created for the transaction.
    /// Precondition:
    /// - An administrator user is logged in to set up test prerequisites.
    /// - A cashier user is logged in to perform the transaction creation.
    /// - Required entities such as a bank and a concept are created.
    /// Test Steps:
    /// - Create an administrator user and log in.
    /// - Set up the required entities: bank, concept, and cashier user.
    /// - Log in as the cashier user and prepare a transaction with the necessary fields.
    /// - Call the service to create the transaction.
    /// - Verify that the transaction has been assigned a valid positive identifier.
    /// - Query the audit log to confirm that an entry exists for the created transaction's identifier.
    /// Expected Outcome:
    /// - The transaction is persisted successfully with a valid identifier.
    /// - At least one audit log entry exists associated with the created transaction's identifier.
    ///
    /// @throws Exception if an error occurs during the test execution
    @Test
    void testCreateTransaction_SuccessAndAudited() throws Exception {
        // given: create prerequisites as admin, then login as cashier to create the transaction
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("Interbank").get();
        Concept concept = conceptService.createConcept("Telephone", ConceptType.FIXED, new BigDecimal("1.0000")).get();

        User cashier = userService.createUser("cashier.tx", UserRole.CASHIER, "password").get();
        ctx.getUserSession().setCurrentUser(cashier);

        Transaction tx = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("10.00"))
                .commission(new BigDecimal("1.00"))
                .status(TransactionStatus.REGISTERED)
                .build();

        // when
        transactionService.createTransaction(tx).get();

        // then: id assigned
        assertNotNull(tx.getId(), "Transaction id must be assigned by DB sequence");
        assertTrue(tx.getId() > 0, "Transaction id must be positive");

        // and an audit log with entityId must exist
        try (EntityManager em = ctx.getEntityManagerFactory().createEntityManager()) {
            List<AuditLog> logs = em.createQuery("SELECT a FROM AuditLog a WHERE a.entityId = :id", AuditLog.class)
                    .setParameter("id", tx.getId())
                    .getResultList();
            assertFalse(logs.isEmpty(), "AuditLog should contain at least one entry for created transaction");
        }
    }

    /// Tests that creating a transaction fails when no user is logged in.
    /// Validates that the system prevents the creation of a transaction when
    /// there is no currently logged-in user, resulting in a [PersistenceException].
    /// Test Scenario:
    /// 1. An administrator user is created and logged in to perform initial setups.
    /// 2. Prerequisite entities, such as a bank, concept, and cashier, are created.
    /// 3. The administrator user is logged out, ensuring no user is active in the session.
    /// 4. A transaction object is built with valid data.
    /// 5. The transaction creation is attempted using the service.
    /// 6. The service fails with an exception due to the absence of a logged-in user.
    /// Expected Outcome:
    /// - The transaction creation fails with an [ExecutionException],
    ///   whose cause is a [PersistenceException], indicating the requirement
    ///   for an audit user in the system.
    ///
    /// @throws Exception if any unexpected errors occur during test execution.
    @Test
    void testCreateTransaction_FailsWhenNoUserLogged() throws Exception {
        // given: create prerequisites but ensure no user is logged at the moment of creation
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("ACME").get();
        Concept concept = conceptService.createConcept("Internet", ConceptType.RATE, new BigDecimal("0.1000")).get();
        User cashier = userService.createUser("cashier.no.session", UserRole.CASHIER, "password").get();

        // logout so that AuditLogger has no actor
        ctx.getUserSession().logout();

        Transaction tx = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("5.00"))
                .commission(new BigDecimal("0.50"))
                .status(TransactionStatus.REGISTERED)
                .build();

        // when/then
        ExecutionException ex = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(tx).get());
        assertInstanceOf(PersistenceException.class, ex.getCause(), "Expected PersistenceException due to null audit user");
    }

    /// Verifies that creating a transaction fails when any required field is null.
    /// This test ensures that the system prevents creating invalid transactions by
    /// raising a [PersistenceException] when any mandatory field in the
    /// transaction object is null. The following fields are validated in this test:
    /// - Bank
    /// - Concept
    /// - Cashier
    /// - Amount
    /// - Commission
    /// - Status
    /// Test Scenario:
    /// 1. An administrator user is created and logged in to set up the required environment.
    /// 2. Valid prerequisite entities are created, including a bank, a concept, and a cashier.
    /// 3. A series of transactions are built with each essential field set to null, one at a time.
    /// 4. Each transaction is passed to the `transactionService.createTransaction` method.
    /// 5. The service is expected to throw an [ExecutionException] with a cause of
    ///    [PersistenceException] due to the violation of null constraints.
    /// Expected Outcome:
    /// - The system should raise an exception for each transaction, indicating that the
    ///   respective null field is not allowed.
    /// Notes:
    /// - The `moment` field is excluded from this test as it is managed by the database
    ///   with a default value of `CURRENT_TIMESTAMP`, and cannot be explicitly set to null.
    ///
    /// @throws Exception if there are errors during test execution
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testCreateTransaction_NullFieldsFail() throws Exception {
        // given: valid prerequisites and logged-in cashier to avoid failing due to missing audit user
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("Nacional").get();
        Concept concept = conceptService.createConcept("Water", ConceptType.FIXED, new BigDecimal("2.0000")).get();
        User cashier = userService.createUser("cashier.nulls", UserRole.CASHIER, "password").get();
        ctx.getUserSession().setCurrentUser(cashier);

        // bank null
        Transaction t1 = Transaction.builder()
                .bank(null)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("1.00"))
                .commission(new BigDecimal("0.10"))
                .status(TransactionStatus.REGISTERED)
                .build();
        ExecutionException ex1 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t1).get());
        assertInstanceOf(PersistenceException.class, ex1.getCause());

        // concept null
        Transaction t2 = Transaction.builder()
                .bank(bank)
                .concept(null)
                .cashier(cashier)
                .amount(new BigDecimal("1.00"))
                .commission(new BigDecimal("0.10"))
                .status(TransactionStatus.REGISTERED)
                .build();
        ExecutionException ex2 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t2).get());
        assertInstanceOf(PersistenceException.class, ex2.getCause());

        // cashier null
        Transaction t3 = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(null)
                .amount(new BigDecimal("1.00"))
                .commission(new BigDecimal("0.10"))
                .status(TransactionStatus.REGISTERED)
                .build();
        ExecutionException ex3 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t3).get());
        assertInstanceOf(GangComisionesException.class, ex3.getCause());

        // amount null
        Transaction t4 = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(null)
                .commission(new BigDecimal("0.10"))
                .status(TransactionStatus.REGISTERED)
                .build();
        ExecutionException ex4 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t4).get());
        assertInstanceOf(PersistenceException.class, ex4.getCause());

        // commission null
        Transaction t5 = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("1.00"))
                .commission(null)
                .status(TransactionStatus.REGISTERED)
                .build();
        ExecutionException ex5 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t5).get());
        assertInstanceOf(PersistenceException.class, ex5.getCause());

        // status null
        Transaction t6 = Transaction.builder()
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(new BigDecimal("1.00"))
                .commission(new BigDecimal("0.10"))
                .status(null)
                .build();
        ExecutionException ex6 = assertThrows(ExecutionException.class, () -> transactionService.createTransaction(t6).get());
        assertInstanceOf(PersistenceException.class, ex6.getCause());

        // NOTE: moment is DB-managed (DEFAULT CURRENT_TIMESTAMP), cannot be set to null for insertion.
    }
}
