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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;

import java.util.Objects;
/// Supplier implementation for providing instances of [SetGlobalConfigController].
/// This class is responsible for constructing the controller by resolving the required
/// dependencies from the application context and ensuring their availability.
///
/// Implements [UseCaseSupplier] to integrate with the use case framework.
/// # The supplier ensures the following:
/// - Resolves the application context via `appContextSupplier`.
/// - Validates the existence of the `ViewRegistry` and associated services.
/// - Constructs a [SetGlobalConfigController] instance with resolved dependencies.
/// # Responsibilities:
/// - Handles dependency resolution from the application context.
/// - Forms a structured error message if a required component is missing.
/// # Dependencies:
/// - The [AppContext] must be initialized and provide access to
///   `ViewRegistry`, `UserService`, and `GlobalConfigService`.
/// # Error Handling:
/// - If any required component is unavailable, throws a [NullPointerException] with
///   a descriptive error message.
/// # Thread Safety:
/// - This supplier assumes singleton nature of dependencies provided by [AppContext].
/// - Thread safety is only guaranteed if the dependent components are thread-safe themselves.
///
/// @author InfoYupay SACS
/// @version 1.0
public class SetGlobalConfigControllerSupplier implements UseCaseSupplier<SetGlobalConfigController> {
    @Override
    public SetGlobalConfigController get() {
        var context = Objects.requireNonNull(appContextSupplier().get(),
                () -> nullPointerMessage(SetGlobalConfigController.class,
                        AppContext.class));
        var viewRegistry = Objects.requireNonNull(context.getViewRegistry(),
                () -> nullPointerMessage(SetGlobalConfigController.class,
                        ViewRegistry.class));
        return new SetGlobalConfigController(
                viewRegistry.resolve(SetGlobalConfigView.class),
                context.getUserService(),
                context.getGlobalConfigService(),
                false);
    }
}
