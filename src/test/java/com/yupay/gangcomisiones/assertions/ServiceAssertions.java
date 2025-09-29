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

import org.assertj.core.api.IterableAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * The {@code ServiceAssertions} class provides utility methods for asserting
 * specific conditions on service-provided asynchronous data. These methods
 * are intended to simplify unit testing by ensuring data consistency and
 * expected results for asynchronous operations.
 * <br/>
 * Core functionality includes:
 * <ul>
 *   <li>Validation of non-nullness for provided data.</li>
 *   <li>Assertions to check the presence of specific items in asynchronous collections.</li>
 *   <li>Support for fluent assertion chaining using AssertJ's {@link IterableAssert}.</li>
 * </ul>
 * <p>
 * <br/>
 * Designed for use in contexts where service APIs return data wrapped in
 * asynchronous constructs like {@link CompletableFuture}.
 * <br/>
 * Example features include:
 * <ol>
 *   <li>Direct integration with asynchronous service calls.</li>
 *   <li>Simplified validation against expected results.</li>
 * </ol>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class ServiceAssertions {
    /**
     * Constructor that avoids instanciation of a utility class.
     */
    @Contract(pure = true)
    private ServiceAssertions() {
    }

    /**
     * Asserts that a given item with a specified identifier is present within a list
     * that is provided asynchronously.
     * <br/>
     * The method validates based on the following:
     * <ul>
     *   <li>The list of items is not null.</li>
     *   <li>An item matching the provided identifier is present within the list.</li>
     * </ul>
     * Usage example:
     * {@snippet java:
     *      var persisted = service.create("Somebody To Love", 400, true);
     *      ServiceAssertions.assertIsListed(
     *               service::listAllActive,
     *               persisted,
     *               "allActive");
     *}
     *
     * @param futureSupplier a {@link Supplier} that provides a {@link CompletableFuture}
     *                       resolving to the iterable list of items to be checked.
     *                       Must not be null.
     * @param value          the item to be checked.
     * @param listName       an {@link Object} representing the name of the list being validated.
     * @param <T>            type of item to list.
     */
    public static <T> void assertListed(
            @NotNull Supplier<CompletableFuture<Iterable<T>>> futureSupplier,
            T value,
            String listName) {
        var list = futureSupplier.get().join();
        assertThat(list)
                .as("Listed items cannot be null.")
                .isNotNull();
        assertThat(list)
                .as("Expected in %s the item %s.", listName, value)
                .contains(value);
    }
}
