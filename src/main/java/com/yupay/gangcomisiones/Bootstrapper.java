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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.yupay.gangcomisiones.LocalFiles.*;

/**
 * Performs mandatory bootstrap tasks before launching the application.
 * <br/>
 * Ensures required directories exist and initializes logging.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class Bootstrapper {
    /**
     * Constructs a new instance of the Bootstrapper class.
     * <br/>
     * This constructor is private and marked as pure to prevent instantiation
     * of the Bootstrapper class. The Bootstrapper class is designed to only
     * provide static bootstrap functionality and should not be instantiated.
     */
    @Contract(pure = true)
    private Bootstrapper() {
        //hide initialization.
    }

    /**
     * Executes the bootstrap sequence.
     *
     * @throws IOException if directories cannot be created or
     *                     logging initialization fails due to I/O errors.
     */
    public static void bootstrap() throws IOException {
        prepareDirectories();
        LogConfig.initLogging();
    }

    /// Ensures the existence of required application directories by creating them
    /// if they are missing. This is a critical step before application initialization,
    /// as these directories are necessary for proper functioning of the system.
    /// The directories include:
    /// - YUPAY: Directory for Yupay-specific files or configurations.
    /// - PROJECT: Directory for general project files or configurations.
    /// - LOGS: Directory for log storage.
    /// This method relies on the [#createDirIfMissing(Path)] helper method to
    /// create each directory if it does not exist.
    ///
    /// @throws IOException if an I/O error occurs while attempting to create the required
    ///                                                             directories.
    private static void prepareDirectories() throws IOException {
        createDirIfMissing(YUPAY);
        createDirIfMissing(PROJECT);
        createDirIfMissing(LOGS);
    }

    /**
     * Ensures the existence of a directory represented by the provided {@code PathHolder}.
     * If the directory does not exist, it is created, including any necessary but non-existent
     * parent directories.
     *
     * @param path the {@code PathHolder} instance representing the directory path to check
     *             or create. The {@link PathHolder#asPath()} method is used to resolve the
     *             actual path.
     * @throws IOException if an I/O error occurs while checking for the directory or attempting
     *                     to create it.
     */
    private static void createDirIfMissing(@NotNull PathHolder path) throws IOException {
        var p = path.asPath();
        if (Files.notExists(p)) {
            Files.createDirectories(p);
        }
    }
}

