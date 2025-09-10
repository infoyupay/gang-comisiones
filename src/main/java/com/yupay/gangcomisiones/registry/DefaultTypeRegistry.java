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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Default thread-safe implementation of {@link TypeRegistry}.
 * <br/>
 * <strong>Characteristics:</strong>
 * <ul>
 *   <li>Backed by a {@link ConcurrentHashMap} for concurrent access.</li>
 *   <li>{@link #register(Class, Supplier)} replaces any existing supplier for the same class.</li>
 *   <li>{@link #resolve(Class)} throws {@link TypeRegistryException} if
 *       no supplier is found or if the produced instance is incompatible.</li>
 *   <li>Null checks are enforced via {@link Objects#requireNonNull(Object, String)}.</li>
 * </ul>
 * <strong>Thread-safety:</strong>
 * <ul>
 *   <li>All registry operations are safe for concurrent use.</li>
 *   <li>The thread-safety of created component instances depends on the provided {@link Supplier}.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class DefaultTypeRegistry implements TypeRegistry {
    private final Map<Class<?>, Supplier<?>> suppliers = new ConcurrentHashMap<>();

    @Override
    public <U> void register(@NotNull Class<U> componentClass, @NotNull Supplier<? extends U> supplier) {
        Objects.requireNonNull(componentClass, "componentClass must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        suppliers.put(componentClass, supplier);
    }

    @Override
    public <U> U resolve(Class<U> componentClass) {
        var supplier = suppliers.get(componentClass);
        if (supplier == null) {
            throw new TypeRegistryException("Unable to find a supplier for " + componentClass);
        }
        var result = supplier.get();
        if (componentClass.isInstance(result)) {
            return componentClass.cast(result);
        }
        throw new TypeRegistryException("Retrieved instance doesn't match " + componentClass);
    }

    @Contract(pure = true)
    @Override
    public boolean isRegistered(Class<?> componentClass) {
        return suppliers.containsKey(componentClass);
    }

    @Override
    public void unregister(Class<?> componentClass) {
        suppliers.remove(componentClass);
    }
}
