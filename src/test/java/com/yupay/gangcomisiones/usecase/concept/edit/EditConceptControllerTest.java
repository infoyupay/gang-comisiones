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

package com.yupay.gangcomisiones.usecase.concept.edit;

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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link EditConceptController}, covering the "Edit Concept" flow
 * under success, cancellation, and error scenarios.<br/>
 * Differences from the create flow:
 * <ul>
 *   <li>A pre-existing {@link Concept} must be created and provided to the controller.</li>
 *   <li>
 *       The {@link ConceptView} receives the entity in {@link FormMode#EDIT}, modifies it,
 *       and returns the edited instance.
 *   </li>
 *   <li>
 *       The controller then coordinates validation, privilege checks, UI interaction,
 *       and persistence through the service layer.
 *   </li>
 * </ul>
 *  <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 5 tests in 2.241s at 2025-09-29 22:41 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class EditConceptControllerTest extends AbstractPostgreIntegrationTest {
    final AtomicReference<Concept> conceptRef = new AtomicReference<>();
    ConceptView view;

    /**
     * Initializes the test fixture before each test execution.
     * <ul>
     *   <li>Cleans persisted entities to start from a known, isolated state.</li>
     * </ul>
     */
    @BeforeEach
    void prepare() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Cleans resources and resets test environment after each test execution.
     * <ul>
     *   <li>Unregisters the {@link ConceptView} from the registry if present.</li>
     *   <li>Clears references to mocks and test data.</li>
     *   <li>Resets the current user in the session.</li>
     * </ul>
     */
    @AfterEach
    void cleanUp() {
        if (viewRegistry.isRegistered(ConceptView.class)) {
            viewRegistry.unregister(ConceptView.class);
        }
        view = null;
        conceptRef.set(null);
        ctx.getUserSession().setCurrentUser(null);
    }

    /**
     * Verifies a successful edit by an authenticated ADMIN user. It expects:
     * <ul>
     *   <li>Form is shown in {@link FormMode#EDIT}.</li>
     *   <li>Result is {@link UseCaseResultType#OK} with a non-null value.</li>
     *   <li>A success message indicating the concept was updated.</li>
     *   <li>An audit log entry associated to the editor and the updated entity.</li>
     * </ul>
     */
    @Test
    void givenAdminAndValidEdit_whenRun_thenUpdated() {
        // Arrange persisted admin and concept
        record Entities(User admin, Concept concept) {
        }
        var persisted = performInTransaction(em -> {
            var admin = TestPersistedEntities.persistAdminUser(em);
            var concept = TestPersistedEntities.persistConcept(em);
            return new Entities(admin, concept);
        });
        ctx.getUserSession().setCurrentUser(persisted.admin());

        // User edits name and value
        var edited = Concept.builder()
                .id(persisted.concept().getId())
                .name(persisted.concept().getName() + " v2")
                .type(ConceptType.FIXED)
                .value(new BigDecimal("20.0000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(any(Concept.class), eq(FormMode.EDIT))).thenReturn(Optional.of(edited));
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new EditConceptController(ctx);

        // Act
        var result = controller.run(persisted.concept()).join();

        // Assert
        assertThat(result.result()).isEqualTo(UseCaseResultType.OK);
        assertThat(result.value()).isNotNull();
        verify(view).showUserForm(any(Concept.class), eq(FormMode.EDIT));
        verify(view, never()).showError(anyString());
        verify(view, atLeastOnce()).showSuccess(contains("actualizado exitosamente"));

        assertThat(AuditLogChecker.checkAuditLogExists(result.value().getId(), persisted.admin(),
                ctx.getEntityManagerFactory())).isTrue();
    }

    /**
     * Verifies cancellation by the user. It expects:
     * <ul>
     *   <li>Form is shown in {@link FormMode#EDIT} and returns empty.</li>
     *   <li>Result is {@link UseCaseResultType#CANCEL}.</li>
     *   <li>No success or error messages.</li>
     * </ul>
     */
    @Test
    void givenAdminAndCancel_whenRun_thenCancelled() {
        var admin = performInTransaction(TestPersistedEntities::persistAdminUser);
        var concept = performInTransaction(TestPersistedEntities::persistConcept);
        ctx.getUserSession().setCurrentUser(admin);

        view = mock(ConceptView.class);
        when(view.showUserForm(any(Concept.class), eq(FormMode.EDIT))).thenReturn(Optional.empty());
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new EditConceptController(ctx);
        var result = controller.run(concept).join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.CANCEL);
        assertThat(result.value()).isNull();
        verify(view).showUserForm(any(Concept.class), eq(FormMode.EDIT));
        verify(view, never()).showError(anyString());
        verify(view, never()).showSuccess(anyString());

        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory())).isFalse();
    }

    /**
     * Verifies that when no user is authenticated the controller does not open the form and returns
     * {@link UseCaseResultType#ERROR}, showing an informative error to the user.
     */
    @Test
    void givenNoUser_whenRun_thenErrorAndNoFormShown() {
        var concept = performInTransaction(TestPersistedEntities::persistConcept);
        ctx.getUserSession().setCurrentUser(null);

        view = mock(ConceptView.class);
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new EditConceptController(ctx);
        var result = controller.run(concept).join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view, never()).showUserForm(any(), any());
        verify(view).showError("Inicia sesión con privilegios de ADMIN para poder editar un concepto.");
        verify(view, never()).showSuccess(anyString());

        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory())).isFalse();
    }

    /**
     * Verifies that a non-ADMIN user cannot perform the edit action. The form is not shown
     * and an error is presented to the user.
     */
    @Test
    void givenCashierUser_whenRun_thenErrorAndNoFormShown() {
        var cashier = performInTransaction(TestPersistedEntities::persistCashierUser);
        var concept = performInTransaction(TestPersistedEntities::persistConcept);
        ctx.getUserSession().setCurrentUser(cashier);

        view = mock(ConceptView.class);
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new EditConceptController(ctx);
        var result = controller.run(concept).join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view, never()).showUserForm(any(), any());
        verify(view).showError(contains("El usuario no tiene privilegios"));
        verify(view, never()).showSuccess(anyString());

        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory())).isFalse();
    }

    /**
     * Verifies that a persistence error while updating (e.g., duplicate name) surfaces as
     * {@link UseCaseResultType#ERROR}, displaying an error message and not producing audit logs.
     */
    @Test
    void givenDuplicatedConceptName_whenRun_thenError() {
        // Arrange admin and two concepts with different names
        record Entities(User admin, Concept c1, Concept c2) {
        }
        var persisted = performInTransaction(em -> {
            var admin = TestPersistedEntities.persistAdminUser(em);
            var c1 = TestPersistedEntities.persistConcept(em);
            // create a second different concept
            var c2 = Concept.builder()
                    .active(true)
                    .name("Network Fee")
                    .type(ConceptType.FIXED)
                    .value(new BigDecimal("5.0000"))
                    .build();
            em.persist(c2);
            return new Entities(admin, c1, c2);
        });
        ctx.getUserSession().setCurrentUser(persisted.admin());

        // Try to rename c1 to c2's name
        var edited = Concept.builder()
                .id(persisted.c1().getId())
                .name(persisted.c2().getName())
                .type(ConceptType.FIXED)
                .value(new BigDecimal("7.0000"))
                .active(true)
                .build();

        view = mock(ConceptView.class);
        when(view.showUserForm(any(Concept.class), eq(FormMode.EDIT))).thenReturn(Optional.of(edited));
        viewRegistry.registerInstance(ConceptView.class, view);

        var controller = new EditConceptController(ctx);
        var result = controller.run(persisted.c1()).join();

        assertThat(result.result()).isEqualTo(UseCaseResultType.ERROR);
        assertThat(result.value()).isNull();
        verify(view).showUserForm(any(Concept.class), eq(FormMode.EDIT));
        verify(view).showError(contains("Error al actualizar un Concepto"));
        verify(view, never()).showSuccess(anyString());

        assertThat(AuditLogChecker.checkAnyAuditLogExists(ctx.getEntityManagerFactory())).isFalse();
    }
}
