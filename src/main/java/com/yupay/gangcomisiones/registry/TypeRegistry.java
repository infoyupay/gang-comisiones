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

package com.yupay.gangcomisiones.registry;

import com.yupay.gangcomisiones.exceptions.TypeRegistryException;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.function.Supplier;

/**
 * General-purpose registry that centralizes registration and resolution of components by their {@link Class}.
 * <br/>
 * Responsibilities:
 * <ul>
 *   <li>Associate a component type with a {@link Supplier} that produces instances of that type.</li>
 *   <li>Resolve component instances on demand via the registered supplier.</li>
 *   <li>Optionally register singleton instances and query or remove registrations.</li>
 * </ul>
 * Notes:
 * <ul>
 *   <li>Thread-safety is implementation-dependent.</li>
 *   <li>Suppliers may return a new instance on each call or a shared instance, depending on their implementation.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface TypeRegistry {
    /**
     * Registers a {@link Supplier} for the specified component {@link Class}.
     * <ul>
     *   <li>If a supplier is already registered for the provided class, it is replaced.</li>
     *   <li>The supplier may produce new instances or return a singleton, depending on its implementation.</li>
     * </ul>
     *
     * @param componentClass the component class to associate with the supplier
     * @param supplier       the {@link Supplier} that produces instances of {@code componentClass}
     * @param <U>            the component type
     * @throws NullPointerException if {@code componentClass} or {@code supplier} is {@code null} (implementation-dependent)
     */
    <U> void register(Class<U> componentClass, Supplier<? extends U> supplier);

    /**
     * Resolves an instance of the specified component {@link Class}.
     * <ol>
     *   <li>Looks up the supplier registered for the given class.</li>
     *   <li>Invokes the supplier to obtain an instance.</li>
     *   <li>Verifies the instance is assignable to the requested class and returns it.</li>
     * </ol>
     *
     * @param componentClass the component class to resolve
     * @param <U>            the component type
     * @return an instance of the requested component type
     * @throws TypeRegistryException if no supplier is registered for {@code componentClass}
     *                               or the produced instance is not compatible
     */
    <U> U resolve(Class<U> componentClass) throws TypeRegistryException;

    /**
     * Registers a singleton instance for the specified component {@link Class}.
     * <ul>
     *   <li>Equivalent to {@link #register(Class, java.util.function.Supplier)} with a supplier that always returns {@code instance}.</li>
     * </ul>
     *
     * @param componentClass the component class to associate with the given instance
     * @param instance       the instance to be returned on every resolution
     * @param <U>            the component type
     * @throws NullPointerException if {@code componentClass} or {@code instance} is {@code null} (implementation-dependent)
     */
    @TestOnly
    @VisibleForTesting
    default <U> void registerInstance(Class<U> componentClass, U instance) throws NullPointerException {
        register(componentClass, () -> instance);
    }

    /**
     * Checks whether a supplier is registered for the given {@link Class}.
     *
     * @param componentClass the component class to query
     * @return {@code true} if a supplier is registered, {@code false} otherwise
     */
    boolean isRegistered(Class<?> componentClass);

    /**
     * Unregisters the supplier associated with the given {@link Class}, if present.
     *
     * @param componentClass the component class whose registration should be removed
     */
    void unregister(Class<?> componentClass);
}
