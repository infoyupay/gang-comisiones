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
    ROOT,
    /**
     * Represents a user role within the system that has administrative
     * capabilities, typically with elevated permissions required for
     * managing key aspects of the application. Except users management, which
     * is a exclusive privilege of ROOT.
     */
    ADMIN,
    /**
     * Represents a user role responsible for handling operational tasks,
     * such as processing transactions or assisting customers, with limited
     * access rights compared to administrative roles.
     */
    CASHIER
}
