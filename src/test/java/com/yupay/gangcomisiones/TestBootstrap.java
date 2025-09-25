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

package com.yupay.gangcomisiones;import com.yupay.gangcomisiones.logging.LogConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a utility class for initializing the application's local file structure
 * in a controlled test environment.
 * <br/>
 * This class simplifies the configuration and setup of file paths and logging for
 * the test mode of the application.
 * <br/>
 * Key responsibilities include:
 * <ol>
 *     <li>Setting up application-specific directories and files for test purposes.</li>
 *     <li>Ensuring the logging configuration is initialized properly.</li>
 *     <li>Providing a standardized process for initializing and testing the local
 *         file structure.</li>
 * </ol>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class TestBootstrap {
    /**
     * Initializes the local file structure necessary for the application in test mode.
     * This method performs the following operations:
     * <ul>
     *     <li>Initializes application-specific paths based on the provided {@code path} in test mode
     *     using {@link LocalFiles#init(AppMode, Path)}.</li>
     *     <li>Creates the directories for storing log files at the path resolved by {@link LocalFiles#logs()}.</li>
     *     <li>Copies a dummy JPA properties file to the path resolved by {@link LocalFiles#jpaProperties()}.</li>
     *     <li>Initializes the logging configuration by invoking {@link LogConfig#initLogging()}.</li>
     * </ul>
     *
     * @param path The base path used for initializing the local application-specific paths.
     *             <ul>
     *                 <li>Must not be {@code null}.</li>
     *                 <li>Typically represents a root directory to configure the test environment.</li>
     *             </ul>
     * @throws IOException If an I/O error occurs while creating directories or copying files.
     */
    public static void bootstrap(Path path) throws IOException {
        LocalFiles.init(AppMode.TEST, path);
        Files.createDirectories(LocalFiles.logs());
        Files.copy(DummyHelpers.getDummyJpaProperties(), LocalFiles.jpaProperties());
        LogConfig.initLogging();
    }
}
