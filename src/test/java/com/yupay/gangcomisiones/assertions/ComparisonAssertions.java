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

import java.util.function.Consumer;

/**
 * Utility class providing assertion methods to validate adherence to the equality contract
 * of objects through the use of AssertJ consumers.
 * <br/>
 * The methods within this class allow for validation of {@link Object#equals(Object)} and
 * {@link Object#hashCode()} consistency, along with verifying specific edge cases, such as
 * when objects have {@code null} identifiers.
 * <br/>
 * Features of this class include:
 * <ul>
 *   <li>Static utility methods designed for compatibility with AssertJ's {@link SoftAssertions}.</li>
 *   <li>Non-instantiable class, as it only contains static helper methods.</li>
 *   <li>Facilitates validation of comparison, equality, and hash code behavior in tests for domain-specific objects.</li>
 * </ul>
 * <br/>
 * Designed to enable thorough testing of equality-related behavior while maintaining accessibility and reusability
 * in testing frameworks.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class ComparisonAssertions {

    /**
     * Utility class containing assertion methods for validating the equality contract of objects.
     * <br/>
     * Provides methods for asserting proper implementation of {@link Object#equals(Object)} and {@link Object#hashCode()}
     * as well as specific rules regarding equality behavior for certain object conditions.
     * <br/>
     * This class is designed to be used alongside {@link SoftAssertions} for comprehensive assertion handling and reporting.
     * <br/>
     * Characteristics:
     * <ul>
     *   <li>Contains static utility methods, which return assertion consumers compatible with AssertJ.</li>
     *   <li>Cannot be instantiated, as it is strictly a utility class.</li>
     *   <li>Designed to verify equality constraints and corner cases in object comparisons.</li>
     * </ul>
     * <br/>
     * Intended for use in testing to facilitate validation of equality and hash code behavior in object models
     * within the application's domain logic.
     */
    @Contract(pure = true)
    private ComparisonAssertions() {
    }

    /**
     * Provides a consumer for {@link SoftAssertions} to assert that two items of the same class are equal
     * according to their equality contract, including both the equality comparison and the hash code consistency.
     * <br/>
     * This method verifies the following:
     * <ol>
     *   <li>The two provided items are equal as per their {@link Object#equals(Object)} method.</li>
     *   <li>The two provided items produce the same hash code as per their {@link Object#hashCode()} method.</li>
     * </ol>
     * <br/>
     * These assertions ensure compliance with the equality and hash code contract of the class under test.
     *
     * @param <T>   the type of the objects being compared
     * @param item1 the first item to be compared; must not be null
     * @param item2 the second item to be compared; must not be null
     * @return a consumer that performs the described assertions when applied to a {@link SoftAssertions} instance
     */
    @Contract(pure = true)
    public static <T> @NotNull Consumer<SoftAssertions> assertEqualityContractAndHashcode(T item1, T item2) {
        return softly -> {
            softly.assertThat(item1)
                    .as("Two items of class %s should be equal.", item1.getClass())
                    .isEqualTo(item2);

            softly.assertThat(item1)
                    .as("Two items of class %s should have the same hash code.", item1.getClass())
                    .hasSameHashCodeAs(item2);
        };
    }

    /**
     * Provides a consumer for {@link SoftAssertions} to assert that two items of the same class
     * with {@code null} identifiers are not considered equal.
     * <br/>
     * This method is useful for verifying that the equality rules for objects of the provided class
     * differentiate objects that lack identifiers (e.g., database objects with null primary keys).
     * <br/>
     * <ul>
     * <li>If the two items are of the same class and both have {@code null} as their identifier, this assertion confirms they are not considered equal.</li>
     * <li>Leverages AssertJ's {@link SoftAssertions} for comprehensive assertion reporting.</li>
     * </ul>
     *
     * @param <T>   the type of the objects being compared
     * @param item1 the first item to compare; must be non-null
     * @param item2 the second item to compare; must be non-null
     * @return a consumer that performs the described assertions when applied to a {@link SoftAssertions} instance
     */
    @Contract(pure = true)
    public static <T> @NotNull Consumer<SoftAssertions> assertEqualityContractNotEquals(T item1, T item2) {
        return softly -> softly.assertThat(item1)
                .as("Two items of class %s should not be equal.", item1.getClass())
                .isNotEqualTo(item2);
    }


}
