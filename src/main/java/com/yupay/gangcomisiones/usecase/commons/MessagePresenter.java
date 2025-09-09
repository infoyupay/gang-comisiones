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
 * Presenter interface capable of showing messages of different types and errors.
 * Extends {@link ErrorPresenter} so all implementations can also present errors.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface MessagePresenter extends ErrorPresenter {
    /**
     * Show a message with a specific type.
     *
     * @param message the message content
     * @param type the {@link MessageType} determining how to categorize the message
     */
    void showMessage(String message, MessageType type);

    /**
     * Convenience method to show success/information messages.
     * Delegates to {@link #showMessage(String, MessageType)} with {@link MessageType#INFORMATION}.
     *
     * @param message the message to display
     */
    default void showSuccess(String message) {
        showMessage(message, MessageType.INFORMATION);
    }

    /**
     * Convenience override to route errors through the message channel using {@link MessageType#ERROR}.
     *
     * @param message the error message to display
     */
    @Override
    default void showError(String message) {
        showMessage(message, MessageType.ERROR);
    }

    /**
     * Displays a warning message to the user. This method delegates the message
     * display to the {@link #showMessage(String, MessageType)} method with
     * {@link MessageType#WARNING}.
     *
     * @param message the warning message to display
     */
    default void showWarning(String message) {
        showMessage(message, MessageType.WARNING);
    }
}
