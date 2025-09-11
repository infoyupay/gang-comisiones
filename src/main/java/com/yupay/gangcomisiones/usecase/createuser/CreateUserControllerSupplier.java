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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A supplier class responsible for providing instances of {@link CreateUserController}.
 * This class implements the {@link UseCaseSupplier} interface and supplies a configured
 * instance of the {@code CreateUserController} by utilizing the application context and
 * its associated resources.
 * <br/>
 * The {@code CreateUserControllerSupplier} ensures that all the required dependencies,
 * such as the {@link AppContext}, {@link ViewRegistry}, and view classes, are resolved
 * before supplying the controller instance. It also validates that none of the retrieved
 * dependencies are null.
 *
 * @author InfoYupay SACS
 * @version 1.0
 * @implNote <div style="border: 2px solid orange; padding: 4px;">
 * <strong>&#x26A0;&#xFE0F;WARNING!</strong>: This implementation only provides CreateUserController instances
 * suitable for normal execution ({@code bootstrapMode=false}). This is because the application context
 * is not fully initialized during bootstrap mode, which may lead to unexpected behavior, and registries are
 * not suposed to be ready during the bootstrap process, such as {@link ViewRegistry}.
 * </div>
 */
public final class CreateUserControllerSupplier implements UseCaseSupplier<CreateUserController> {
    @Override
    public @NotNull CreateUserController get() {
        var context = Objects.requireNonNull(appContextSupplier().get(),
                () -> nullPointerMessage(CreateUserController.class,
                        AppContext.class));
        var viewRegistry = Objects.requireNonNull(context.getViewRegistry(),
                () -> nullPointerMessage(CreateUserController.class,
                        ViewRegistry.class));

        return new CreateUserController(
                viewRegistry.resolve(CreateUserView.class),
                context.getUserService(),
                false);
    }
}
