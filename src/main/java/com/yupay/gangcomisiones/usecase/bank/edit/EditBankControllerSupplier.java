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

package com.yupay.gangcomisiones.usecase.bank.edit;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;

import java.util.Objects;

/**
 * Supplies an instance of {@link EditBankController} within the application context. This
 * implementation ensures that the required {@link AppContext} is available and facilitates
 * the instantiation of the {@code EditBankController}. If the necessary components are
 * unavailable, a {@link NullPointerException} is thrown with a detailed message.
 *
 * <ul>
 *   <li>Relies on {@code appContextSupplier} to provide the {@link AppContext} singleton instance.</li>
 *   <li>Supports use case orchestration by supplying controllers for editing bank entities.</li>
 * </ul>
 * <p>
 * Key responsibilities:
 * <ol>
 *   <li>Invoke {@code appContextSupplier} to fetch the application context.</li>
 *   <li>Validate that the {@code AppContext} is not null, raising a detailed {@link NullPointerException} when necessary.</li>
 *   <li>Instantiate and supply a fully initialized {@link EditBankController}.</li>
 * </ol>
 *
 * @author InfoYupay SACS
 * @version 1.0
 * @implNote This supplier implementation provides an additional safeguard against null application contexts
 * during controller instantiation, which ensures proper configuration for handling bank editing use cases.
 */
public class EditBankControllerSupplier implements UseCaseSupplier<EditBankController> {
    @Override
    public EditBankController get() {
        return new EditBankController(Objects.requireNonNull(appContextSupplier().get(),
                () -> nullPointerMessage(EditBankController.class, AppContext.class)));
    }
}
