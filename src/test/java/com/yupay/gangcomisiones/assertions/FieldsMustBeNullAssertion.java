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

import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents an assertion utility designed to validate that specific fields
 * of an object, extracted using given extractor functions, are {@code null}.
 * <br/>
 * This record is intended to be used in conjunction with soft assertions
 * from AssertJ's {@link SoftAssertions} library, allowing multiple assertions
 * to be performed without interrupting execution upon failures.
 * <br/>
 * Key characteristics of this utility:
 * <ul>
 *     <li>Encapsulates a textual description to provide meaningful context for assertions.</li>
 *     <li>Uses extractor functions to target specific fields or properties of a given object.</li>
 *     <li>Softly asserts that the extracted fields are {@code null}.</li>
 *     <li>Designed for usage in test environments, particularly for nullity validation scenarios.</li>
 * </ul>
 *
 * @param <T>         the type of the object being validated.
 * @param description the description for the assertion failure message.
 * @param extracting  the functions to extract fields values whose nullity must be asserted.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record FieldsMustBeNullAssertion<T>(@NotNull String description,
                                           @NotNull Function<? super T, ?> @NotNull ... extracting) {

    /**
     * Constructs an instance of {@code FieldsMustBeNullAssertion}, a utility for performing
     * nullity assertions on specific fields or properties of a given object in a testing context.
     * This is achieved through extractor functions which define the fields to be validated.
     * <br/>
     * The assertions are typically applied softly, allowing multiple validation failures to be
     * collected and reported at once instead of halting execution after the first failure.
     * <br/>
     * <ul>
     *     <li>Ensures description and extractor functions are non-{@code null}.</li>
     *     <li>Supports soft assertion mechanisms through the use of {@link SoftAssertions}.</li>
     *     <li>Designed for flexible and reusable null verification logic in test cases.</li>
     * </ul>
     *
     * @param description the description for the assertion failure message.
     * @param extracting  the functions to extract fields values whose nullity must be asserted.
     */
    @Contract(pure = true)
    @SafeVarargs
    public FieldsMustBeNullAssertion {
    }

    /**
     * Creates a {@link Consumer} that performs a series of soft assertions to validate
     * that specific fields, extracted from the given value using provided extractor functions,
     * are null. The assertions will be softly applied, and any failures will be collected
     * without halting execution.
     * <br/>
     * This method is useful for verifying that certain fields or properties of an object
     * meet null expectations within a testing context.
     * <br/>
     * <ul>
     *     <li>Ensures the value is not {@code null} before applying assertions.</li>
     *     <li>Uses the provided extractor functions to access specific fields of the given value.</li>
     *     <li>Softly asserts that each extracted field is {@code null}.</li>
     *     <li>Provides a description to contextualize failed assertions in test reports.</li>
     * </ul>
     *
     * @param <T>         the type of the value to be validated.
     *                    <br/>
     * @param description a textual description that provides context for the nullity assertions.
     *                                       <ul>
     *                                           <li>Must not be {@code null}.</li>
     *                                       </ul>
     *                    <br/>
     * @param value       the object from which fields or properties will be accessed and validated.
     *                                 <ul>
     *                                     <li>May be {@code null}; if so, a related null assertion is softly applied.</li>
     *                                 </ul>
     *                    <br/>
     * @param extracting  varargs of functions used to extract fields or properties from the value.
     *                                      <ul>
     *                                          <li>Must not be {@code null}; each provided function must operate in a non-null context.</li>
     *                                      </ul>
     *                    <br/>
     * @return a {@link Consumer} that applies the generated assertions to a {@link SoftAssertions} instance.
     */
    @SafeVarargs
    @Contract(pure = true)
    public static <T> @NotNull Consumer<SoftAssertions> softly(
            @NotNull String description,
            @Nullable T value,
            @NotNull Function<? super T, ?> @NotNull ... extracting) {
        return softly -> {
            softly.assertThat(value)
                    .as("Value to extract fields cannot be null.")
                    .isNotNull();
            new FieldsMustBeNullAssertion<>(description, extracting).softlyAssertNull(softly, value);
        };
    }

    /**
     * Asserts that all values extracted using the provided functions are null,
     * adding the assertions softly to the provided {@link SoftAssertions} instance.
     * <br/>
     * This method is part of a validation mechanism to ensure that specific fields
     * of a given value are null during testing.
     * <br/>
     * The assertions are lazily applied, and failures are collected without halting execution.
     * <br/>
     * <ul>
     *     <li>Uses the {@code extracting} functions to access fields or properties of the {@code value}.</li>
     *     <li>Asserts each extracted value to be {@code null}.</li>
     *     <li>Uses the specified description to provide context for any failed assertion.</li>
     * </ul>
     *
     * @param softly the {@link SoftAssertions} instance to collect the null assertions.
     *               <ul>
     *                   <li>Must not be {@code null}.</li>
     *               </ul>
     * @param value  the object whose fields or properties are accessed and tested for null.
     *               <ul>
     *                   <li>Must not be {@code null}.</li>
     *               </ul>
     */
    public void softlyAssertNull(SoftAssertions softly, T value) {
        Stream.of(extracting)
                .map(f -> f.apply(value))
                .forEach(obj -> softly
                        .assertThat(obj)
                        .as("%s found %s", description, obj)
                        .isNull());
    }
}
