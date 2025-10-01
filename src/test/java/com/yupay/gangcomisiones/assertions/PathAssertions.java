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

import org.jetbrains.annotations.Contract;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Provides custom assertion helpers for {@link java.nio.file.Path} objects
 * to improve readability and expressiveness in tests.
 * <br/>This utility class complements {@link org.assertj.core.api.PathAssert}
 * by adding domain-specific assertions commonly required when verifying
 * file system operations.
 * <br/>Currently, the following helpers are available:
 * <ul>
 *   <li>{@link #assertFileWasCreated(java.nio.file.Path)}: verifies that a path
 *   points to an existing regular file</li>
 *   <li>{@link #assertDirWasCreated(java.nio.file.Path)}: verifies that a path
 *   points to an existing directory</li>
 * </ul>
 * <br/>All methods in this class throw {@link org.opentest4j.AssertionFailedError}
 * if the assertion fails, ensuring smooth integration with JUnit 5.
 *
 * @author InfoYupay SACS
 * @version 1.0
 * @implNote This is a utility class and cannot be instantiated.
 * The existence check is implicit in {@link org.assertj.core.api.AbstractPathAssert#isRegularFile()}
 * and {@link org.assertj.core.api.AbstractPathAssert#isDirectory()}, so explicit calls
 * to {@code exists()} are not required in these helpers.
 */
public class PathAssertions {
    /**
     * Private constructor to prevent instantiation.
     */
    @Contract(pure = true)
    private PathAssertions() {
    }

    /**
     * Asserts that the given {@link Path} points to a regular file that exists.
     * <br/>This method performs the following checks in order:
     * <ul>
     *   <li>The {@link Path} is not {@code null}</li>
     *   <li>The {@link Path} exists</li>
     *   <li>The {@link Path} is a regular file</li>
     * </ul>
     * The assertion message includes the file name if available.
     *
     * @param path the {@link Path} to validate
     * @implNote The existence check is implicit in {@link org.assertj.core.api.AbstractPathAssert#isRegularFile()},
     * so calling {@code exists()} explicitly is not required.
     */
    public static void assertFileWasCreated(Path path) {
        assertThat(path)
                .isNotNull()
                .as("File %s should have been created.", path.getFileName())
                //.exists() check implicit in isRegularFile
                .isRegularFile();
    }

    /**
     * Asserts that the given {@link Path} points to a directory that exists.
     * <br/>This method performs the following checks in order:
     * <ul>
     *   <li>The {@link Path} is not {@code null}</li>
     *   <li>The {@link Path} exists</li>
     *   <li>The {@link Path} is a directory</li>
     * </ul>
     * The assertion message includes the directory name if available.
     *
     * @param path the {@link Path} to validate
     * @implNote The existence check is implicit in {@link org.assertj.core.api.AbstractPathAssert#isDirectory()},
     * so calling {@code exists()} explicitly is not required.
     */
    public static void assertDirWasCreated(Path path) {
        assertThat(path)
                .isNotNull()
                .as("Directory %s should have been created.", path.getFileName())
                //.exists() check implicit in isDirectory
                .isDirectory();
    }

}
