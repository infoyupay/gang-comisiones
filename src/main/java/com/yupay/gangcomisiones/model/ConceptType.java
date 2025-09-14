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

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    FIXED {
        @Override
        public BigDecimal computeCommission(@NotNull BigDecimal amount, @NotNull BigDecimal value) {
            return value;
        }
    },
    /**
     * Represents a percentage-based commission type,
     * where the commission is calculated as a percentage of the transaction amount.
     */
    RATE {
        @Override
        public BigDecimal computeCommission(@NotNull BigDecimal amount, @NotNull BigDecimal value) {
            return amount.multiply(value).setScale(2, RoundingMode.HALF_UP);
        }
    };

    /**
     * Computes the commission based on the provided amount and value.
     * <br/>
     * The implementation of this method depends on the specific type of commission:
     * <ul>
     *   <li>For fixed commissions, it returns a constant value.</li>
     *   <li>For percentage-based commissions, it calculates a percentage of the given amount.</li>
     * </ul>
     *
     * @param amount The transaction amount on which the commission is calculated. Must not be null.
     *               <br/>Examples:
     *               <ol>
     *                 <li>In percentage-based commissions, this value is used as the base for the calculation.</li>
     *                 <li>In fixed commissions, this value is not used directly in the computation.</li>
     *               </ol>
     * @param value The rate or fixed value of the commission. Must not be null.
     *              <br/>Examples:
     *              <ul>
     *                <li>In fixed commissions, this represents the predefined fixed amount to return.</li>
     *                <li>In percentage-based commissions, this represents the rate at which the amount is calculated (e.g., 0.05 for 5%).</li>
     *              </ul>
     * @return The computed commission as a {@link BigDecimal}, according to the commission type.
     *         <br/>
     *         <ul>
     *           <li>For a fixed commission type, the value is returned as-is.</li>
     *           <li>For a rate commission type, the calculated percentage of the amount is returned.</li>
     *         </ul>
     */
    public abstract BigDecimal computeCommission(BigDecimal amount, BigDecimal value);
}
