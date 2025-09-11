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

package com.yupay.gangcomisiones.usecase.registry;

import com.yupay.gangcomisiones.exceptions.UseCaseControllerRegistryException;
import com.yupay.gangcomisiones.usecase.bank.create.CreateBankController;
import com.yupay.gangcomisiones.usecase.bank.create.CreateBankControllerSupplier;
import com.yupay.gangcomisiones.usecase.bank.edit.EditBankController;
import com.yupay.gangcomisiones.usecase.bank.edit.EditBankControllerSupplier;
import com.yupay.gangcomisiones.usecase.bank.manage.ManageBankController;
import com.yupay.gangcomisiones.usecase.bank.manage.ManageBankControllerSupplier;
import com.yupay.gangcomisiones.usecase.createuser.CreateUserController;
import com.yupay.gangcomisiones.usecase.createuser.CreateUserControllerSupplier;
import com.yupay.gangcomisiones.usecase.installkeys.InstallKeyControllerSupplier;
import com.yupay.gangcomisiones.usecase.installkeys.InstallKeysController;
import com.yupay.gangcomisiones.usecase.setglobalconfig.SetGlobalConfigController;
import com.yupay.gangcomisiones.usecase.setglobalconfig.SetGlobalConfigControllerSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Utility class for managing and providing access to {@link UseCaseControllerRegistry} instances.
 * Primarily used to retrieve a default registry implementation for use case controllers.
 * <br/>
 * Uses the initialization-on-demand holder idiom to provide lazy and thread-safe initialization of the default
 * registry instance without synchronization overhead.
 * <br/>
 * Supports a one-time, optional defaults registration via {@link #registerDefaults(Consumer)} that is applied exactly
 * once upon the first initialization of the default registry.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class ControllerRegistries {
    /**
     * Atomic reference holding a one-time consumer initializer for the default implementation of
     * {@link UseCaseControllerRegistry}.
     * <br/>
     * This field is used to facilitate the registration of custom initialization logic that can modify
     * or populate the default {@link UseCaseControllerRegistry} before its first use.
     * <br/>
     * The consumer initializer set in this reference should be thread-safe, as the default registry
     * may be accessed concurrently. Once the default registry has been initialized, this reference
     * will no longer have an effect.
     * <br/>
     * Intended for internal use within {@link ControllerRegistries} to manage default registry setup
     * in a thread-safe and controlled manner.
     */
    private static final AtomicReference<Consumer<UseCaseControllerRegistry>> INITIALIZER = new AtomicReference<>();

    /**
     * Private constructor to prevent instantiation of the {@code ControllerRegistries} utility class.
     * <br/>
     * This class is designed to provide static methods and is not intended to be instantiated.
     * Marked as pure to indicate it has no side effects.
     */
    @Contract(pure = true)
    private ControllerRegistries() {
        // Prevent instantiation
    }

    /**
     * Provides the default implementation of {@link UseCaseControllerRegistry}.
     * The instance is created lazily in a thread-safe manner on first access.
     *
     * @return the default implementation of {@link UseCaseControllerRegistry}
     */
    @Contract(pure = true)
    public static UseCaseControllerRegistry defaultRegistry() {
        return Holder.INSTANCE;
    }

    /**
     * Registers a one-time initializer to populate the default registry right before its first use.
     * <br/>
     * If the default registry has already been initialized, this method has no effect and returns {@code false}.
     * If an initializer was already registered and the registry is not yet initialized, this method returns
     * {@code false} and preserves the first initializer.
     *
     * @param initializer a consumer that receives the registry to register default controllers
     * @return {@code true} if the initializer was registered successfully, {@code false} otherwise
     */
    @Contract("null -> fail")
    public static boolean registerDefaults(Consumer<UseCaseControllerRegistry> initializer) {
        if (initializer == null) {
            throw new UseCaseControllerRegistryException("initializer must not be null");
        }
        // If the instance is already created, do nothing.
        // Touching Holder.INSTANCE would force initialization, so we avoid that here.
        // We only accept the initializer if none was set before.
        return INITIALIZER.compareAndSet(null, initializer);
    }

    /**
     * Registers the default controllers for the application by utilizing a predefined initializer.
     * The initializer will register specific components in the default registry before its first use.
     * <br/>
     * This provides the necessary wiring for default controllers.
     * If the default registry has already been initialized, this method has no effect
     * and returns {@code false}. If an initializer was already registered and the registry
     * is not yet initialized, this method returns {@code false} and preserves the initial initializer.
     *
     * @return {@code true} if the default initializer was registered successfully, {@code false} otherwise
     */
    public static boolean registerAllDefaults() {
        return registerDefaults(reg -> {
            reg.register(InstallKeysController.class,
                    new InstallKeyControllerSupplier());
            reg.register(CreateUserController.class,
                    new CreateUserControllerSupplier());
            reg.register(SetGlobalConfigController.class,
                    new SetGlobalConfigControllerSupplier());
            reg.register(CreateBankController.class,
                    new CreateBankControllerSupplier());
            reg.register(EditBankController.class,
                    new EditBankControllerSupplier());
            reg.register(ManageBankController.class,
                    new ManageBankControllerSupplier());
        });
    }

    /**
     * Static nested class that holds the default instance of {@link UseCaseControllerRegistry}.
     * Implements the initialization-on-demand holder idiom for lazy and thread-safe instantiation.
     * <br/>
     * The singleton instance is created upon the first invocation of its enclosing class's {@code defaultRegistry} method.
     * This design ensures that the default registry is initialized only once and without requiring explicit
     * synchronization, while allowing optional customization prior to the registry's creation.
     * <br/>
     * This mechanism leverages an optional initializer, which, if supplied via {@code ControllerRegistries#registerDefaults},
     * is applied exactly once during the creation of the default registry.
     */
    private static final class Holder {
        private static final UseCaseControllerRegistry INSTANCE = create();

        /**
         * Creates and initializes a new instance of {@link UseCaseControllerRegistry}.
         * This method employs an atomic initializer, ensuring that any optional
         * customization logic is applied exactly once. The returned registry is
         * an instance of {@link DefaultUseCaseControllerRegistry}.
         *
         * @return a fully initialized and potentially customized {@link UseCaseControllerRegistry} instance
         */
        private static @NotNull UseCaseControllerRegistry create() {
            UseCaseControllerRegistry registry = new DefaultUseCaseControllerRegistry();
            Consumer<UseCaseControllerRegistry> init = INITIALIZER.getAndSet(null);
            if (init != null) {
                init.accept(registry);
            }
            return registry;
        }
    }
}
