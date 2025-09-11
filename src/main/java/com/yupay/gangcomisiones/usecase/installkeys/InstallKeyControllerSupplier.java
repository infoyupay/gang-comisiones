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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.services.ZipInstallerService;
import com.yupay.gangcomisiones.usecase.task.TaskMonitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/// A supplier for [InstallKeysController], which is responsible for
/// providing an instance of the controller using a supplied application context.
/// This class implements [Supplier] and leverages a provided [AppContext]
/// to resolve required dependencies for the [InstallKeysController].
/// ## Design
/// The `InstallKeyControllerSupplier` relies on a [Supplier] of
/// [AppContext] to retrieve the application context. Using this context,
/// it ensures that all necessary components, such as the `ViewRegistry`,
/// `InstallationService`, and specific views, are available to construct
/// the [InstallKeysController].
/// ## Validation
/// Validation is performed to ensure that the following components are non-null:
/// - The application context retrieved from the `contextSupplier`.
/// - The `ViewRegistry` obtained from the application context.
/// If these components are not available, an [IllegalArgumentException]
/// is thrown.
/// ## Instances Provided
/// The supplier constructs and provides instances of [InstallKeysController]
/// that are ready to manage installation keys. The controller is initialized with:
/// - A resolved [InstallKeysView].
/// - A resolved [TaskMonitor].
/// - The [ZipInstallerService] from the application context.
///
/// This design ensures that each call to [#get()] returns a fully initialized,
/// ready-to-use instance of [InstallKeysController].
///
/// @param contextSupplier A supplier for the [AppContext]. The provided
///                        context must contain all necessary components, including
///                        the `ViewRegistry` and `InstallationService`.
/// @author InfoYupay SACS
/// @version 1.0
public record InstallKeyControllerSupplier(Supplier<AppContext> contextSupplier)
        implements Supplier<InstallKeysController> {
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
        var context = Objects.requireNonNull(contextSupplier.get(),
                "Supplied context cannot be null");
        var viewRegistry =
                Objects.requireNonNull(context.getViewRegistry(),
                        "View registry cannot be null in supplied context.");
        return new InstallKeysController(viewRegistry.resolve(InstallKeysView.class),
                viewRegistry.resolve(TaskMonitor.class),
                context.getInstallationService());
    }
}
