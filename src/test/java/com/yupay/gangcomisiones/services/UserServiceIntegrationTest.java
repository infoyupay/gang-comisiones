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
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.function.Consumer;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static com.yupay.gangcomisiones.services.UserSessionHelpers.createAndLogCashierUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Integration tests for UserService basic operations (create, findById, findByUsername).<br/>
 * <div style="border: 1px solid black; padding: 2px">
 *    <strong>Execution Note:</strong> dvidal@infoyupay.com passed 12 tests in 4.354s at 2025-09-29 21:54 UTC-5.
 * </div>
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
     * Cleans up resources and resets the state after each test execution.
     * <br/>
     * This method is annotated with {@code @AfterEach}, ensuring that it runs after each test method in the current test class.
     * <br/><br/>
     * The method performs the following operations:
     * <ul>
     *   <li>Resets the {@code userService} reference to {@code null}.</li>
     *   <li>Logs out the current user session by invoking {@code logout()} on {@code ctx.getUserSession()}.</li>
     * </ul>
     */
    @AfterEach
    void cleanUp() {
        userService = null;
        ctx.getUserSession().logout();
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
        var created = performInTransaction(TestPersistedEntities::persistAdminUser);

        // when
        var found = userService.findById(created.getId()).get();

        // then
        assertThat(found).hasValueSatisfying(f -> assertSoftly(assertUserDeeplyEquals(f, created)));
    }

    /**
     * A method that generates a {@link Consumer} to perform deep equality checks between two {@link User} objects
     * using soft assertions. It compares several attributes of the {@code actual} and {@code expected} {@link User}
     * instances to ensure consistency, while ensuring the assertion process does not stop at the first mismatch.
     * <br/><br/>
     * The following attributes are verified:
     * <ol>
     *   <li><strong>Username:</strong> Verifies that the username of both users matches.</li>
     *   <li><strong>Role:</strong> Verifies that the role of both users matches.</li>
     *   <li><strong>Active Status:</strong> Ensures that the actual user is marked as active.</li>
     *   <li><strong>ID:</strong> Verifies that the IDs of both users match.</li>
     * </ol>
     *
     * @param actual   the {@link User} instance that represents the actual user to be validated.
     * @param expected the {@link User} instance that serves as the expected reference user for validation.
     * @return a {@link Consumer} of {@link SoftAssertions} that performs the deep equality checks between the provided users.
     */
    Consumer<SoftAssertions> assertUserDeeplyEquals(User actual, User expected) {
        return softly -> {
            softly.assertThat(actual.getUsername())
                    .as("username must match")
                    .isEqualTo(expected.getUsername());

            softly.assertThat(actual.getRole())
                    .as("role must match")
                    .isEqualTo(expected.getRole());

            softly.assertThat(actual.getActive())
                    .as("user must be active")
                    .isTrue();

            softly.assertThat(actual.getId())
                    .as("id must match")
                    .isEqualTo(expected.getId());
        };
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
        var created = performInTransaction(TestPersistedEntities::persistAdminUser);

        // when
        var found = userService.findByUsername(created.getUsername()).get();

        // then
        assertThat(found)
                .hasValueSatisfying(v -> assertSoftly(assertUserDeeplyEquals(v, created)));
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
        userService.createUser("bob", UserRole.CASHIER, "password123").get();

        // when
        var authenticated = userService.validateUser("bob", "password123", null).get();

        // then
        assertThat(authenticated)
                .hasValueSatisfying(u -> assertThat(u.getUsername()).isEqualTo("bob"));
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
        runInTransaction(em -> createAndLogCashierUser(ctx, em));
        // given
        var created = userService.createUser("carol", UserRole.CASHIER, "oldPass1").get();

        // when
        userService.changePassword(created.getId(), "oldPass1", "newPass1").get();

        // and authenticate with new password
        var authenticated = userService.validateUser("carol", "newPass1", null).get();
        assertThat(authenticated).isPresent();
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
        var rootPassword = "password";
        var oldPassword = "resetMe!";
        var newPassword = "newPass0rd";
        // given
        var root = userService.createUser("god", UserRole.ROOT, rootPassword).get();
        ctx.getUserSession().setCurrentUser(root);
        var created = userService.createUser("dave", UserRole.CASHIER, oldPassword).get();

        // when
        userService.resetPassword(root.getUsername(), rootPassword, created.getUsername(), newPassword).get();

        // old password should fail
        var oldAuth = userService.validateUser(created.getUsername(), oldPassword, null).get();
        assertThat(oldAuth).as("Old password should no longer work.").isEmpty();

        // new password should work
        var newAuth = userService.validateUser(created.getUsername(), newPassword, null).get();
        assertThat(newAuth).as("New password should work after reset.").isPresent();
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
        var result = userService.findById(-9999L).get();
        assertThat(result).as("No user should be found for a non-existing ID").isEmpty();
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
        var result = userService.findByUsername("ghost").get();
        assertThat(result).as("No user should be found for a non-existing username").isEmpty();
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

        var ex = catchThrowable(
                () -> userService.createUser("eve", UserRole.ADMIN, "password02").get()
        );
        assertExpectedCause(SQLException.class).assertCauseWithMessage(ex, "uq_user_username");
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
        var result = userService.validateUser("nobody", "pw", null).get();
        assertThat(result).as("Authentication should fail for unknown username").isEmpty();
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

        var result = userService.validateUser("frank", "wrong", null).get();
        assertThat(result).as("Authentication should fail for unknown username").isEmpty();
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
        var ex = catchThrowable(
                () -> userService.changePassword(9999L, "pw", "newPw123").get());
        assertExpectedCause(PersistenceServicesException.class)
                .assertCauseWithMessage(ex, "User not found by ID");
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
        var ex = catchThrowable(
                () -> userService.resetPassword(root.getUsername(), "secret01",
                        "r3roiuh", "f08e3h0pqh0pigh0peihgo").get()
        );
        assertExpectedCause(PersistenceServicesException.class)
                .assertCauseWithMessage(ex, "User not found by username");
    }
}

