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

package com.yupay.gangcomisiones.usecase.createuser;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link CreateUserController}, validating the user creation flow
 * across bootstrap and normal modes with different privilege scenarios and error handling.
 * <br/>
 * This test class focuses on:
 * <ul>
 *   <li>Bootstrap mode synchronous user creation.</li>
 *   <li>Cancellation flow when user input is not provided.</li>
 *   <li>Normal mode creation with ROOT-privileged users.</li>
 *   <li>Permission denial for non-ROOT users attempting creation.</li>
 *   <li>Error propagation on duplicate usernames.</li>
 * </ul>
 * <div style="border: 1px solid black; padding: 1px;">
 * <b>Execution note:</b> dvidal@infoyupay.com passed 5 tests in 2.471s at 2025-09-11 11:35 UTC-5
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class CreateUserControllerTest extends AbstractPostgreIntegrationTest {
    private CreateUserView view;

    /**
     * Initializes the test fixture before each test execution.
     * <ul>
     *   <li>Cleans persisted entities to start from a known state.</li>
     *   <li>Creates a mock view to observe controller interactions.</li>
     * </ul>
     *
     * @throws RuntimeException if the persistence context cannot be cleaned
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(AppContext.getInstance().getEntityManagerFactory());
        view = mock(CreateUserView.class);
        viewRegistry.registerInstance(CreateUserView.class, view);
    }

    /// Cleans up the test environment by unregistering the `CreateUserView`
    /// instance from the `viewRegistry`, if it is registered. This ensures
    /// that the state of the registry is reset after each test execution.
    ///
    /// Preconditions:
    /// - `viewRegistry` must be non-null.
    /// - `viewRegistry.isRegistered(Class<?>)` is expected to return `true`
    /// if the specified component is already registered.
    ///
    /// Postconditions:
    /// - If the `CreateUserView` was registered, it will be unregistered.
    /// - If the `CreateUserView` was not registered, there will be no effect.
    @AfterEach
    void cleanRegistry() {
        if (viewRegistry != null && viewRegistry.isRegistered(CreateUserView.class)) {
            viewRegistry.unregister(CreateUserView.class);
        }
    }

    /**
     * Verifies that in bootstrap mode the controller creates a user synchronously and reports success.
     * <ul>
     *   <li>Shows the create user form in bootstrap mode.</li>
     *   <li>Invokes the synchronous creation path.</li>
     *   <li>Asserts OK result and success message.</li>
     * </ul>
     *
     * @throws RuntimeException if the controller fails unexpectedly during execution
     */
    @Test
    void bootstrap_createsUserSynchronously() {
        var dto = new CreateUserDTO("rootUser", "password", UserRole.ROOT);
        when(view.showCreateUserForm(true)).thenReturn(Optional.of(dto));
        Answer<?> mockPrintLn = invocation -> {
            System.out.println(invocation.getArgument(0, String.class));
            return null;
        };
        doAnswer(mockPrintLn).when(view).showSuccess(anyString());
        doAnswer(mockPrintLn).when(view).showError(anyString());

        var controller = new CreateUserController(view, ctx.getUserService(), true);

        var result = controller.run().join();

        assertEquals(UseCaseResultType.OK, result.result());
        var value = result.value();
        assertNotNull(value);
        assertEquals("rootUser", value.getUsername());
        verify(view).showSuccess(contains("rootUser"));
        verify(view, never()).showError(anyString());
        verify(view).showCreateUserForm(true);
    }

    /**
     * Verifies that in bootstrap mode the user can cancel the operation
     * and the controller returns a CANCEL result without showing success or error messages.
     * <ul>
     *   <li>Simulates empty input from the create user form.</li>
     *   <li>Asserts CANCEL result and no success/error interactions.</li>
     * </ul>
     */
    @Test
    void bootstrap_cancel() {
        when(view.showCreateUserForm(true)).thenReturn(Optional.empty());
        var controller = new CreateUserController(view, ctx.getUserService(), true);
        var result = controller.run().join();

        assertEquals(UseCaseResultType.CANCEL, result.result());
        verify(view, never()).showSuccess(anyString());
        verify(view, never()).showError(anyString());
        verify(view).showCreateUserForm(true);
    }

    /**
     * Verifies that in normal mode a ROOT user can create another user successfully.
     * <ul>
     *   <li>Persists a ROOT user and sets it as the current session user.</li>
     *   <li>Submits a valid DTO through the view.</li>
     *   <li>Asserts OK result and success message with created username.</li>
     * </ul>
     *
     * @throws RuntimeException if database operations or controller execution fail unexpectedly
     */
    @Test
    void normal_rootUser_canCreate() {
        User currentUser = performInTransaction(ctx, TestPersistedEntities::persistRootUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var dto = new CreateUserDTO("cashier", "12345678", UserRole.CASHIER);
        when(view.showCreateUserForm(false)).thenReturn(Optional.of(dto));
        Answer<?> mockPrintLn = invocation -> {
            System.out.println(invocation.getArgument(0, String.class));
            return null;
        };
        doAnswer(mockPrintLn).when(view).showSuccess(anyString());
        doAnswer(mockPrintLn).when(view).showError(anyString());

        var controller = new CreateUserController(view, ctx.getUserService(), false);

        var result = controller.run().join();

        assertEquals(UseCaseResultType.OK, result.result());
        var value = result.value();
        assertNotNull(value);
        assertEquals("cashier", value.getUsername());
        verify(view).showSuccess(contains("cashier"));
        verify(view, never()).showError(anyString());
        verify(view).showCreateUserForm(false);
    }

    /**
     * Verifies that in normal mode a non-ROOT user is not allowed to create users.
     * <ul>
     *   <li>Persists an ADMIN user and sets it as the current session user.</li>
     *   <li>Asserts ERROR result and that the view shows a ROOT-privilege error.</li>
     *   <li>Ensures the create user form is not displayed.</li>
     * </ul>
     *
     * @throws RuntimeException if database operations or controller execution fail unexpectedly
     */
    @Test
    void normal_nonRootUser_cannotCreate() {
        User currentUser = performInTransaction(ctx, TestPersistedEntities::persistAdminUser);
        ctx.getUserSession().setCurrentUser(currentUser);

        var controller = new CreateUserController(view, ctx.getUserService(), false);

        var result = controller.run().join();

        assertEquals(UseCaseResultType.ERROR, result.result());
        verify(view).showError(contains("ROOT"));
        verify(view, never()).showCreateUserForm(anyBoolean());
        verify(view, never()).showSuccess(anyString());
    }

    /**
     * Verifies that attempting to create a user with a duplicate username results in an error.
     * <ul>
     *   <li>Persists a ROOT user and a conflicting existing user.</li>
     *   <li>Submits a DTO with the duplicate username.</li>
     *   <li>Asserts ERROR result and that an appropriate error message is shown.</li>
     * </ul>
     *
     * @throws RuntimeException if database operations or controller execution fail unexpectedly
     */
    @Test
    void normal_errorOnDuplicateUsername() {
        var current = performInTransaction(ctx, em -> {
            var _current = TestPersistedEntities.persistRootUser(em);
            em.persist(User.builder()
                    .username("dupUser")
                    .password("abcd1234")
                    .role(UserRole.CASHIER)
                    .active(true)
                    .build());
            return _current;
        });
        ctx.getUserSession().setCurrentUser(current);

        var dto = new CreateUserDTO("dupUser", "1234ABCD", UserRole.CASHIER);
        when(view.showCreateUserForm(false)).thenReturn(Optional.of(dto));
        Answer<?> mockPrintLn = invocation -> {
            System.out.println(invocation.getArgument(0, String.class));
            return null;
        };
        doAnswer(mockPrintLn).when(view).showError(anyString());

        var controller = new CreateUserController(view, ctx.getUserService(), false);

        var result = controller.run().join();

        assertEquals(UseCaseResultType.ERROR, result.result());
        verify(view).showCreateUserForm(false);
        verify(view, never()).showSuccess(anyString());
        verify(view).showError(contains("Error creando usuario"));
    }
}
