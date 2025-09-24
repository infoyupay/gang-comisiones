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

package com.yupay.gangcomisiones.usecase.transaction.create;

import com.yupay.gangcomisiones.export.OutputType;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import com.yupay.gangcomisiones.usecase.commons.UserPrompter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface to interact with {@link CreateTransactionController}.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface CreateTransactionView
        extends UserPrompter<CreateTransactionRequest> {

    /**
     * Asks the user where to send the ticket after creating a transaction.
     * The default implementation returns an empty Optional (meaning "none").
     *
     * @return Optional with the desired output type, or empty to skip export
     */
    Optional<OutputType> askTicketOutputType();

    /**
     * Shows a preview of the ticket to be exported.
     *
     * @param htmlBytes the html bytes to be used as a preview to show to user
     */
    void showExportPreview(byte @NotNull [] htmlBytes);
}
