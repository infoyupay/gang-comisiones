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

package com.yupay.gangcomisiones.usecase.transaction.reversal.request;

import com.yupay.gangcomisiones.usecase.commons.MessagePresenter;

import java.util.Optional;

/**
 * View contract for the Request Reversion use case handled by {@link RequestReversionController}.
 * It prompts the user for a textual reason and displays information, success or error messages.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface RequestReversionView extends MessagePresenter {
    /**
     * Shows a dialog asking for the reason of the reversal request.
     *
     * @return an Optional with the text reason when user accepts, or empty when the user cancels.
     */
    Optional<String> showReasonDialog();

    /**
     * Shows an informational message to the user.
     *
     * @param message the message to display
     */
    default void showInfo(String message) {
        // Alias for success/information channel.
        showSuccess(message);
    }
}
