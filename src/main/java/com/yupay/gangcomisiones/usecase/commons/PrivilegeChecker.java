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

package com.yupay.gangcomisiones.usecase.commons;

import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.UserService;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling privilege checks for users.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class PrivilegeChecker {
    private static final Logger LOG = LoggerFactory.getLogger(PrivilegeChecker.class);

    /**
     * Checks if a user has the specified privilege and handles errors appropriately.
     *
     * @param emf         The {@code EntityManagerFactory} used to create an {@code EntityManager}
     *                    for database operations.
     * @param userService The {@code UserService} used to verify the user's privileges.
     * @param user        The {@code User} whose privileges will be checked.
     * @param privilege   The {@code UserRole} privilege to check for the user.
     * @param onError     The {@code ErrorPresenter} used to handle and display errors when the user lacks privileges
     *                    or a runtime error occurs.
     * @return {@code true} if the user has the specified privilege, {@code false} if the privilege check fails.
     * @throws RuntimeException if an unexpected error occurs during the privilege check.
     */
    public static boolean checkPrivileges(@NotNull EntityManagerFactory emf,
                                          @NotNull UserService userService,
                                          @NotNull User user,
                                          @NotNull UserRole privilege,
                                          @NotNull ErrorPresenter onError) {
        try (var em = emf.createEntityManager()) {
            userService.checkPrivilegesOrException(em, user.getId(), privilege);
            return true;
        } catch (GangComisionesException e) {
            onError.showError("El usuario no tiene privilegios de %s.%n%s".formatted(privilege, e.getMessage()));
            return false;
        } catch (RuntimeException e) {
            onError.showError("No se pudieron verificar los privilegios del usuario.");
            throw e;
        }
    }

    /**
     * Checks if the given user is still active.
     *
     * @param userService The {@link  UserService} used to verify the user's active status.
     * @param user        The {@link  User} whose active status is to be checked.
     * @param onError     The {@link  ErrorPresenter} used to handle and display errors that occur during the check.
     * @return {@code true} if the user is still active, {@code false} otherwise.
     * @throws RuntimeException if an unexpected error occurs during the active status check.
     */
    public static boolean isUserStillActive(@NotNull UserService userService,
                                            @NotNull User user,
                                            @NotNull ErrorPresenter onError) {
        try {
            return userService.checkUserCurrentlyActive(user.getId());
        } catch (RuntimeException e) {
            onError.showError("No se pudo verificar si el usuario está activo.\n" + e.getMessage());
            LOG.error("Error al verificar si el usuario está activo", e);
            throw e;
        }
    }
}
