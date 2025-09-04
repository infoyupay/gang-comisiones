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
import com.yupay.gangcomisiones.model.UserRole;

/**
 * Helper class to create and log-in a user for each access level.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class UserSessionHelpers {
    /**
     * Creates an admin user with a mock username, sets their password,
     * and logs them into the current session.
     * <br/>
     * This method interacts with the application's user service to register a new user
     * with administrative privileges and establishes the newly created user as the active
     * user in the session.
     */
    public static void createAndLogAdminUser() {
        var r = AppContext.getInstance()
                .getUserService()
                .createUser("john.doe-admin", UserRole.ADMIN, "password")
                .join();
        AppContext.getInstance().getUserSession().setCurrentUser(r);
    }

    /**
     * Creates a cashier user with a mock username and role, sets their password,
     * and logs them into the current session.
     * <br/>
     * This method interacts with the application's user service to register a new user
     * with administrative privileges and establishes the newly created user as the active
     * user in the session.
     */
    public static void createAndLogCashierUser() {
        var r = AppContext.getInstance()
                .getUserService()
                .createUser("john.doe-cashier", UserRole.CASHIER, "password")
                .join();
        AppContext.getInstance().getUserSession().setCurrentUser(r);
    }

    /**
     * Creates a cashier user with a mock username and role, sets their password,
     * and logs them into the current session.
     * <br/>
     * This method interacts with the application's user service to register a new user
     * with administrative privileges and establishes the newly created user as the active
     * user in the session.
     */
    public static void createAndLogRootUser() {
        var r = AppContext.getInstance()
                .getUserService()
                .createUser("john.doe-root", UserRole.ROOT, "password")
                .join();
        AppContext.getInstance().getUserSession().setCurrentUser(r);
    }

}
