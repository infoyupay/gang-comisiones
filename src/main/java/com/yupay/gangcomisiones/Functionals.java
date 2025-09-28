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

package com.yupay.gangcomisiones;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utility class containing functional programming utilities.
 * <br/><br/>
 * This class is designed to house static methods offering functional constructs such as no-op consumers
 * and methods to chain consumers. These utilities are aimed at supporting functional programming paradigms
 * in Java.
 * <br/><br/>
 * Characteristics:
 * <ul>
 *     <li>Cannot be instantiated, as its constructor is private.</li>
 *     <li>Provides reusable, thread-safe functional artifacts.</li>
 *     <li>Focused on operations related to {@link Consumer}.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class Functionals {
    /**
     * Utility class containing functional programming utilities.
     * <br/><br/>
     * This class is designed to house static methods offering functional constructs such as no-op consumers
     * and methods to chain consumers. These utilities are aimed at supporting functional programming paradigms
     * in Java.
     * <br/><br/>
     * Characteristics:
     * <ul>
     *     <li>Cannot be instantiated, as its constructor is private.</li>
     *     <li>Provides reusable, thread-safe functional artifacts.</li>
     *     <li>Focused on operations related to {@link Consumer}.</li>
     * </ul>
     */
    @Contract(pure = true)
    private Functionals() {
    }

    /**
     * Returns a no-op {@link Consumer} that performs no operation when invoked.
     * <br/>
     * This method provides a reusable, stateless, and thread-safe {@link Consumer} instance that can be
     * utilized in scenarios where a functional placeholder or default behavior is required.
     * <br/><br/>
     * Key characteristics of the returned {@link Consumer}:
     * <ul>
     *   <li>It accepts any input type {@code T}.</li>
     *   <li>It performs no side effects or operations.</li>
     *   <li>It can be safely reused across threads without state modification.</li>
     * </ul>
     *
     * @param <T> The type of input to the {@link Consumer}.
     *            <br/>
     * @return A no-op {@link Consumer} that ignores its input and performs no action.
     */
    @Contract(pure = true)
    public static <T> @NotNull Consumer<T> groundConsumer() {
        return _ -> {
        };
    }

    /**
     * Chains multiple {@link Consumer} instances into a single {@link Consumer}.
     * Each {@link Consumer} in the chain is executed in the order it is provided.
     * <br/>
     * If no {@link Consumer} instances are provided, a default no-op {@link Consumer}
     * will be returned.
     *
     * @param <T>       The type of the input to the {@link Consumer}.
     * @param consumers An array of {@link Consumer} instances to be chained.
     *                                   <ul>
     *                                       <li>If no consumers are provided, the returned {@link Consumer} will be a no-op.</li>
     *                                       <li>If only one {@link Consumer} is provided, it will be returned unchanged.</li>
     *                                       <li>If multiple consumers are provided, they will be chained using {@link Consumer#andThen}.</li>
     *                                   </ul>
     *                                   This parameter can include {@code null} values, which are ignored during the chaining.
     *                                   Non-{@code null} consumers are executed in order.
     *                  <br/>
     * @return A single {@link Consumer} that represents the chain of input {@link Consumer} instances.
     * <ul>
     *     <li>Returns a no-op {@link Consumer} if no input {@link Consumer} instances were provided.</li>
     *     <li>Returns a single chained {@link Consumer} when multiple instances are provided.</li>
     * </ul>
     */
    @SafeVarargs
    public static <T> Consumer<T> chainConsumers(@NotNull Consumer<T> @NotNull ... consumers) {
        return Stream.of(consumers).reduce(Consumer::andThen).orElseGet(Functionals::groundConsumer);
    }
}
