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

package com.yupay.gangcomisiones.usecase.createuser;

import java.util.Optional;

/**
 * Interface for displaying user creation views and handling corresponding user interactions.
 * This interface defines methods for showing forms, error messages, and success messages
 * during the user creation process.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface CreateUserView {
    /// Displays a form for creating a new user and collects the required information. The form shall include the
    /// following fields:
    /// - Username
    /// - Password
    /// - Role: any of [#ADMIN],
    /// [#ADMIN] or [#CASHIER]
    /// If bootstrapMode, the form will force the role to [#ROOT].
    ///
    /// @param bootstrapMode a boolean indicating whether the form should be displayed in bootstrap mode.
    ///                                           If true, the form will use the bootstrap configuration, otherwise, it will not.
    /// @return an `Optional<CreateUserDTO>` containing the user creation data if the form is completed successfully,
    /// or an empty `Optional` if the operation is canceled or fails.
    Optional<CreateUserDTO> showCreateUserForm(boolean bootstrapMode);

    /**
     * Displays an error message to the user.
     *
     * @param message the error message to be displayed. It should provide
     *                information about the error encountered.
     */
    void showError(String message);

    /**
     * Displays a success message to the user.
     *
     * @param message the success message to be displayed. It should convey
     *                information about the successful operation or action.
     */
    void showSuccess(String message);
}
