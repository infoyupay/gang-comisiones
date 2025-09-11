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

package com.yupay.gangcomisiones.usecase.installkeys;

import com.yupay.gangcomisiones.usecase.registry.UseCaseSupplier;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import com.yupay.gangcomisiones.usecase.task.TaskMonitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/// A supplier implementation that provides instances of [InstallKeysController].
/// This class utilizes the application context to resolve and inject all necessary
/// dependencies required by the `InstallKeysController`.
/// The supplier ensures the following:
/// - The application context is retrieved and is non-null.
/// - The [ViewRegistry] and required view and task monitor components are available.
/// - Proper initialization of the `InstallKeysController` with resolved dependencies.
/// Implements [UseCaseSupplier] for structured management of use case controllers.
///
/// @author InfoYupay SACS
/// @version 1.0
public final class InstallKeyControllerSupplier
        implements UseCaseSupplier<InstallKeysController> {
    /**
     * Provides an instance of {@link InstallKeysController} by resolving its dependencies
     * from the supplied application context. The method ensures that all required components
     * are available and non-null before constructing the controller.
     *
     * @return a fully initialized {@link InstallKeysController} with all required dependencies
     * resolved from the application context.
     * @throws NullPointerException if the supplied context, view registry, or any required
     *                              component is null.
     */
    @Override
    public @NotNull InstallKeysController get() throws NullPointerException {
        var context = Objects.requireNonNull(appContextSupplier().get(),
                "Supplied context cannot be null");
        var viewRegistry =
                Objects.requireNonNull(context.getViewRegistry(),
                        "View registry cannot be null in supplied context.");
        return new InstallKeysController(viewRegistry.resolve(InstallKeysView.class),
                viewRegistry.resolve(TaskMonitor.class),
                context.getInstallationService());
    }
}
