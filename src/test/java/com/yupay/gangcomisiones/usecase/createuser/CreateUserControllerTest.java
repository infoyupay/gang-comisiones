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
import com.yupay.gangcomisiones.usecase.UseCaseResultType;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Execution note: dvidal@infoyupay.com passed the 5 tests in 2 sec 611 ms at 2025-09-08 00:42:00 UTC-5.
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
        assertEquals("rootUser", result.value().getUsername());
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
        User currentUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
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
        assertEquals("cashier", result.value().getUsername());
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
        User currentUser, dupUser;
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            currentUser = TestPersistedEntities.persistRootUser(em);
            dupUser = User.builder()
                    .username("dupUser")
                    .password("abcd1234")
                    .role(UserRole.CASHIER)
                    .active(true)
                    .build();
            em.persist(dupUser);
            tx.commit();
            ctx.getUserSession().setCurrentUser(currentUser);
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }

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
