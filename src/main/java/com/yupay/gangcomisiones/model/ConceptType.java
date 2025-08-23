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
 * Type of commission concept.<br/>
 * FIXED = fixed amount<br/>
 * RATE = percentage of transaction amount
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum ConceptType {
    /**
     * Represents a fixed amount commission type.
     */
    FIXED,
    /**
     * Represents a percentage-based commission type,
     * where the commission is calculated as a percentage of the transaction amount.
     */
    RATE
}
