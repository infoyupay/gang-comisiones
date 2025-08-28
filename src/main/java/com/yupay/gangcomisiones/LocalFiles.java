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

import java.nio.file.Path;

/**
 * Provides constants representing file and directory paths used
 * within the Gang-Comisiones project.
 * <br/>
 * This class defines paths to specific application and project directories
 * within the user's file system, allowing for consistent and centralized
 * management of these locations.
 *
 * @author InfoYupay SACS
 * @version 1.0
 *
 */
public class LocalFiles {
    /**
     * Represents the path to the root directory for Yupay important files
     * within the user's home directory. The directory is named ".yupay".
     * <br/>
     * This constant is used as a base path for other application-specific
     * file and directory paths.
     */
    public static final Path YUPAY = Path.of(System.getProperty("user.home"), ".yupay");

    /**
     * Represents the path to the root directory for Gang-Comisiones project files
     * within the Yupay directory. The directory is named "gang-comisiones".
     * <br/>
     * This constant is used as a base path for other project-specific
     * file and directory paths.
     */
    public static final Path PROJECT = YUPAY.resolve("gang-comisiones");

    /**
     * Represents the path to the persistence properties file for the Gang-Comisiones project.
     * This file contains configuration settings for the Jakarta Persistence API (JPA).
     * <br/>
     * This constant is used to locate the JPA properties file within the project directory.
     */
    public static final Path JPA_PROPERTIES = PROJECT.resolve("persistence.properties");
    /**
     * Represents the path to the logs directory for the Gang-Comisiones project.
     * This directory is used to store log files generated during the application's execution.
     * <br/>
     * This constant is used to locate the logs directory within the project directory.
     */
    public static final Path LOGS = PROJECT.resolve("logs");
}
