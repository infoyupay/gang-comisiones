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

/**
 * Types of message that can be shown to the user.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum MessageType {
    /**
     * Represents an informational message type intended to provide non-critical,
     * general information to the user.
     */
    INFORMATION,
    /**
     * Represents a warning message type intended to alert the user about a potential issue
     * that may require their attention but is not critical.
     */
    WARNING,
    /**
     * Represents an error message type intended to indicate a critical issue.
     * Used to notify the user about a situation that requires immediate attention or resolution.
     */
    ERROR
}
