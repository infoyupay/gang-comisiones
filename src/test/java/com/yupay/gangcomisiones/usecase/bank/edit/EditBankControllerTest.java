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

package com.yupay.gangcomisiones.usecase.bank.edit;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.AuditLogChecker;
import com.yupay.gangcomisiones.usecase.PrintLineAnswer;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.MessageType;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link EditBankController}, covering the "Edit Bank" flow
 * under success, cancellation, and error scenarios.<br/>
 * Differences from the create flow:
 * <ul>
 *   <li>A pre-existing {@link Bank} must be created and provided to the controller.</li>
 *   <li>
 *       The {@link BankView} receives the entity in {@link FormMode#EDIT}, modifies it,
 *       and returns the edited instance.
 *   </li>
 *   <li>
 *       The controller then coordinates validation, privilege checks, UI interaction,
 *       and persistence through the service layer.
 *   </li>
 * </ul>
 * This suite exercises:
 * <ul>
 *   <li>
 *       Successful edits by an authenticated {@code ADMIN} user resulting in {@link UseCaseResultType#OK}
 *       and an audit log entry.
 *   </li>
 *   <li>
 *       User cancellation returning {@link UseCaseResultType#CANCEL} without success or error messages.
 *   </li>
 *   <li>
 *       Precondition failures (no authenticated user) returning {@link UseCaseResultType#ERROR} and skipping the form.
 *   </li>
 *   <li>
 *       Authorization failures (non-{@code ADMIN} user) returning {@link UseCaseResultType#ERROR}
 *       and skipping the form.
 *   </li>
 *   <li>
 *       Persistence errors (e.g., duplicate names) returning {@link UseCaseResultType#ERROR}
 *       with a user-visible error message.
 *   </li>
 * </ul>
 * <div style="border: 1px solid black; padding: 1px;">
 *     <b>Execution note:</b> dvidal@infoyupay.com passed 5 tests in 2.177s at 2025-09-09 10:57 UTC-5</div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class EditBankControllerTest extends AbstractPostgreIntegrationTest {
    /**
     * Mocked {@link BankView} to observe interactions from {@link EditBankController}
     * without triggering real UI side effects.
     */
    BankView view;

    /**
     * Initializes the test fixture before each test execution.<br/>
     * Responsibilities:
     * <ul>
     *   <li>Cleans persisted entities to start from a known, isolated state.</li>
     *   <li>Creates a mock {@link BankView} and wires message printing for easier debugging.</li>
     * </ul>
     *
     * @throws RuntimeException if the persistence context cannot be cleaned
     */
    @BeforeEach
    void prepareTest() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        view = Mockito.mock(BankView.class);
        mockViewMessages();
    }

    /**
     * Configures the mock view to print messages passed through
     * {@link BankView#showMessage(String, MessageType)}
     * using a {@link PrintLineAnswer}.<br/>
     * Notes:
     * <ul>
     *   <li>Helps to visualize messages during test execution.</li>
     *   <li>Keeps assertions focused on behavior rather than console output.</li>
     * </ul>
     */
    private void mockViewMessages() {
        doAnswer(PrintLineAnswer.get()).when(view).showMessage(anyString(), any());
    }

    /**
     * Asserts that no audit log entries exist for the current test state.<br/>
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
     * Verifies that with an authenticated {@code ADMIN} user and valid edited values,
     * the controller returns {@link UseCaseResultType#OK}, persists the update, and writes an audit log entry.<br/>
     * Scenario steps:
     * <ol>
     *   <li>Persist an {@code ADMIN} user and an initial {@link Bank}.</li>
     *   <li>Mock the {@link BankView} to edit the bank and return the modified instance.</li>
     *   <li>Invoke {@link EditBankController#run(Bank)} and await completion.</li>
     *   <li>Assert OK result, updated fields, success message, and audit log presence.</li>
     * </ol>
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void givenAdminAndValidUpdate_whenRun_thenBankUpdated() {
        // Arrange: persist an admin and an initial bank
        @SuppressWarnings("MissingJavadoc")
        record Entities(User admin, Bank bank) {
        }
        Entities persisted = TestPersistedEntities.performInTransaction(ctx, em -> {
            var admin = TestPersistedEntities.persistAdminUser(em);
            var bank = TestPersistedEntities.persistBank(em);
            return new Entities(admin, bank);
        });

        ctx.getUserSession().setCurrentUser(persisted.admin());

        // The view receives the existing bank, modifies it, and returns the modified version
        when(view.showUserForm(persisted.bank(), FormMode.EDIT)).thenAnswer(inv -> {
            var b = (Bank) inv.getArgument(0);
            b.setName("Updated Name");
            b.setActive(Boolean.TRUE);
            return Optional.of(b);
        });

        var controller = new EditBankController(ctx);

        // Act
        var result = controller.run(persisted.bank()).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        assertThat(result.value()).isNotNull()
                .extracting(Bank::getName, Bank::getActive)
                .containsExactly("Updated Name", true);

        verify(view).showUserForm(persisted.bank(), FormMode.EDIT);
        verify(view, never()).showError(anyString());
        verify(view, atLeastOnce()).showSuccess(contains("actualizado exitosamente"));

        assertThat(AuditLogChecker.checkAuditLogExists(
                persisted.bank().getId().longValue(), persisted.admin(), ctx.getEntityManagerFactory()))
                .isTrue();
    }

    /**
     * Verifies that when the {@code ADMIN} user cancels at the form,
     * the controller completes with {@link UseCaseResultType#CANCEL}
     * and does not show success or error messages.<br/>
     * Assertions:
     * <ul>
     *   <li>Form is shown in {@link FormMode#EDIT} and returns {@link Optional#empty()}.</li>
     *   <li>Result is CANCEL and value is {@code null}.</li>
     *   <li>No success or error messages are displayed.</li>
     *   <li>No audit log entries are written.</li>
     * </ul>
     */
    @Test
    void givenAdminAndCancel_whenRun_thenCancelled() {
        // Arrange
        var admin = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        var bank = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistBank);
        ctx.getUserSession().setCurrentUser(admin);
        when(view.showUserForm(any(), eq(FormMode.EDIT))).thenReturn(Optional.empty());

        var controller = new EditBankController(ctx);

        // Act
        var result = controller.run(bank).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();

        verify(view).showUserForm(bank, FormMode.EDIT);
        verify(view, never()).showError(anyString());
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /*===============*
     * Unhappy paths *
     *===============*/

    /**
     * Verifies behavior when no user is authenticated.<br/>
     * Expectations:
     * <ul>
     *   <li>The edit form is not shown.</li>
     *   <li>An error message prompts the user to log in with {@code ADMIN} privileges.</li>
     *   <li>The result is {@link UseCaseResultType#ERROR} with a {@code null} value.</li>
     *   <li>No audit log entries are written.</li>
     * </ul>
     */
    @Test
    void givenNoUser_whenRun_thenErrorAndNoFormShown() {
        // Arrange
        var bank = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistBank);
        ctx.getUserSession().setCurrentUser(null);

        var controller = new EditBankController(ctx);

        // Act
        var result = controller.run(bank).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        assertThat(ctx.getUserSession().getCurrentUser()).isNull();

        verify(view, never()).showUserForm(any(Bank.class), any());
        verify(view).showError("Inicia sesión con privilegios de ADMIN para poder editar un banco.");
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies that a non-{@code ADMIN} authenticated user cannot edit a bank.<br/>
     * Expectations:
     * <ul>
     *   <li>The form is not shown.</li>
     *   <li>An authorization error message is displayed.</li>
     *   <li>The result is {@link UseCaseResultType#ERROR} with no value.</li>
     *   <li>No audit log entries are written.</li>
     * </ul>
     */
    @Test
    void givenCashierUser_whenRun_thenErrorAndNoFormShown() {
        // Arrange
        var cashier = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistCashierUser);
        var bank = TestPersistedEntities.performInTransaction(ctx, TestPersistedEntities::persistBank);
        ctx.getUserSession().setCurrentUser(cashier);

        var controller = new EditBankController(ctx);

        // Act
        var result = controller.run(bank).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view, never()).showUserForm(any(Bank.class), any());
        verify(view).showError(contains("El usuario no tiene privilegios"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies that attempting to update a bank with a duplicate name results in an error.<br/>
     * Setup and expectations:
     * <ul>
     *   <li>Persist an {@code ADMIN} user and two banks A and B (B has a distinct name).</li>
     *   <li>Mock the view so that A is edited to use B's name (duplicate).</li>
     *   <li>Invoking {@link EditBankController#run(Bank)} completes with {@link UseCaseResultType#ERROR}.</li>
     *   <li>An error message is shown and no success message is displayed.</li>
     *   <li>No additional audit log entry is written for the failed attempt.</li>
     * </ul>
     */
    @Test
    void givenDuplicatedBankName_whenRun_thenError() {
        // Arrange: persist admin and two banks
        @SuppressWarnings("MissingJavadoc")
        record Entities(User admin, Bank a, Bank b) {
        }
        Entities persisted = TestPersistedEntities.performInTransaction(ctx, em -> {
            var admin = TestPersistedEntities.persistAdminUser(em);
            var a = TestPersistedEntities.persistBank(em);
            var b = Bank.builder()
                    .name("Bank B")
                    .active(true)
                    .build();
            em.persist(b);
            return new Entities(admin, a, b);
        });

        ctx.getUserSession().setCurrentUser(persisted.admin());

        // Mock the view to modify bank A to have bank B's name (duplicate)
        when(view.showUserForm(persisted.a(), FormMode.EDIT)).thenAnswer(inv -> {
            var b = (Bank) inv.getArgument(0);
            b.setName(persisted.b().getName());
            b.setActive(Boolean.TRUE);
            return Optional.of(b);
        });

        var controller = new EditBankController(ctx);

        // Act
        var result = controller.run(persisted.a()).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();

        verify(view).showUserForm(persisted.a(), FormMode.EDIT);
        verify(view).showError(contains("Error al actualizar un Banco"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }
}
