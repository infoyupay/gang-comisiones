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
import com.yupay.gangcomisiones.TestViews;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for SetGlobalConfigController covering bootstrap, cancel, permission checks, and validation errors.
 * <ul>
 *   <li>Bootstrap path: requires a logged-in ROOT user and persists an initial global configuration.</li>
 *   <li>Normal update path: ensures only ROOT users can update the global configuration.</li>
 *   <li>Cancel path: simulates a user canceling the form without persisting changes.</li>
 *   <li>Invalid data path: asserts error feedback is shown when data is incomplete or invalid.</li>
 *   <li>View interactions are verified via mocks to ensure correct messages and prompts are displayed.</li>
 * </ul>
 * <div style="border: 1px solid black; padding: 1px;">
 * <b>Execution note:</b> dvidal@infoyupay.com passed 5 tests in 1.941s at 2025-09-11 12:07 UTC-5.
 * </div>
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
    }

    /**
     * Cleans up resources after each test.
     */
    @AfterEach
    void cleanUp() {
        view = null;
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
        var currentUser = performInTransaction(TestPersistedEntities::persistRootUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var edited = GlobalConfig.builder()
                .ruc("12345678901")
                .legalName("Empresa Legal S.A.")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .announcement("Bienvenidos")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        var view = TestViews.setGlobalConfigView(edited);

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                true);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.OK, result.result());
        var value = result.value();
        assertNotNull(value);
        assertEquals("12345678901", value.getRuc());
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
        // Arrange: create and login a ROOT user (bootstrap requires logged root per prereqs)
        // Arrange: create and login a ROOT user (bootstrap requires logged root per prereqs)
        var currentUser = performInTransaction(TestPersistedEntities::persistRootUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var view = TestViews.setGlobalConfigView(null);

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
        // Arrange: create and login a ROOT user (bootstrap requires logged root per prereqs)
        var currentUser = performInTransaction(TestPersistedEntities::persistRootUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var edited = GlobalConfig.builder()
                .ruc("12345678901")
                .legalName("Empresa Legal S.A.")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .announcement("Bienvenidos")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        var view = TestViews.setGlobalConfigView(edited);

        var controller = new SetGlobalConfigController(view,
                ctx.getUserService(),
                ctx.getGlobalConfigService(),
                false);

        // Act
        var result = controller.run().join();

        // Assert
        assertEquals(UseCaseResultType.OK, result.result());
        var value = result.value();
        assertNotNull(value);
        assertEquals("12345678901", value.getRuc());
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
        // Arrange: create and login a ROOT user (bootstrap requires logged root per prereqs)
        var currentUser = performInTransaction(TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var view = TestViews.setGlobalConfigView(null);

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
       var currentUser =  performInTransaction(TestPersistedEntities::persistRootUser);
       ctx.getUserSession().setCurrentUser(currentUser);

        // Missing required fields to trigger persistence exception
        var invalid = GlobalConfig.builder()
                .legalName("No RUC")
                .businessName("Mi Empresa")
                .address("Calle Falsa 123")
                .updatedBy(currentUser)
                .updatedFrom("test-host")
                .build();
        view = TestViews.setGlobalConfigView(invalid);

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
