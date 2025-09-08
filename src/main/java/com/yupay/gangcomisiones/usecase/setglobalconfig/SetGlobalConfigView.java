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

package com.yupay.gangcomisiones.usecase.setglobalconfig;

import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.usecase.ErrorShower;

import java.util.Optional;

/**
 * Contract for the view of the "Set Global Config" use case.
 * <br/>
 * Implementations are responsible for displaying the form, handling user
 * interactions and returning the resulting {@link GlobalConfig} instance.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface SetGlobalConfigView extends ErrorShower {
    /**
     * Displays the Global Config form to the user.
     * <ul>
     *     <li>The form must allow editing of all {@link GlobalConfig} fields that are intended to be changed by a user.</li>
     *     <li>A copy of the current config is provided; implementations may modify and return it.</li>
     *     <li>If the user cancels/closes the form, return an empty {@link Optional}.</li>
     * </ul>
     *
     * @param config        a copy of the current {@link GlobalConfig} to be edited.
     * @param bootstrapMode whether the application is running in bootstrap mode.
     * @return an {@link Optional} containing the edited {@link GlobalConfig}, or empty if the user cancelled.
     */
    Optional<GlobalConfig> showSetGlobalConfigForm(GlobalConfig config, boolean bootstrapMode);

    /**
     * Shows a success/confirmation message to the user.
     *
     * @param message the text to display.
     */
    void showSuccess(String message);
}
