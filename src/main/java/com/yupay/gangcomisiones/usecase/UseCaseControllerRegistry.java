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

package com.yupay.gangcomisiones.usecase;

import com.yupay.gangcomisiones.exceptions.UseCaseControllerRegistryException;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.function.Supplier;

/**
 * Registry that centralizes the registration and resolution of use case controllers.
 * <br/>
 * Responsibilities:
 * <ul>
 *   <li>Register a controller supplier for a given {@link Class} representing the use case controller type.</li>
 *   <li>Resolve controller instances on demand using the previously registered supplier.</li>
 *   <li>Optionally register singleton instances and query or remove registrations.</li>
 * </ul>
 * Notes:
 * <ul>
 *   <li>Thread-safety is implementation-dependent.</li>
 *   <li>
 *       Suppliers may return a new instance on each call or a shared instance, depending on how they are implemented.
 *   </li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface UseCaseControllerRegistry {
    /**
     * Registers a {@link Supplier} for the specified use case controller {@link Class}.
     * <br/>
     * Semantics:
     * <ul>
     *   <li>If a supplier is already registered for the provided class, it is replaced.</li>
     *   <li>The supplier may produce new instances or return a singleton, depending on its implementation.</li>
     * </ul>
     *
     * @param useCaseClass the controller class to associate with the supplier
     * @param supplier     the {@link Supplier} that produces instances of {@code useCaseClass}
     * @param <U>          the controller type
     * @throws NullPointerException if {@code useCaseClass} or {@code supplier}
     *                              is {@code null} (implementation-dependent)
     */
    <U> void register(Class<U> useCaseClass, Supplier<? extends U> supplier);

    /**
     * Resolves an instance of the specified use case controller {@link Class}.
     * <br/>
     * Behavior:
     * <ol>
     *   <li>Looks up the supplier registered for the given class.</li>
     *   <li>Invokes the supplier to obtain an instance.</li>
     *   <li>Verifies the instance is assignable to the requested class and returns it.</li>
     * </ol>
     *
     * @param useCaseClass the controller class to resolve
     * @param <U>          the controller type
     * @return an instance of the requested controller type
     * @throws UseCaseControllerRegistryException if no supplier is registered for {@code useCaseClass}
     *                                            or the produced instance is not compatible
     */
    <U> U resolve(Class<U> useCaseClass) throws UseCaseControllerRegistryException;

    /**
     * Registers a singleton instance for the specified use case controller {@link Class}.
     * <br/>
     * This is a convenience helper equivalent to:
     * <ul>
     *   <li>{@link #register(Class, Supplier)} with a supplier that always returns {@code instance}.</li>
     * </ul>
     *
     * @param useCaseClass the controller class to associate with the given instance
     * @param instance     the instance to be returned on every resolution
     * @param <U>          the controller type
     * @throws NullPointerException if {@code useCaseClass} or {@code instance}
     *                              is {@code null} (implementation-dependent)
     * @apiNote <strong>WARNING!</strong> This method is intended for testing purposes only.
     */
    @TestOnly
    @VisibleForTesting
    default <U> void registerInstance(Class<U> useCaseClass, U instance) throws NullPointerException {
        register(useCaseClass, () -> instance);
    }

    /**
     * Checks whether a supplier is registered for the given {@link Class}.
     *
     * @param useCaseClass the controller class to query
     * @return {@code true} if a supplier is registered, {@code false} otherwise
     */
    boolean isRegistered(Class<?> useCaseClass);

    /**
     * Unregisters the supplier associated with the given {@link Class}, if present.
     *
     * @param useCaseClass the controller class whose registration should be removed
     */
    void unregister(Class<?> useCaseClass);
}

