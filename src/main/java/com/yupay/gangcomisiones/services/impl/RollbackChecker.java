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

package com.yupay.gangcomisiones.services.impl;

import jakarta.persistence.EntityTransaction;
import org.jetbrains.annotations.Contract;

/**
 * Provides methods to check and rollback transactions.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface RollbackChecker {
    /**
     * Checks if the transaction is active and rolls it back if it is.
     *
     * @param et The entity transaction to check.
     * @param ex The exception to re-throw if inside a catch block.
     */
    @Contract("null, !null -> fail")
    default void checkTxAndRollback(EntityTransaction et, RuntimeException ex) {
        if (et != null && et.isActive()) {
            et.rollback();
        }
        if (ex != null) {
            throw ex;
        }
    }
}
