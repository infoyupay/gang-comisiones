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

import com.yupay.gangcomisiones.AppContext;

import java.util.function.Supplier;

/**
 * Represents a supplier interface specifically designed for use cases.
 * Extends the standard {@link Supplier} interface and provides additional
 * functionality tailored to use case controller management and application context access.
 *
 * @param <U> the type of {@code UseCaseController} supplied by this supplier
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface UseCaseSupplier<U> extends Supplier<U> {
    /**
     * Provides a supplier that returns the default implementation of {@link UseCaseControllerRegistry}.
     * This method leverages {@code ControllerRegistries::defaultRegistry} to supply a lazily initialized,
     * thread-safe instance of the registry.
     *
     * @return a {@link Supplier} providing the default {@link UseCaseControllerRegistry} implementation
     */
    default Supplier<UseCaseControllerRegistry> controllerRegistry() {
        return ControllerRegistries::defaultRegistry;
    }

    /**
     * Supplies the singleton instance of {@link AppContext}.
     *
     * @return a {@link Supplier} that provides the singleton instance of {@code AppContext}
     */
    default Supplier<AppContext> appContextSupplier() {
        return AppContext::getInstance;
    }
}
