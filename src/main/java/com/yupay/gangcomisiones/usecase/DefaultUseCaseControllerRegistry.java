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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Default thread-safe implementation of {@link UseCaseControllerRegistry}.
 * <br/>
 * <strong>Characteristics:</strong>
 * <ul>
 *   <li>Backed by a {@link ConcurrentHashMap} for concurrent access.</li>
 *   <li>{@link #register(Class, Supplier)} replaces any existing supplier for the same class.</li>
 *   <li>{@link #resolve(Class)} throws {@link UseCaseControllerRegistryException} if
 *       no supplier is found or if the produced instance is incompatible.</li>
 *   <li>Null checks are enforced via {@link Objects#requireNonNull(Object, String)}.</li>
 * </ul>
 * <strong>Thread-safety:</strong>
 * <ul>
 *   <li>All registry operations are safe for concurrent use.</li>
 *   <li>The thread-safety of created controller instances depends on the provided {@link Supplier}.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class DefaultUseCaseControllerRegistry implements UseCaseControllerRegistry {
    private final Map<Class<?>, Supplier<?>> controllers = new ConcurrentHashMap<>();

    @Override
    public <U> void register(@NotNull Class<U> useCaseClass, @NotNull Supplier<? extends U> supplier) {
        Objects.requireNonNull(useCaseClass, "useCaseClass must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");
        controllers.put(useCaseClass, supplier);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U resolve(Class<U> useCaseClass) {
        var supplier = controllers.get(useCaseClass);
        if (supplier == null) {
            throw new UseCaseControllerRegistryException("Unable to find a supplier for " + useCaseClass);
        }
        var result = supplier.get();
        if (useCaseClass.isInstance(result)) {
            return (U) result;
        }
        throw new UseCaseControllerRegistryException("Retrieved controller doesn't match " + useCaseClass);
    }

    @Contract(pure = true)
    @Override
    public boolean isRegistered(Class<?> useCaseClass) {
        return controllers.containsKey(useCaseClass);
    }

    @Override
    public void unregister(Class<?> useCaseClass) {
        controllers.remove(useCaseClass);
    }
}
