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
import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService basic operations (create, findById, findByUsername).
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UserServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    private UserService userService;

    /**
     * Sets up the test environment by performing the necessary cleanup and initializing dependencies.
     * <br/>
     * This method is executed before each test case as annotated by {@code @BeforeEach}.
     * It ensures that all persisted entities in the database are cleaned to provide a consistent test state
     * and initializes the {@code userService} instance for use in test methods.
     * <br/>
     * Responsibilities of this method:
     * <ul>
     *  <li> Calls {@code TestPersistedEntities.clean()} to truncate all related database tables and reset in-memory entities.</li>
     *  <li> Retrieves an instance of {@code UserService} through the application context ({@code ctx})
     *      for use in test scenarios.</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        userService = ctx.getUserService();
    }

    /**
     * Tests the successful creation of a user and subsequent retrieval by ID.
     * <br/>
     * This test verifies the following:
     * <ol>
     * <li> A user is successfully created with the specified username, role, and password.</li>
     * <li> The created user can be retrieved using the ID assigned during creation.</li>
     * <li> The retrieved user has the correct username and role.</li>
     * <li> The newly created user is marked as active.</li>
     * </ol>
     * <br/>
     * Steps performed:
     * <ol>
     * <li>Create a user using the `userService.createUser` method.</li>
     * <li>Retrieve the user by ID using the `userService.findById` method.</li>
     * <li>
     * Validate that the retrieved user matches the creation details:
     * <ul>
     * <li>Ensure the user is present.</li>
     * <li>Verify that the username, role, and active status are as expected.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @throws Exception if the test encounters errors during execution
     */
    @Test
    void testCreateUserAndFindById() throws Exception {
        // given
        User created = userService.createUser(
                "john.doe",
                UserRole.ADMIN,
                "superSecret1").get();

        // when
        Optional<User> found = userService.findById(created.getId()).get();

        // then
        assertTrue(found.isPresent(), "User should be found by ID");
        assertEquals("john.doe", found.get().getUsername());
        assertEquals(UserRole.ADMIN, found.get().getRole());
        assertTrue(found.get().getActive(), "User must be active after creation");
    }

    /**
     * Tests the functionality of retrieving a user by username.
     * <br/>
     * This test validates the following:
     * <ul>
     * <li>A user can be successfully created with a specified username, role, and password.</li>
     * <li>The created user can be retrieved using the `findByUsername` method.</li>
     * <li>The retrieved user has the same ID and role as the created user.</li>
     * </ul>
     * <br/>
     * Test Steps:
     * <ol>
     * <li>Create a user using the `userService.createUser` method with the username "alice".</li>
     * <li>Retrieve the user by username using the `userService.findByUsername` method.</li>
     * <li>
     * Verify that:
     * <ul>
     * <li>The retrieved user is present.</li>
     * <li>The ID of the retrieved user matches the ID of the created user.</li>
     * <li>The role of the retrieved user is correct.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @throws Exception if the test encounters any errors during execution
     */
    @Test
    void testFindByUsername() throws Exception {
        // given
        User created = userService.createUser(
                "alice",
                UserRole.CASHIER,
                "password123"
        ).get();

        // when
        Optional<User> found = userService.findByUsername("alice").get();

        // then
        assertTrue(found.isPresent(), "User should be found by username");
        assertEquals(created.getId(), found.get().getId(), "IDs must match");
        assertEquals(UserRole.CASHIER, found.get().getRole());
    }

    /// Tests the successful authentication of a user with valid credentials.
    ///
    /// This test verifies the following:
    /// - A user can be created with a specific username, role, and password.
    /// - The created user can be authenticated using the correct username and password.
    /// - The authenticated user is correctly identified with the expected username.
    ///
    /// Test Steps:
    /// 1. Create a user with the specified username, role, and password using
    ///   the `userService.createUser` method.
    /// 2. Attempt to authenticate the user using the `userService.validateUser` method with
    ///   the correct credentials.
    /// 3. Validate that:
    ///   - Authentication succeeds and the result is present.
    ///   - The authenticated user's username matches the expected username.
    ///
    /// @throws Exception if the test encounters any errors during execution
    @Test
    void testAuthenticateUser_Success() throws Exception {
        // given
        userService.createUser("bob", UserRole.CASHIER, "password123").join();

        // when
        Optional<User> authenticated = userService.validateUser("bob", "password123", null).get();

        // then
        assertTrue(authenticated.isPresent(), "Authentication should succeed");
        assertEquals("bob", authenticated.get().getUsername());
    }

    /// Tests the successful change of a user's password.
    /// This test verifies the following:
    /// - A user is created with a specific username, role, and password.
    /// - The password for the user is successfully updated using the `userService.changePassword` method.
    /// - The updated password allows successful authentication of the user, confirming the change.
    /// Test Steps:
    /// 1. Create a user with a specific username, role, and initial password.
    /// 2. Use the `changePassword` method to update the password.
    /// 3. Attempt to authenticate the user with the updated password using the `validateUser` method.
    /// 4. Verify that:
    ///    - The user can authenticate with the new password.
    ///    - Authentication is successful, and the user is present in the result.
    ///
    /// @throws Exception if any errors occur during execution
    @Test
    void testChangePassword_Success() throws Exception {
        // given
        User created = userService.createUser("carol", UserRole.CASHIER, "oldPass1").get();

        // when
        userService.changePassword(created.getId(), "oldPass1", "newPass1").get();

        // and authenticate with new password
        Optional<User> authenticated = userService.validateUser("carol", "newPass1", null).get();
        assertTrue(authenticated.isPresent(), "Authentication should work with new password");
    }

    /// Tests the successful reset of a user's password by an authorized user with root privileges.
    /// This test validates the following:
    /// - A root user with appropriate credentials can reset the password of another user.
    /// - After the reset, the old password no longer works for authentication.
    /// - The new password allows successful authentication.
    /// Test Steps:
    /// 1. Create a user with a specific username, role, and initial password.
    /// 2. Create a root user with full privileges using a specified password.
    /// 3. Use the `resetPassword` method provided by the `userService` to reset the target user's password.
    /// 4. Verify the following:
    ///    - Authentication with the old password fails.
    ///    - Authentication with the new password succeeds and returns the expected user.
    ///
    /// @throws Exception if any step of the test encounters an error during execution
    @Test
    void testResetPassword_Success() throws Exception {
        var rootPassword = "i'msexyandiknowit";
        var oldPassword = "resetMe!";
        var newPassword = "newPass0rd";
        // given
        User created = userService.createUser("dave", UserRole.CASHIER, oldPassword).get();
        User root = userService.createUser("god", UserRole.ROOT, rootPassword).get();

        // when
        userService.resetPassword(root.getUsername(), rootPassword, created.getUsername(), newPassword).get();

        // old password should fail
        Optional<User> oldAuth = userService.validateUser(created.getUsername(), oldPassword, null).get();
        assertTrue(oldAuth.isEmpty(), "Old password should no longer work");

        // new password should work
        Optional<User> newAuth = userService.validateUser(created.getUsername(), newPassword, null).get();
        assertTrue(newAuth.isPresent(), "New password should work after reset");
    }

    // -------------------------------------------------------------------------
    // Failure cases
    // -------------------------------------------------------------------------

    /// Tests the scenario where a user is not found by a non-existing ID.
    /// This test validates the following:
    /// - The `findById` method correctly returns an empty `Optional` for an ID that does not exist.
    /// - The result of the method call is explicitly verified to ensure no user is present.
    /// Test Steps:
    /// 1. Attempt to retrieve a user by an invalid ID using the `findById` method.
    /// 2. Assert that the returned `Optional` result is empty.
    ///
    /// @throws Exception if any errors occur during the test execution
    @Test
    void testFindById_NotFound() throws Exception {
        Optional<User> result = userService.findById(-9999L).get();
        assertTrue(result.isEmpty(), "No user should be found for a non-existing ID");
    }

    /// Tests the scenario where a user cannot be found by a non-existing username.
    /// This test validates the behavior of the `findByUsername` method when invoked with
    /// a username that does not exist in the system.
    /// Test Steps:
    /// 1. Call the `findByUsername` method with a non-existent username ("ghost").
    /// 2. Verify that the returned `Optional` result is empty.
    /// Expected Outcome:
    /// - The result of the method call should be empty, and no user should be found for the given username.
    ///
    /// @throws Exception if any errors occur during the test execution
    @Test
    void testFindByUsername_NotFound() throws Exception {
        Optional<User> result = userService.findByUsername("ghost").get();
        assertTrue(result.isEmpty(), "No user should be found for a non-existing username");
    }

    /// Tests the behavior of the `createUser` method when attempting to create a user
    /// with a username that already exists in the system.
    /// This test ensures that the system prevents the creation of users with duplicate usernames
    /// by throwing an appropriate exception.
    /// Test Steps:
    /// 1. Create a user with a specific username ("eve"), role, and password.
    /// 2. Attempt to create another user with the same username ("eve") but a different role and password.
    /// 3. Verify that:
    ///    - The method call throws an `ExecutionException`.
    ///    - The cause of the exception is a `PersistenceException`, indicating a conflict due to the duplicate username.
    /// Expected Outcome:
    /// - The system should throw a `PersistenceException` wrapped in an `ExecutionException` to prevent duplicate usernames.
    /// Assertions:
    /// - The thrown exception's cause is an instance of `PersistenceException`.
    @Test
    void testCreateUser_DuplicateUsername() {
        userService.createUser("eve", UserRole.CASHIER, "pwassword01").join();

        ExecutionException ex = assertThrows(
                ExecutionException.class,
                () -> userService.createUser("eve", UserRole.ADMIN, "password02").get()
        );
        assertInstanceOf(PersistenceException.class, ex.getCause(),
                "Should throw PersistenceException due to duplicate username");
    }

    /// Tests the scenario where authentication fails due to an unknown username.
    /// This test validates the following:
    /// - The `validateUser` method returns an empty `Optional` when invoked with a username
    ///   that does not exist in the system.
    /// - No user is authenticated if the username is unrecognized.
    /// Test Steps:
    /// 1. Call the `validateUser` method with a non-existent username ("nobody"), a password ("pw"),
    ///    and a null value for additional parameters.
    /// 2. Verify that the returned result is empty, indicating authentication failure.
    /// Expected Outcome:
    /// - Authentication should fail, and the result of the method call should be an empty `Optional`.
    ///
    /// @throws Exception if any errors occur during test execution
    @Test
    void testAuthenticateUser_UnknownUsername() throws Exception {
        Optional<User> result = userService.validateUser("nobody", "pw", null).get();
        assertTrue(result.isEmpty(), "Authentication should fail for unknown username");
    }

    /// Tests the authentication behavior when the provided password is incorrect.
    /// This method verifies that the `validateUser` method correctly fails to authenticate
    /// a user who provides the wrong password. It ensures that an empty result is returned
    /// when the user credentials are invalid due to an incorrect password.
    ///
    /// @throws Exception if an unexpected error occurs during the test execution
    @Test
    void testAuthenticateUser_WrongPassword() throws Exception {
        userService.createUser("frank", UserRole.CASHIER, "secret00").join();

        Optional<User> result = userService.validateUser("frank", "wrong", null).get();
        assertTrue(result.isEmpty(), "Authentication should fail for wrong password");
    }

    /// Tests the changePassword method of the UserService when attempting to change
    /// the password for a user that does not exist in the system.
    /// This test ensures that the method throws an ExecutionException with a
    /// cause of type PersistenceServicesException, indicating a failure due to
    /// the user not being found in the persistence layer.
    /// The test performs the following validations:
    /// - Verifies that an ExecutionException is thrown when attempting to change
    ///   the password for a non-existing user.
    /// - Confirms that the cause of the exception is a PersistenceServicesException.
    @Test
    void testChangePassword_UserNotFound() {
        var ex = assertThrows(
                ExecutionException.class,
                () -> userService.changePassword(9999L, "pw", "newPw123").get());
        assertInstanceOf(PersistenceServicesException.class, ex.getCause(),
                "Changing password on non-existing user should fail");
    }

    /// Tests the resetPassword functionality of the `UserService` when the specified user does not exist.
    /// This test ensures that an attempt to reset the password for a non-existing user
    /// results in a `PersistenceServicesException`.
    /// Test Steps:
    /// - Create a user with the username "god" and role `UserRole.ROOT`.
    /// - Attempt to reset the password for a non-existing user by providing invalid credentials.
    /// - Assert that an `ExecutionException` is thrown.
    /// - Verify the underlying cause of the exception is `PersistenceServicesException`.
    /// Expected Behavior:
    /// - The reset password operation should throw an exception indicating that the user cannot be found.
    @Test
    void testResetPassword_UserNotFound() {
        var root = userService.createUser("god", UserRole.ROOT, "secret01").join();
        ExecutionException ex = assertThrows(
                ExecutionException.class,
                () -> userService.resetPassword(root.getUsername(), "secret01",
                        "r3roiuh", "f08e3h0pqh0pigh0peihgo").get()
        );
        assertInstanceOf(PersistenceServicesException.class, ex.getCause(),
                "Should throw PersistenceServicesException for reset on non-existing user");
    }
}

