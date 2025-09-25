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

import com.yupay.gangcomisiones.logging.LogConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

/// This class contains unit tests for the `Bootstrapper` class, specifically testing
/// the behavior of the `bootstrap` method. The tests verify the correct creation of necessary
/// directories, the initialization of logging, and proper handling of I/O exceptions during
/// the bootstrap process.
///
/// The tested scenarios include:
/// - Successful creation of directories and initialization of logging when directories do not exist.
/// - No directory creation when all required directories already exist.
/// - Graceful handling of [IOException] when directory creation fails.
///
/// Mocking techniques are employed to simulate behaviors of file system operations and logging
/// components. Static methods of the [Files] class and the [LogConfig]
/// class are mocked to control and observe their behavior for testing purposes.
///
/// @author InfoYupay SACS
/// @version 1.0
class BootstrapperTest {

    /// Tests the bootstrap process to verify that it correctly creates necessary directories
    /// and initializes logging. This ensures that:
    /// - Non-existent directories are detected and created.
    /// - The logging component is initialized successfully.
    /// The method utilizes mocking to simulate file operations and logging initialization:
    /// - Directory existence checks and creation are mocked using the [Files] API.
    /// - Logging initialization logic is mocked using [LogConfig].
    /// Verification steps:
    /// - Checks directory existence for required paths.
    /// - Ensures appropriate methods to create directories are invoked for paths where they do not exist.
    /// - Confirms that the logging initialization method [#initLogging] is called.
    /// - Validates no additional unexpected operations on the mocks.
    ///
    /// @throws IOException if an error occurs during file operations.
    @Test
    void testBootstrap_CreatesDirectoriesAndInitializesLogging() throws IOException {
        try (var filesMock = Mockito.mockStatic(Files.class);
             var logMock = Mockito.mockStatic(LogConfig.class)) {

            // Simulate non-existent directories
            filesMock.when(() -> Files.notExists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);

            // Simulate logging initialization.
            logMock.when(LogConfig::initLogging).thenAnswer(_ -> null);

            // Run bootstrap
            Bootstrapper.bootstrap(AppMode.TEST);

            // Verify directories were correctly checked and created.
            filesMock.verify(() -> Files.notExists(LocalFiles.yupay()));
            filesMock.verify(() -> Files.createDirectories(LocalFiles.yupay()));

            filesMock.verify(() -> Files.notExists(LocalFiles.project()));
            filesMock.verify(() -> Files.createDirectories(LocalFiles.project()));

            filesMock.verify(() -> Files.notExists(LocalFiles.logs()));
            filesMock.verify(() -> Files.createDirectories(LocalFiles.logs()));

            // Verifying loggin initialization.
            logMock.verify(LogConfig::initLogging);

            // No more interactions.
            filesMock.verifyNoMoreInteractions();
            logMock.verifyNoMoreInteractions();
        }
    }

    /// Tests the bootstrap process when all required directories already exist, ensuring no directories
    /// are created unnecessarily.
    /// This test verifies the following:
    /// - The existence check for all required directories is performed correctly.
    /// - No directory creation is triggered as all required directories exist.
    /// - Logging initialization is invoked and executed successfully.
    /// - No unexpected operations are performed on the mocked file system or logging components.
    /// Mocked behavior:
    /// - File existence checks return `false` for all paths, simulating that directories already exist.
    /// - Logging initialization method does not perform real operations.
    /// Assertions:
    /// - Confirms that `Files.notExists` is called for each required path.
    /// - Validates that `Files.createDirectories` is never called as directories already exist.
    /// - Ensures that the logging initialization method is invoked.
    /// - Verifies no additional interactions occur with the mocked dependencies.
    ///
    /// @throws IOException if an error occurs during execution of mocked file operations.
    @Test
    void testBootstrap_DirectoriesExist_NoCreation() throws IOException {
        try (var filesMock = Mockito.mockStatic(Files.class);
             var logMock = Mockito.mockStatic(LogConfig.class)) {

            // Simulate existing directories
            filesMock.when(() -> Files.notExists(any(Path.class))).thenReturn(false);
            logMock.when(LogConfig::initLogging).thenAnswer(_ -> null);

            Bootstrapper.bootstrap(AppMode.TEST);

            // Verify directories were correctly checked.
            filesMock.verify(() -> Files.notExists(LocalFiles.yupay()));
            filesMock.verify(() -> Files.notExists(LocalFiles.project()));
            filesMock.verify(() -> Files.notExists(LocalFiles.logs()));

            // Check that no directory was created
            filesMock.verify(() -> Files.createDirectories(any(Path.class)), times(0));

            //Check Logging DO initializes
            logMock.verify(LogConfig::initLogging);

            // End of interactions
            filesMock.verifyNoMoreInteractions();
            logMock.verifyNoMoreInteractions();
        }
    }

    /**
     * Tests the behavior of the `bootstrap` method when an {@link IOException} is thrown during the
     * directory creation process.
     *
     * <ul>
     *   <li>Mocks {@link Files} API to simulate non-existent directories and an {@link IOException}
     *       when attempting to create them.</li>
     *   <li>Uses {@link MockedStatic} to mock static methods in {@link Files} and {@link LogConfig}.</li>
     *   <li>Verifies that the exception thrown contains the expected message.</li>
     *   <li>Ensures the correct methods are invoked on {@link Files} to check directory existence
     *       and attempt creation, and confirms that logging is not initialized.</li>
     * </ul>
     * <p>
     * Assertions:
     * <ul>
     *   <li>Validates that {@link IOException} is thrown with the message "Test IOException".</li>
     *   <li>Ensures that {@code Files.notExists()} and {@code Files.createDirectories()} are called
     *       as expected before failure.</li>
     *   <li>Confirms that {@link LogConfig#initLogging()} is not invoked since directory creation
     *       fails prematurely.</li>
     *   <li>Validates no additional interactions occur with the mocked dependencies.</li>
     * </ul>
     * <p>
     * This test ensures that the bootstrap process handles I/O errors gracefully and does not
     * proceed with logging initialization or other operations when directory creation fails.
     */
    @Test
    void testBootstrap_ThrowsIOException() {
        try (var filesMock = Mockito.mockStatic(Files.class);
             var logMock = Mockito.mockStatic(LogConfig.class)) {

            filesMock.when(() -> Files.notExists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Test IOException"));

            var exception = assertThrows(IOException.class, () -> Bootstrapper.bootstrap(AppMode.TEST));
            assertEquals("Test IOException", exception.getMessage());

            // Verifies that directories were created prior to failure.
            filesMock.verify(() -> Files.notExists(LocalFiles.yupay()));
            filesMock.verify(() -> Files.createDirectories(any(Path.class)));

            //Verify no logging initialization was attempted.
            logMock.verifyNoInteractions();
            filesMock.verifyNoMoreInteractions();
        }
    }
}
