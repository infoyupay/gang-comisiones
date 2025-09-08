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

import java.util.Optional;

/**
 * <b>Overview</b><br/>
 * UserPrompter is an abstraction that unifies the most common UI operations required by a use-case view with a
 * reusable error-reporting contract.<br/>
 * It centralizes how forms are shown, messages are displayed, and errors are surfaced, promoting consistency
 * and maintainability across views.<br/>
 * <br/>
 * <b>Responsibilities</b>
 * <ul>
 *   <li>Expose a method to display a user-facing form and return its outcome as an Optional value.</li>
 *   <li>
 *       Provide a generic message channel parameterized by a message type (for example, information, warning, error).
 *   </li>
 *   <li>Offer convenience helpers for success and error notifications that delegate to the generic channel.</li>
 * </ul>
 * <br/>
 * <b>Typical Workflow</b>
 * <ol>
 *   <li>Invoke {@code showUserForm()} to capture user input and obtain the result.</li>
 *   <li>Use {@code showMessage(...)} to inform the user about process status or validation feedback.</li>
 *   <li>
 *       Call {@code showSuccess(...)} for positive outcomes and rely on {@code showError(...)} for failures.
 *   </li>
 * </ol>
 * <br/>
 * <b>Design Notes</b>
 * <ul>
 *   <li>
 *       Combines UI prompting with the {@code ErrorShower} interface to avoid duplication and enforce a
 *       single error-reporting mechanism.
 *   </li>
 *   <li>Returns {@code Optional} for form results to represent absence of input (for example, user cancellation)
 *   without exceptions.</li>
 *   <li>Default methods route success and error notifications through the same messaging pipeline,
 *   ensuring consistent UI behavior.</li>
 * </ul>
 *
 * @param <T> the type of the value produced by the user form (for example, a DTO collected from user input).
 * @author InfoYupay SACS
 * @version 1.0
 * @implNote <b>Intended Use</b>
 * <ul>
 *   <li>
 *       Implement in UI layers (desktop, web, or mobile) to standardize how use-case views prompt users
 *       and communicate results.
 *   </li>
 *   <li>Adopt across multiple views to improve reusability, reduce boilerplate, and simplify maintenance.</li>
 * </ul>
 */
public interface UserPrompter<T> extends ErrorShower {
    /**
     * Displays a user-facing form for input and captures the outcome.
     *
     * @param mode the mode of the form, indicating whether it is for creation, update, or view.
     * @return an {@link Optional} containing the value of type T representing the user's input,
     * or an empty {@link Optional} if the input is not provided or the form is canceled.
     */
    Optional<T> showUserForm(FormMode mode);

    /**
     * Displays a user-facing message categorized by a specific message type. This method can be used to
     * provide information, warnings, or errors to the user, depending on the specified message type.
     *
     * @param message the message content to be displayed to the user. It should clearly convey the intended information.
     * @param type    the type of the message being displayed, represented by the {@link MessageType} enumeration.
     *                This determines how the message will be categorized and possibly rendered.
     */
    void showMessage(String message, MessageType type);

    /**
     * Displays a success message to the user. The message is categorized as an informational message
     * to provide positive feedback or confirmation of a successful operation.
     *
     * @param message the success message content to be displayed. It should clearly convey the success
     *                of the operation or action to the user.
     */
    default void showSuccess(String message) {
        showMessage(message, MessageType.INFORMATION);
    }

    @Override
    default void showError(String message) {
        showMessage(message, MessageType.ERROR);
    }
}
