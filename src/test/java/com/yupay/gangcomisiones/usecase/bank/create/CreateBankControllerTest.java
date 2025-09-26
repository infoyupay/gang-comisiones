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

package com.yupay.gangcomisiones.usecase.bank.create;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.AuditLogChecker;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link CreateBankController}, covering the "Create Bank" flow
 * under success, cancellation, and error scenarios.
 * <br/>
 * This test class verifies:
 * <ul>
 *   <li>
 *       Successful creation when an {@code ADMIN} user provides valid input
 *   via {@link BankView} in {@link FormMode#CREATE}.
 *   </li>
 *   <li>Cancellation when the user dismisses the form (no input returned).</li>
 *   <li>Error handling when no user is authenticated.</li>
 *   <li>Error handling when an authenticated non-{@code ADMIN} user attempts creation.</li>
 *   <li>Error propagation on duplicate bank names returned by the persistence layer.</li>
 * </ul>
 * <div style="border: 1px solid black; padding: 1px;">
 *   <b>Execution note:</b> dvidal@infoyupay.com passed 5 tests in 2.037s at 2025-09-11 15:04 UTC-5.
 *   </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class CreateBankControllerTest extends AbstractPostgreIntegrationTest {
    /**
     * Mocked {@link BankView} to observe interactions from {@link CreateBankController}
     * without invoking UI side effects.
     */
    BankView view;
    static final AtomicReference<Bank> bankRef = new AtomicReference<>(null);

    /**
     * Initializes the test fixture before each test execution.
     * <ul>
     *   <li>Cleans persisted entities to start from a known state.</li>
     *   <li>Creates a mock {@link BankView} and wires message printing for easier debugging.</li>
     * </ul>
     *
     * @throws RuntimeException if the persistence context cannot be cleaned
     */
    @BeforeEach
    void prepareTest() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Cleans up the test environment after each test method execution.
     * <br/>
     * Resets the `view` field to `null` and removes any registered instance of
     * {@link BankView} from the {@code viewRegistry}.
     */
    @AfterEach
    void cleanUp() {
        view = null;
        if (viewRegistry.isRegistered(BankView.class)) {
            viewRegistry.unregister(BankView.class);
        }
    }


    /**
     * Asserts that no audit log entries exist for the current test state.
     * <ul>
     *   <li>Uses {@link AuditLogChecker#checkAnyAuditLogExists(EntityManagerFactory)}.</li>
     *   <li>Intended to validate that error and cancel paths do not write audit logs.</li>
     * </ul>
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
     * Verifies that with an authenticated {@code ADMIN} user and valid bank input,
     * {@link CreateBankController#run()} results in {@link UseCaseResultType#OK} and persists the bank.
     * <ul>
     *   <li>Arranges an {@code ADMIN} user in session.</li>
     *   <li>Mocks {@link BankView#showUserForm(FormMode)} to return a valid {@link Bank}.</li>
     *   <li>
     *       Asserts OK result, returned value mirrors input name/active, success message shown, and audit log written.
     *   </li>
     * </ul>
     */
    @Test
    void givenAdminAndValidBank_whenRun_thenBankCreated() {
        // Arrange
        var admin = performInTransaction(TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        var sampleBank = Bank.builder().name("Random Bank").active(true).build();
        bankRef.set(sampleBank);
        view = TestViews.bankView(FormMode.CREATE, bankRef);
        viewRegistry.registerInstance(BankView.class, view);
        var controller = new CreateBankController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        verify(view).showUserForm(FormMode.CREATE);
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        //noinspection DataFlowIssue
        assertThat(result.value()).isNotNull()
                .extracting(Bank::getName, Bank::getActive)
                .containsExactly(sampleBank.getName(), sampleBank.getActive());

        verify(view, never()).showError(anyString());
        verify(view, atLeastOnce()).showSuccess(contains("creado exitosamente con código"));

        assertThat(AuditLogChecker.checkAuditLogExists(
                result.value().getId().longValue(), admin, ctx.getEntityManagerFactory()))
                .isTrue();
    }

    /**
     * Verifies that when the {@code ADMIN} user cancels at the form,
     * {@link CreateBankController#run()} completes with {@link UseCaseResultType#CANCEL}
     * and does not show success or error messages.
     * <ul>
     *   <li>Arranges an {@code ADMIN} user in session.</li>
     *   <li>Mocks {@link BankView#showUserForm(FormMode)} to return {@link Optional#empty()}.</li>
     *   <li>Asserts CANCEL result, no value, no messages, and no audit log entries.</li>
     * </ul>
     */
    @Test
    void givenAdminAndCancel_whenRun_thenCancelled() {
        // Arrange
        var admin = performInTransaction(TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        bankRef.set(null);
        view = TestViews.bankView(FormMode.CREATE, bankRef);
        viewRegistry.registerInstance(BankView.class, view);

        var controller = new CreateBankController(ctx);

        // Act
        var result = controller.run().join();

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
     * Verifies that when no user is authenticated, the controller:
     * <ul>
     *   <li>Does not show the creation form.</li>
     *   <li>Shows an error prompting for {@code ADMIN} login.</li>
     *   <li>Completes with {@link UseCaseResultType#ERROR} and no value.</li>
     *   <li>Does not write any audit log entry.</li>
     * </ul>
     */
    @Test
    void givenNoUser_whenRun_thenErrorAndNoFormShown() {
        // Arrange
        var sampleBank = Bank.builder().name("Random Bank").active(true).build();
        bankRef.set(sampleBank);
        view = TestViews.bankView(FormMode.CREATE, bankRef);
        viewRegistry.registerInstance(BankView.class, view);
        ctx.getUserSession().setCurrentUser(null);

        var controller = new CreateBankController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        assertThat(ctx.getUserSession().getCurrentUser()).isNull();

        verify(view, never()).showUserForm(any());
        verify(view).showError("Inicia sesión con privilegios de ADMIN para poder crear un banco.");
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies that a non-{@code ADMIN} authenticated user cannot create a bank:
     * <ul>
     *   <li>Form is not shown.</li>
     *   <li>An authorization error message is displayed.</li>
     *   <li>Result is {@link UseCaseResultType#ERROR} with no value.</li>
     *   <li>No audit log is written.</li>
     * </ul>
     */
    @Test
    void givenCashierUser_whenRun_thenErrorAndNoFormShown() {
        // Arrange
        var cashier = performInTransaction(TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);
        var sampleBank = Bank.builder().name("Random Bank").active(true).build();
        bankRef.set(sampleBank);
        view = TestViews.bankView(FormMode.CREATE, bankRef);
        viewRegistry.registerInstance(BankView.class, view);

        var controller = new CreateBankController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view, never()).showUserForm(any());
        verify(view).showError(contains("El usuario no tiene privilegios"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies that attempting to create a bank with a duplicate name results in an error:
     * <ul>
     *   <li>Form is shown and returns an entity with an existing name.</li>
     *   <li>Result is {@link UseCaseResultType#ERROR} with no value.</li>
     *   <li>An error message is displayed to the user.</li>
     *   <li>No additional audit log is written for the failed attempt.</li>
     * </ul>
     */
    @Test
    void givenDuplicatedBankName_whenRun_thenError() {
        // Arrange
        @SuppressWarnings("MissingJavadoc")
        record Entities(User admin, Bank bank) {
        }
        Entities persisted = performInTransaction(em -> {
            var user = TestPersistedEntities.persistAdminUser(em);
            var bank = TestPersistedEntities.persistBank(em);
            return new Entities(user, bank);
        });

        var bankDuplicate = Bank.builder().name(persisted.bank().getName()).active(true).build();
        bankRef.set(bankDuplicate);
        ctx.getUserSession().setCurrentUser(persisted.admin);
        view = TestViews.bankView(FormMode.CREATE, bankRef);
        viewRegistry.registerInstance(BankView.class, view);

        var controller = new CreateBankController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view).showUserForm(FormMode.CREATE);
        verify(view).showError(contains("Error al crear un Banco"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }
}

