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

package com.yupay.gangcomisiones.usecase.setglobalconfig;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.UseCaseResultType;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for SetGlobalConfigController covering bootstrap, cancel, permission checks, and validation errors.
 * <ul>
 *   <li>Bootstrap path: requires a logged-in ROOT user and persists an initial global configuration.</li>
 *   <li>Normal update path: ensures only ROOT users can update the global configuration.</li>
 *   <li>Cancel path: simulates a user canceling the form without persisting changes.</li>
 *   <li>Invalid data path: asserts error feedback is shown when data is incomplete or invalid.</li>
 *   <li>View interactions are verified via mocks to ensure correct messages and prompts are displayed.</li>
 * </ul>
 * <p>dvidal@infoyupay.com passed 5 testts in 2sec 231ms at 2025-09-08 08:03 UTC-5</p>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class SetGlobalConfigControllerTest extends AbstractPostgreIntegrationTest {
    private SetGlobalConfigView view;

    /**
     * Resets persistent entities to a clean state for each test.
     * Initializes a mocked view to capture and verify UI interactions.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(AppContext.getInstance().getEntityManagerFactory());
        view = mock(SetGlobalConfigView.class);
    }

    /**
     * Verifies the bootstrap flow succeeds with a logged-in ROOT user and valid input.
     * <ol>
     *   <li>Arrange: persist and authenticate a ROOT user.</li>
     *   <li>Mock the view to return a valid edited GlobalConfig when bootstrapping.</li>
     *   <li>Act: run the controller and join the async result.</li>
     *   <li>Assert: result is OK, the RUC is updated, success message is shown, and no error is displayed.</li>
     * </ol>
     *
     * @throws RuntimeException if the setup transaction fails or the controller execution triggers an unchecked error
     */
    @Test
    void bootstrap_success() {
        // Arrange: create and login a ROOT user (bootstrap requires logged root per prereqs)
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

        var edited = GlobalConfig.builder()
                .ruc("12345678901")
                .legalName("Empresa Legal S.A.")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .announcement("Bienvenidos")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), eq(true))).thenReturn(Optional.of(edited));

        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showSuccess(anyString());
        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showError(anyString());

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                true);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.OK, result.result());
        assertEquals("12345678901", result.value().getRuc());
        verify(view).showSuccess(contains("Configuración global actualizada"));
        verify(view, never()).showError(anyString());
        verify(view).showSetGlobalConfigForm(any(GlobalConfig.class), eq(true));
    }

    /**
     * Validates that canceling the bootstrap form yields a CANCEL result and no side effects.
     * <ol>
     *   <li>Arrange: authenticate a ROOT user.</li>
     *   <li>Mock the view to return an empty Optional on bootstrap.</li>
     *   <li>Act: run the controller and join the async result.</li>
     *   <li>Assert: result is CANCEL and no success or error messages are shown.</li>
     * </ol>
     *
     * @throws RuntimeException if the setup transaction fails or the controller execution triggers an unchecked error
     */
    @Test
    void bootstrap_cancel() {
        // Arrange
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), eq(true))).thenReturn(Optional.empty());

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                true);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.CANCEL, result.result());
        verify(view, never()).showSuccess(anyString());
        verify(view, never()).showError(anyString());
        verify(view).showSetGlobalConfigForm(any(GlobalConfig.class), eq(true));
    }

    /**
     * Ensures that in normal mode a ROOT user can update the global configuration successfully.
     * <ol>
     *   <li>Arrange: authenticate a ROOT user.</li>
     *   <li>Mock the view to return valid edited data with bootstrap flag set to false.</li>
     *   <li>Act: execute the controller.</li>
     *   <li>Assert: result is OK, data is updated, and a success message is shown without errors.</li>
     * </ol>
     *
     * @throws RuntimeException if the setup transaction fails or the controller execution triggers an unchecked error
     */
    @Test
    void normal_rootUser_canUpdate() {
        // Arrange a ROOT user in session
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

        var edited = GlobalConfig.builder()
                .ruc("12345678901")
                .legalName("Empresa Legal S.A.")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .announcement("Bienvenidos")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), eq(false))).thenReturn(Optional.of(edited));

        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showSuccess(anyString());
        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showError(anyString());

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                false);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.OK, result.result());
        assertEquals("12345678901", result.value().getRuc());
        verify(view).showSuccess(contains("Configuración global actualizada"));
        verify(view, never()).showError(anyString());
        verify(view).showSetGlobalConfigForm(any(GlobalConfig.class), eq(false));
    }

    /**
     * Asserts that non-ROOT users are blocked from updating global configuration in normal mode.
     * <ol>
     *   <li>Arrange: authenticate a non-ROOT user.</li>
     *   <li>Act: run the controller.</li>
     *   <li>Assert: result is ERROR, an error message referencing ROOT is shown, and the form is never displayed.</li>
     * </ol>
     *
     * @throws RuntimeException if the setup transaction fails or the controller execution triggers an unchecked error
     */
    @Test
    void normal_nonRootUser_cannotUpdate() {
        // Arrange an ADMIN user (non-ROOT)
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistAdminUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                false);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.ERROR, result.result());
        verify(view).showError(contains("ROOT"));
        verify(view, never()).showSetGlobalConfigForm(any(GlobalConfig.class), anyBoolean());
        verify(view, never()).showSuccess(anyString());
    }

    /**
     * Verifies that validation/persistence errors are surfaced as an error message and error result.
     * <ol>
     *   <li>Arrange: authenticate a ROOT user.</li>
     *   <li>Mock the view to return invalid data (e.g., missing required fields).</li>
     *   <li>Act: run the controller.</li>
     *   <li>Assert: result is ERROR, the form is shown once, success is never shown, and an error message is emitted.</li>
     * </ol>
     *
     * @throws RuntimeException if the setup transaction fails or the controller execution triggers an unchecked error
     */
    @Test
    void normal_errorOnInvalidData_showsError() {
        // Arrange a ROOT user in session
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

        // Missing required fields to trigger persistence exception
        var invalid = GlobalConfig.builder()
                .legalName("No RUC")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        when(view.showSetGlobalConfigForm(any(GlobalConfig.class), eq(false))).thenReturn(Optional.of(invalid));
        doAnswer(inv -> {
            System.out.println(inv.getArgument(0, String.class));
            return null;
        }).when(view).showError(anyString());

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                false);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.ERROR, result.result());
        verify(view).showSetGlobalConfigForm(any(GlobalConfig.class), eq(false));
        verify(view, never()).showSuccess(anyString());
        verify(view).showError(contains("Error actualizando configuración global"));
    }
}
