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

package com.yupay.gangcomisiones.usecase.concept.create;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.AuditLogChecker;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import com.yupay.gangcomisiones.usecase.concept.ConceptView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for validating the behavior of the {@link CreateConceptController}.
 * This class performs unit tests to ensure the correct functionality of the controller methods, using mocked
 * dependencies and verifying the results of various interactions with the {@link ConceptView} and persistence layer.
 * <br/>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 5 tests in 2.065s at 2025-09-29 22:40 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class CreateConceptControllerTest extends AbstractPostgreIntegrationTest {
    ConceptView view;

    /**
     * Sets up the test environment before each test case execution.
     * This method ensures that all persisted entities are cleaned and
     * the database tables are truncated to maintain a consistent state.
     * It utilizes the TestPersistedEntities.clean method to reset the test data.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Handles the teardown process after each test case execution.
     * <br/>
     * This method ensures that resources utilized during test execution are properly cleaned up. In particular:
     * <ul>
     *   <li>Unregisters the {@link ConceptView} class from the {@code viewRegistry}, if it is registered.</li>
     *   <li>Resets the {@code view} instance of the test class to {@code null}.</li>
     *   <li>Clears the currently logged-in user in the {@code UserSession} associated with the {@code AppContext}.</li>
     * </ul>
     * This ensures that subsequent test cases are not affected by resources or state retained from previous executions.
     */
    @AfterEach
    void tearDown() {
        if (viewRegistry.isRegistered(ConceptView.class)) {
            viewRegistry.unregister(ConceptView.class);
        }
        view = null;
        ctx.getUserSession().setCurrentUser(null);
    }

    /**
     * Verifies that no audit log entries exist in the database.
     * <br/>
     * This method utilizes the {@link AuditLogChecker#checkAnyAuditLogExists} method to confirm that there are no persisted
     * audit log entries. If any entries are present, the assertion fails.
     * <br/>
     * Typically used in testing scenarios to ensure that specific operations do not generate audit logs as a side effect.
     */
    private void assertNoAuditLog() {
        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory())).isFalse();
    }


    /**
     * Verifies the behavior of the {@link CreateConceptController#run} method when executed under the following conditions:
     * <ol>
     *   <li>The logged-in user is an administrator.</li>
     *   <li>A valid {@link Concept} entity is provided for creation.</li>
     * </ol>
     * This test ensures the following outcomes:
     * <ul>
     *   <li>The user form is shown in {@link FormMode#CREATE} mode.</li>
     *   <li>The system successfully creates the {@link Concept} and returns {@link UseCaseResultType#OK}.</li>
     *   <li>No error messages are displayed via the {@link ConceptView}.</li>
     *   <li>A success message indicating successful creation is shown.</li>
     *   <li>An audit log entry is generated for the action.</li>
     * </ul>
     * <div style="border: 1px solid black; padding: 2px;">
     * <strong>Execution note:</strong> Tested by: dvidal@infoyupay.com, passed N tests in Ns at YYYY-MM-dd HH:mm UTC-5
     * </div>
     *
     * @throws CancellationException if the test execution is interrupted while waiting for the operation to complete.
     * @throws CompletionException   if the {@link CreateConceptController#run} method encounters an exception during execution.
     */
    @Test
    void givenAdminAndValidConcept_whenRun_thenCreated() {
        // Arrange
        var admin = performInTransaction(TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);
        var input = Concept.builder()
                .name("Internet")
                .type(ConceptType.RATE)
                .value(new BigDecimal("0.1000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(FormMode.CREATE)).thenReturn(Optional.of(input));
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new CreateConceptController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        verify(view).showUserForm(FormMode.CREATE);
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        assertThat(result.value()).isNotNull();
        verify(view, never()).showError(anyString());
        verify(view, atLeastOnce()).showSuccess(contains("creado exitosamente"));

        assertThat(AuditLogChecker.checkAuditLogExists(result.value().getId(), admin, ctx.getEntityManagerFactory()))
                .isTrue();
    }


    /**
     * Verifies the behavior of the {@link CreateConceptController#run} method under the following conditions:
     * <ol>
     *   <li>The logged-in user is an administrator.</li>
     *   <li>The user opts to cancel the operation while the form is shown in {@link FormMode#CREATE} mode.</li>
     * </ol>
     * This test ensures the following outcomes:
     * <ul>
     *   <li>The user form is shown in {@link FormMode#CREATE} mode.</li>
     *   <li>The result is of type {@link UseCaseResultType#CANCEL}.</li>
     *   <li>No value is returned for the operation.</li>
     *   <li>No success or error messages are shown via {@link ConceptView}.</li>
     *   <li>No audit log entries are generated as a result of the operation.</li>
     * </ul>
     *
     * @throws CancellationException if the test execution is interrupted while awaiting the operation's completion.
     * @throws CompletionException   if the {@link CreateConceptController#run} method encounters an exception during execution.
     */
    @Test
    void givenAdminAndCancel_whenRun_thenCancelled() {
        var admin = performInTransaction(TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(admin);

        view = mock(ConceptView.class);
        when(view.showUserForm(FormMode.CREATE)).thenReturn(Optional.empty());
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new CreateConceptController(ctx);

        var result = controller.run().join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();
        verify(view).showUserForm(FormMode.CREATE);
        verify(view, never()).showError(anyString());
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies the behavior of the {@link CreateConceptController#run} method under the following conditions:
     * <ol>
     *   <li>No user is logged into the system.</li>
     * </ol>
     * This test ensures the following outcomes:
     * <ul>
     *   <li>The result of the operation is {@link UseCaseResultType#ERROR}.</li>
     *   <li>No user form is shown in {@link FormMode} at any stage of the process.</li>
     *   <li>An error message is displayed to inform the absence of a logged-in administrator user.</li>
     *   <li>No success messages are displayed for any part of the process.</li>
     *   <li>No audit log entries are created as a side effect of the operation.</li>
     * </ul>
     *
     * @throws CancellationException if the test execution is interrupted while awaiting the operation's completion.
     * @throws CompletionException   if the {@link CreateConceptController#run} method encounters an exception
     *                               during execution.
     */
    @Test
    void givenNoUser_whenRun_thenErrorAndNoFormShown() {
        var input = Concept.builder()
                .name("Water")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("2.0000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(FormMode.CREATE)).thenReturn(Optional.of(input));
        viewRegistry.registerInstance(ConceptView.class, view);
        ctx.getUserSession().setCurrentUser(null);

        var controller = new CreateConceptController(ctx);

        var result = controller.run().join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view, never()).showUserForm(any());
        verify(view).showError("Inicia sesión con privilegios de ADMIN para poder crear un concepto.");
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies the behavior of the {@link CreateConceptController#run} method under the following conditions:
     * <ol>
     *   <li>The logged-in user is a cashier (non-administrator).</li>
     * </ol>
     * This test ensures the following outcomes:
     * <ul>
     *   <li>The result of the operation is {@link UseCaseResultType#ERROR}.</li>
     *   <li>No user form is shown in {@link FormMode} at any stage of the process.</li>
     *   <li>An error message is displayed, indicating that the user does not have the required privileges.</li>
     *   <li>No success messages are displayed for any part of the process.</li>
     *   <li>No audit log entries are generated as a side effect of the operation.</li>
     * </ul>
     *
     * @throws CancellationException if the test execution is interrupted while awaiting the operation's completion.
     * @throws CompletionException   if the {@link CreateConceptController#run} method encounters an exception
     *                               during execution.
     */
    @Test
    void givenCashierUser_whenRun_thenErrorAndNoFormShown() {
        var cashier = performInTransaction(TestPersistedEntities::persistCashierUser);
        ctx.getUserSession().setCurrentUser(cashier);

        var input = Concept.builder()
                .name("Phone")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("1.0000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(FormMode.CREATE)).thenReturn(Optional.of(input));
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new CreateConceptController(ctx);

        var result = controller.run().join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view, never()).showUserForm(any());
        verify(view).showError(contains("El usuario no tiene privilegios"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }

    /**
     * Verifies the behavior of the {@link CreateConceptController#run} method under the following conditions:
     * <ol>
     *   <li>The logged-in user is an administrator.</li>
     *   <li>A {@link Concept} with a duplicate name already exists in the system.</li>
     * </ol>
     * <p>
     * This test ensures the following outcomes:
     * <ul>
     *   <li>The user form is shown in {@link FormMode#CREATE} mode.</li>
     *   <li>The system returns a result of type {@link UseCaseResultType#ERROR}.</li>
     *   <li>No value is returned for the operation.</li>
     *   <li>An error message is displayed, indicating the failure to create the {@link Concept} due to duplicate name.</li>
     *   <li>No success messages are displayed for any part of the process.</li>
     *   <li>No audit log entries are generated as a result of the operation.</li>
     * </ul>
     *
     * @throws CancellationException if the test execution is interrupted while awaiting the operation's completion.
     * @throws CompletionException   if the {@link CreateConceptController#run} method encounters an exception
     *                               during execution.
     */
    @Test
    void givenDuplicatedConceptName_whenRun_thenError() {
        // Arrange persisted admin and concept
        record Entities(User admin, Concept concept) {
        }
        var persisted = performInTransaction(em -> {
            var user = TestPersistedEntities.persistAdminUser(em);
            var concept = TestPersistedEntities.persistConcept(em);
            return new Entities(user, concept);
        });
        ctx.getUserSession().setCurrentUser(persisted.admin());

        var duplicate = Concept.builder()
                .name(persisted.concept().getName())
                .type(ConceptType.FIXED)
                .value(new BigDecimal("3.0000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(FormMode.CREATE)).thenReturn(Optional.of(duplicate));
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new CreateConceptController(ctx);

        // Act
        var result = controller.run().join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view).showUserForm(FormMode.CREATE);
        verify(view).showError(contains("Error al crear un Concepto"));
        verify(view, never()).showSuccess(anyString());

        assertNoAuditLog();
    }
}
