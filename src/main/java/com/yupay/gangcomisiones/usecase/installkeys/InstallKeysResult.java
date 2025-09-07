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

package com.yupay.gangcomisiones.usecase.installkeys;

/**
 * Possible outcomes of the InstallKeys use case.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum InstallKeysResult {
    /**
     * The keys installation process, and the subsequent
     * AppContext initialization was successfully completed.
     */
    SUCCESS,
    /**
     * The user choose to abort the process before completion.
     */
    ABORT,
    /**
     * The installation process or AppContext initialization catastrophically failed.
     */
    FAILURE
}
