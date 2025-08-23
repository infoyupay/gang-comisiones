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
 * Status of a reversal request.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum ReversalRequestStatus {
    /**
     * Represents a reversal request that has been submitted and is awaiting review or action.
     */
    PENDING,
    /**
     * Represents a reversal request that has been reviewed and approved.
     */
    APPROVED,
    /**
     * Represents a reversal request that has been reviewed and denied.
     */
    REJECTED
}