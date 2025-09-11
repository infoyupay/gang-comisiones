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

package com.yupay.gangcomisiones.usecase.bank.manage;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseControllerRegistry;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;

/**
 * A supplier implementation for providing instances of {@link ManageBankController}.
 * This class extends the {@code UseCaseSupplier} interface, making it part of a specialized
 * framework for handling use case controllers. It supplies fully configured instances
 * of {@link ManageBankController} using the application's context and controller registry.
 * <br/>
 * The implementation relies on the default {@link AppContext} and {@link UseCaseControllerRegistry}
 * to construct new instances of the controller.
 * <br/>
 * This class ensures that the supplied {@code ManageBankController} is properly initialized
 * with all necessary dependencies.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class ManageBankControllerSupplier implements UseCaseSupplier<ManageBankController> {
    @Override
    public ManageBankController get() {
        return new ManageBankController(appContextSupplier(), controllerRegistry().get());
    }
}
