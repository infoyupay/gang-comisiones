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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Supplier for {@link RequestReversionController} that wires the default {@link AppContext}.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class RequestReversionControllerSupplier implements UseCaseSupplier<RequestReversionController> {
    @Override
    public RequestReversionController get() {
        Supplier<AppContext> appContext = () -> Objects.requireNonNull(appContextSupplier().get(),
                () -> nullPointerMessage(RequestReversionController.class, AppContext.class));
        return new RequestReversionController(appContext.get());
    }
}
