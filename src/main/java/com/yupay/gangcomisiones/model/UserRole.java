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

package com.yupay.gangcomisiones.model;

import org.jetbrains.annotations.Contract;

import java.util.Comparator;

/**
 * User roles in the system.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum UserRole {
    /**
     * Represents the highest-level user role in the system
     * with full access and administrative privileges.
     */
    ROOT(Integer.MAX_VALUE),
    /**
     * Represents a user role within the system that has administrative
     * capabilities, typically with elevated permissions required for
     * managing key aspects of the application. Except users management, which
     * is a exclusive privilege of ROOT.
     */
    ADMIN(2),
    /**
     * Represents a user role responsible for handling operational tasks,
     * such as processing transactions or assisting customers, with limited
     * access rights compared to administrative roles.
     */
    CASHIER(1);
    /**
     * The level of the user role, the ROOT is MAX_VALUE
     * becaouse nobody can have more privileges than ROOT.
     * This number represents the priority level, the greater the
     * number, the more privileged the user is.
     */
    private final int level;

    /**
     * Constructs a new UserRole with a specified privilege level.
     *
     * @param level the privilege level associated with the user role.
     *              The higher the level, the greater the privileges.
     */
    UserRole(int level) {
        this.level = level;
    }

    /**
     * Determines whether the current user role has at least the same privilege level as the specified role.
     *
     * @param role the user role to compare against. If null, this method will return false.
     * @return true if the current user role has an equal or higher privilege level than the specified role;
     *         false otherwise.
     */
    @Contract(value = "null -> false", pure = true)
    public boolean isAtLeast(UserRole role) {
        if (role == null) return false;
        return this.level >= role.level;
    }
}
