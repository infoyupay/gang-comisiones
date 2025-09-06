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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@code PathHolder} class, particularly focusing on its ability to
 * handle dynamic updates to the root path and caching behavior for hierarchical paths.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class PathHolderTest {

    /**
     * Tests the behavior of {@link PathHolder#asPath()} when the system property "user.home" changes.
     * Verifies that the {@link Path} returned by {@link PathHolder#asPath()} updates correctly
     * after the user home directory is modified and the {@link PathHolder} is reset.
     *
     * @param tempDir a temporary directory provided by JUnit, used to simulate
     *                a change in the "user.home" system property
     */
    @Test
    void testAsPath_WithUserHomeChange(@TempDir @NotNull Path tempDir) {
        // Arrange
        String originalUserHome = System.getProperty("user.home");

        try {
            // Use original user.home
            PathHolder pathHolder = PathHolder.ofRoot(() -> System.getProperty("user.home"), "subdir");

            // Assert initial Path points to original user.home
            Path initialPath = pathHolder.asPath();
            assertEquals(Path.of(originalUserHome, "subdir"), initialPath);

            // Change user.home to temporary directory
            System.setProperty("user.home", tempDir.toString());

            // Reset pathHolder and recompute Path
            pathHolder.reset();
            Path updatedPath = pathHolder.asPath();

            // Assert updated Path points to new temporary directory
            assertEquals(tempDir.resolve("subdir"), updatedPath);

        } finally {
            // Restore original user.home
            System.setProperty("user.home", originalUserHome);
        }
    }
}
