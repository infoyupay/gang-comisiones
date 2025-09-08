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

import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Services for {@link User} entities.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface UserService {
    /**
     * Asynchronously creates a new user with the given username, role, and plain password.
     *
     * @param username      The username of the new user.
     * @param role          The role of the new user.
     * @param plainPassword The plain password of the new user.
     * @return A CompletableFuture that completes with the newly created user.
     */
    CompletableFuture<User> createUser(String username, UserRole role, String plainPassword);

    /**
     * Creates a new user synchronously with the given username, role, and plain password.
     *
     * @param username      The username of the new user.
     * @param role          The role to assign to the new user.
     * @param plainPassword The plain text password for the new user.
     * @return The newly created User object.
     */
    User createUserSync(String username, UserRole role, String plainPassword);

    /**
     * Changes the password of the user with the given ID.
     *
     * @param userId      The ID of the user whose password should be changed.
     * @param newPassword The new password for the user.
     * @param oldPassword The current password for the user.
     * @return A CompletableFuture that completes when the password change is complete.
     */
    CompletableFuture<Void> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Finds a user by their ID.
     *
     * @param userId The ID of the user to find.
     * @return A CompletableFuture that completes with an Optional containing the user
     * if found, or an empty Optional if not found.
     */
    CompletableFuture<Optional<User>> findById(Long userId);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find.
     * @return A CompletableFuture that completes with an Optional containing the user
     * if found, or an empty Optional if not found.
     */
    CompletableFuture<Optional<User>> findByUsername(String username);

    /**
     * Lists all users.
     *
     * @return A CompletableFuture that completes with a list of all users.
     */
    CompletableFuture<List<User>> listAllUsers();

    /**
     * Validates a user by their username and password for a certain role.
     *
     * @param username The username of the user to validate.
     * @param password The password of the user to validate.
     * @param role     The role of the user to validate.
     * @return A CompletableFuture that completes with an Optional containing the user
     * if found and the password and role match, or an empty Optional if not found or
     * the password or role does not match.
     */
    CompletableFuture<Optional<User>> validateUser(@NotNull String username,
                                                   @NotNull String password,
                                                   @Nullable UserRole role);

    /**
     * Resets the password of a user. A root user must be authenticated first.
     *
     * @param rootUsername The username of the root user.
     * @param rootPassword The password of the root user.
     * @param username     The username of the user to reset the password for.
     * @param newPassword  The new password for the user.
     * @return A CompletableFuture that completes when the password has been reset.
     */
    CompletableFuture<Void> resetPassword(@NotNull String rootUsername,
                                          @NotNull String rootPassword,
                                          @NotNull String username,
                                          @NotNull String newPassword);

    /**
     * Checks if a user is still valid in database for at least the given role. If not, throws a runtime exception.
     *
     * @param em   The EntityManager to use for database operations.
     * @param id   The id of the user to check.
     * @param role The minimum required role level for user.
     * @return a fresh User object from database.
     */
    @NotNull User checkPrivilegesOrException(@NotNull EntityManager em, long id, @NotNull UserRole role);

    /**
     * Checks if there is at least one user in the database.
     *
     * @return A CompletableFuture that completes with true if there is at least one user, false otherwise.
     */
    CompletableFuture<Boolean> isAnyUser();
}
