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

package com.yupay.gangcomisiones.usecase.bank.create;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;

import java.util.Objects;

/**
 * Supplier implementation for providing {@link CreateBankController} instances.
 * Utilizes the {@link AppContext} singleton to supply the necessary application context
 * components during the initialization of the {@link CreateBankController}.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class CreteBankControllerSupplier implements UseCaseSupplier<CreateBankController> {
    @Override
    public CreateBankController get() {
        return new CreateBankController(
                Objects.requireNonNull(appContextSupplier().get(),
                        () -> nullPointerMessage(CreateBankController.class, AppContext.class))
        );
    }
}
