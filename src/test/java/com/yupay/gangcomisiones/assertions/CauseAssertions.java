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

package com.yupay.gangcomisiones.assertions;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * A utility class designed to facilitate assertions on the causes within a throwable's causal chain.
 * <br/>
 * This class provides functionality to verify:
 * <ul>
 *   <li>The presence of a specific type of cause within a throwable hierarchy.</li>
 *   <li>The cause's message content, supporting case-insensitive matching.</li>
 * </ul>
 * <br/>
 * {@link CauseAssertions} serves as a helpful tool for building robust and expressive assertions related
 * to error handling and exception cause validation.
 * <p>
 * <br/>
 * Features include:
 * <ol>
 *   <li>Static factory method for instantiating type-safe {@link CauseAssertions} based on the expected cause type.</li>
 *   <li>Assertions to ensure the presence of the specified type of cause in a throwable's chain.</li>
 *   <li>Enhanced methods to combine cause type validation with message content checking.</li>
 * </ol>
 *
 * @param <C>      The type of the expected cause. The specified type must extend {@link Throwable}.
 * @param expected A {@link Class} object representing the type of the cause to assert. Must not be {@code null}.
 *                 *                 Throws a {@link NullPointerException} if the provided {@code Class} is {@code null}.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record CauseAssertions<C extends Throwable>(Class<C> expected) {

    /**
     * Creates a new {@link CauseAssertions} instance for verifying the presence of a specific type of cause
     * within a throwable's causal chain.
     * <br/>
     * The validation process involves:
     * <ul>
     *   <li>Specifying the expected cause type to be checked within a throwable's hierarchy.</li>
     *   <li>Providing additional assertion utility methods to validate the throwable's cause.</li>
     * </ul>
     *
     * @param <C>      The type of the expected cause. This must extend {@link Throwable}.
     * @param expected A {@link Class} object representing the type of the cause to assert. Must not be {@code null}.
     *                 Throws a {@link NullPointerException} if the provided {@code Class} is {@code null}.
     * @return A new and initialized {@link CauseAssertions} instance for verifying conditions on the throwable's causes.
     */
    @Contract("_ -> new")
    public static <C extends Throwable> @NotNull CauseAssertions<C> assertExpectedCause(Class<C> expected) {
        return new CauseAssertions<>(expected);
    }

    /**
     * Asserts that the provided {@code Throwable} contains a cause of the expected type.
     * <br/>
     * This method ensures that the {@code Throwable} or one of its nested causes matches the expected type,
     * and throws a validation failure assertion if no such cause is found.
     * <br/>
     * <p>
     * The process involves:
     * <ol>
     *   <li>Locating a cause within the {@code Throwable} hierarchy that matches the expected type.</li>
     *   <li>Validating that the located cause is not {@code null}, ensuring that the expected type is present.</li>
     * </ol>
     *
     * @param t The root {@link Throwable} to assert. Must not be {@code null}.
     *          Throws a {@link NullPointerException} if the provided {@code Throwable} is {@code null}.
     * @return An {@link AbstractThrowableAssert} for further customized assertions on the located cause of the expected type.
     */
    public AbstractThrowableAssert<?, C> assertCause(Throwable t) {
        return assertThat(findCause(t))
                .as(t.getClass() + " must contain a cause of type " + expected)
                .isNotNull();
    }

    /**
     * Asserts that the provided {@code Throwable} contains a cause of the expected type
     * and that the cause's message contains the specified text, ignoring case.
     * <br/>
     * This method combines type checking and message content verification to ensure
     * both conditions are met for a valid assertion.
     * <br/>
     * <p>
     * The process involves:
     * <ol>
     *   <li>Verifying that the root or nested causes of the {@code Throwable} match the expected type.</li>
     *   <li>Inspecting the message of the identified cause to ensure it contains the provided text, case-insensitively.</li>
     * </ol>
     *
     * @param t       The root {@link Throwable} to assert. Must not be {@code null}.
     *                Throws a {@link NullPointerException} if the provided {@code Throwable} is {@code null}.
     * @param message A {@link String} that should be present, case-insensitively, in the message
     *                of the identified cause. Must not be {@code null} or empty.
     * @return An {@link AbstractStringAssert} object to further customize the assertion on the identified cause's message.
     */
    public AbstractStringAssert<?> assertCauseWithMessage(Throwable t, String message) {
        return assertCause(t)
                .message()
                .containsIgnoringCase(message);
    }

    /**
     * Attempts to locate and return a cause of the specified type within the given {@code Throwable} hierarchy.
     * The method inspects the throwable's causal chain to identify if at least one cause matches the expected type.
     * <br/>
     * <p>
     * The traversal process involves checking:
     * <ol>
     *   <li>The input {@code Throwable} itself.</li>
     *   <li>The causes of the input {@code Throwable}, recursively, if available.</li>
     * </ol>
     * <p>
     * If no matching cause is found, {@code null} is returned.
     *
     * @param t The root {@link Throwable} to start the search from. Must not be {@code null}.
     *          Throwing an {@link NullPointerException} if the provided {@code Throwable} is null.
     * @return The first cause of the type {@code C} found in the exception chain, or {@code null} if no such cause exists.
     */
    public @Nullable C findCause(@NotNull Throwable t) {
        Objects.requireNonNull(t, "Throwable must not be null");
        for (var cause = t; cause != null; cause = cause.getCause()) {
            if (expected.isInstance(cause)) {
                return expected.cast(cause);
            }
        }
        t.printStackTrace();
        return null;
    }
}
