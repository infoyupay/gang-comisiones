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

package com.yupay.gangcomisiones.usecase.bank.manage;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.registry.ControllerRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

/// Test class for the functionality and behavior of [ManageBankController].
///
/// This class is responsible for testing the "Manage Bank" feature, ensuring that all its
/// operations such as creating, editing, and listing banks, as well as privilege propagation,
/// function correctly. It leverages the setup from [AbstractPostgreIntegrationTest] to
/// provide an integration testing environment with a PostgreSQL backend.
///
/// Functionalities under test cover:
/// - Validation of controller interactions with associated views.
/// - Proper handling of the user session and bank operations.
/// - Assertions of state transitions and user feedback displayed in the UI.
///
/// @author InfoYupay SACS
/// @version 1.0
/// <div style="border: 1px solid black; padding: 1px;">
///     <strong>Execution note:</strong> dvidal@infoyupay.com passed 1 tests in 7.412s at 2025-09-12 00:25 UTC-5
/// </div>
/// @implNote although the time seems excesive, pauses are expected due to the nature of the tests. In order
/// to simulate human user interactions, a pause between the usage of one or another option has been made.
class ManageBankControllerTest extends AbstractPostgreIntegrationTest {
    final AtomicReference<Bank> bank = new AtomicReference<>();
    User user;
    BankView bankView;
    BankBoardView board;
    ManageBankController controller;

    /// Arranges the test environment before each test execution.
    /// This method initializes the test setup by performing the following actions:
    /// - Cleans persisted entities from the database using `TestPersistedEntities.clean`.
    /// - Initializes a test user by persisting an admin user and assigns it to the `user` field.
    /// - Prepares a new `Bank` instance with the name "My New Bank" and assigns it to the `bank` field.
    /// - Sets up view instances for `BankBoardView` and `BankView`, registering them with the `viewRegistry`.
    /// - Associates the current test user with the user session using `ctx.getUserSession().setCurrentUser`.
    /// - Resolves the `ManageBankController` instance from the default controller registry and assigns it to the `controller` field.
    /// This method ensures that all test dependencies and components are consistently initialized to a known state,
    /// avoiding inconsistencies and potential side effects between test cases. It is annotated with `@BeforeEach`,
    /// ensuring its execution before every test method in the corresponding test class.
    @BeforeEach
    void arrange() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        user = performInTransaction(TestPersistedEntities::persistAdminUser);
        bank.set(Bank.builder().name("My New Bank").build());
        board = TestViews.bankBoardView();
        bankView = TestViews.bankView(null, bank, true);
        viewRegistry.registerInstance(BankBoardView.class, board);
        viewRegistry.registerInstance(BankView.class, bankView);
        ctx.getUserSession().setCurrentUser(user);
        controller = ControllerRegistries.defaultRegistry().resolve(ManageBankController.class);
    }

    /// Cleans up the test environment after each test execution.
    /// This method resets all relevant fields and registry entries to a null or default state,
    /// ensuring that subsequent test cases are not affected by leftover data or state. It is annotated
    /// with `@AfterEach`, indicating it will be executed after every test in the corresponding test class.
    /// Actions performed:
    /// - Sets the following fields to `null`:
    ///   - `user`
    ///   - `bank`
    ///   - `board`
    ///   - `bankView`
    ///   - `controller`
    /// - Unregisters the components associated with [BankBoardView] and [BankView] from the `viewRegistry`.
    /// - Resets the currently logged-in user to `null` using `ctx.getUserSession().setCurrentUser(null)`.
    @AfterEach
    void cleanUp() {
        user = null;
        bank.set(null);
        board = null;
        bankView = null;
        viewRegistry.unregister(BankBoardView.class);
        viewRegistry.unregister(BankView.class);
        ctx.getUserSession().setCurrentUser(null);
        controller = null;
    }

    /// Tests the functionality of the "Manage Bank" feature when executed by a logged-in admin user.
    /// This test verifies if all operations (create, edit, list, and privileges propagation) are
    /// successfully performed, and ensures that the view and controller interact as expected.
    ///
    /// Test Steps:
    /// - Setup and initiate the "Manage Bank" use case by calling `startUseCase()`.
    /// - Simulate operations including creating a bank, editing the bank details, and listing all banks.
    /// - Validate the interactions between the controller and the associated views using mock verifications.
    ///
    /// Verifications:
    /// - Ensures that the view is displayed and closed appropriately during the use case lifecycle.
    /// - Confirms that the bank creation and editing operations are reflected correctly in the view.
    /// - Checks that the privileges are properly propagated for the logged-in admin user.
    /// - Asserts the success messages displayed during the creation and editing processes.
    ///
    /// This test uses mock objects and in-order verifications to validate the sequence and correctness
    /// of interactions between the controller and the view layer.
    @Test
    void givenLogedAdmin_whenIntentsAllOptions_success() {
        /*=====*
         * ACT *
         *=====*/
        controller.startUseCase();
        // pause();
        controller.createBank();
        pause();
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            em.createQuery("SELECT B FROM Bank B", Bank.class).getResultList()
                    .stream()
                    .findAny()
                    .ifPresent(bank::set);
        }
        controller.editBank(bank.get());
        pause();
        controller.listBank();
        pause();
        board.closeView();

        /*========*
         * ASSERT *
         *========*/

        verify(board).showView();
        verify(board).showView();
        verify(board, atLeastOnce()).showList(ArgumentMatchers.notNull());
        verify(board).closeView();
        verify(board).insert(any(Bank.class));
        verify(board).replace(any(Bank.class));
        verify(board).propagatePrivileges(any(User.class));

        verify(bankView).showUserForm(eq(FormMode.CREATE));
        verify(bankView).showUserForm(nullable(Bank.class), eq(FormMode.EDIT));
        var _inOrder = inOrder(bankView);
        _inOrder.verify(bankView).showSuccess(contains("creado exitosamente"));
        _inOrder.verify(bankView).showSuccess(contains("actualizado exitosamente"));
    }

    /**
     * Pauses the execution of the current thread for a fixed duration of 2 seconds.
     * If the thread is interrupted during the sleep, the interruption status is restored.
     * <br/>
     * This method can be useful in scenarios where a brief delay in execution
     * is required, such as waiting for processing or simulating latency.
     */
    void pause() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

}
