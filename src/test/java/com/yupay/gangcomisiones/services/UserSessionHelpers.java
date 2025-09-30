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

import com.yupay.gangcomisiones.AppContext;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;

import static com.yupay.gangcomisiones.model.TestPersistedEntities.*;

/**
 * Helper class to create and log-in a user for each access level.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class UserSessionHelpers {
    /**
     * Creates an admin user with a predefined configuration, sets their role and credentials,
     * and logs them into the current session.
     * <br/>
     * This method ensures that the new admin user is created and automatically set as the
     * active user in the provided application context.
     *
     * @param ctx the application context responsible for managing user sessions and interactions; must not be {@code null}.
     * @param em  the entity manager for handling persistence operations required during user creation; must not be {@code null}.
     */
    public static void createAndLogAdminUser(@NotNull AppContext ctx,
                                             @NotNull EntityManager em) {
        ctx.getUserSession().setCurrentUser(persistAdminUser(em));
    }

    /**
     * Creates a cashier user, persistently saves them to the database, and logs them into the current session.
     * <br/>
     * The method sets the new cashier user as the active user for the provided application context.
     * <br/>
     * This operation is performed using the provided entity manager for persistence interaction.
     *
     * @param ctx the application context responsible for user session management; must not be {@code null}.
     *            <br/>
     *            <ul>
     *              <li>Handles the active user session for the lifecycle of the application.</li>
     *              <li>Ensures proper assignment of the logged-in user.</li>
     *            </ul>
     * @param em  the entity manager used for persisting the new cashier user; must not be {@code null}.
     *            <br/>
     *            <ul>
     *              <li>Facilitates database operations such as saving the user entity.</li>
     *            </ul>
     */
    public static void createAndLogCashierUser(@NotNull AppContext ctx,
                                               @NotNull EntityManager em) {
        ctx.getUserSession().setCurrentUser(persistCashierUser(em));
    }

    /**
     * Creates a root user with default settings, persists them into the database, and logs them into the current session.
     * <br/>
     * The method sets the newly created root user as the active user within the provided application context.
     * <br/>
     * This operation leverages the provided {@link EntityManager} for persistence tasks.
     *
     * @param ctx the application context used to manage user sessions and scope; must not be {@code null}.
     *            <br/>
     *            <ul>
     *              <li>Responsible for the lifecycle management of the active user session.</li>
     *              <li>Ensures proper user assignment into the session.</li>
     *            </ul>
     * @param em  the entity manager used to perform persistence operations for saving the root user; must not be {@code null}.
     *            <br/>
     *            <ul>
     *              <li>Enables interaction with the persistence layer for database-related tasks.</li>
     *            </ul>
     */
    public static void createAndLogRootUser(@NotNull AppContext ctx,
                                            @NotNull EntityManager em) {
        ctx.getUserSession().setCurrentUser(persistRootUser(em));
    }


}
