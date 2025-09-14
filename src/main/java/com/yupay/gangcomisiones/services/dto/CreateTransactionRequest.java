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
package com.yupay.gangcomisiones.services.dto;

import com.yupay.gangcomisiones.model.ConceptType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Request object for creating a new transaction.
 * <br/>
 * Carries the input data provided by the client layer
 * (e.g., controller or UI) to the application service.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
public class CreateTransactionRequest {
    /**
     * Identifier of the bank associated with the transaction.
     */
    private final long bankId;

    /**
     * Identifier of the concept associated with the transaction.
     */
    private final long conceptId;

    /**
     * Identifier of the cashier (user) responsible for the transaction.
     */
    private final long cashierId;

    /**
     * Monetary amount of the transaction.
     */
    private final BigDecimal amount;
    /**
     * Type of the concept associated with the transaction.
     */
    private final ConceptType conceptType;

    /**
     * Value of the commission associated with the concept, if
     * {@link #conceptType} is {@link ConceptType#FIXED}
     * the commisionAmount is just this value; if concept type is
     * {@link ConceptType#RATE} then commisionAmount is calculated as
     * {@code conceptCommisionValue * amount}.
     */
    private final BigDecimal conceptCommissionValue;

}
