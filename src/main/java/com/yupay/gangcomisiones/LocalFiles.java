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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
 * @implNote <strong>⚠️IMPORTANT⚠️</strong> You must call {@link #init(AppMode)} or {@link #init(AppMode, Path)} before
 * accessing any other methods. Otherwise, {@link #project()} and its derived paths may be undefined.
 *
 */
public final class LocalFiles {
    private static Path PROJECT;
    private static Path USER_HOME;

    /**
     * Constructs a private instance of the {@code LocalFiles} class.
     * <br/>
     * This constructor is private to enforce the non-instantiability of the class.
     * <br/>
     * <ul>
     *     <li>This class contains only static utility methods and constants.</li>
     *     <li>It is designed to group application-specific path and file configurations.</li>
     * </ul>
     * <p>
     * <br/>
     * By marking the class as non-instantiable:
     * <ol>
     *     <li>Accidental instantiation is prevented.</li>
     *     <li>The class adheres to the principle of utility-focused static methods.</li>
     * </ol>
     */
    @Contract(pure = true)
    private LocalFiles() {
    }

    /**
     * Initializes application-specific paths using a default user's home directory.
     * This method delegates to {@link #init(AppMode, Path)} internally, using the system-defined home directory.
     * <br/><br/>
     * The following {@link AppMode} options determine the behavior:
     * <ul>
     *     <li>{@link AppMode#WORK}: Configures the project path to the production directory {@code "gang-comisiones"}.</li>
     *     <li>{@link AppMode#TOY}: Appends {@code "sandbox.0"} to the production directory path.</li>
     *     <li>{@link AppMode#GHOST}: Appends {@code "sandbox.tmp"} to the production directory path.</li>
     * </ul>
     *
     * @param mode The {@link AppMode} that determines how to configure project paths.
     *             <ul>
     *                <li>Must not be {@code null}.</li>
     *             </ul>
     */
    public static void init(AppMode mode) {
        init(mode, Path.of(System.getProperty("user.home")));
    }


    /**
     * Initializes application-specific paths based on the provided {@link AppMode} and user's home directory.
     * <br/><br/>
     * This method configures the project directory path by resolving it relative to the `.yupay` folder
     * in the user's home directory. The resolved path varies depending on the specified {@link AppMode}.
     * <br/><br/>
     * The following {@link AppMode} options determine the behavior:
     * <ul>
     *     <li>{@link AppMode#WORK}: Sets the project path to the production directory {@code "gang-comisiones"}. </li>
     *     <li>{@link AppMode#TOY}: Appends {@code "sandbox.0"} to the production directory path.</li>
     *     <li>{@link AppMode#GHOST}: Appends {@code "sandbox.tmp"} to the production directory path.</li>
     * </ul>
     *
     * @param mode     The {@link AppMode} that determines how the project directory path is configured.
     *                             <ul>
     *                               <li>Must not be {@code null}.</li>
     *                             </ul>
     *                 <br/>
     * @param userHome The path to the user's home directory, used as the base path for resolving the project directory
     *                 and other application-specific paths.
     *                 <ul>
     *                   <li>Must not be {@code null}.</li>
     *                   <li>Typically resolves using {@code System.getProperty("user.home")}.</li>
     *                 </ul>
     * @throws NullPointerException if {@code userHome} is {@code null}.
     */
    public static void init(@NotNull AppMode mode, @NotNull Path userHome) {
        USER_HOME = userHome;
        var yupay = yupay();

        switch (mode) {
            case WORK -> PROJECT = yupay.resolve("gang-comisiones");
            case TOY -> PROJECT = yupay.resolve("gang-comisiones", "sandbox.0");
            case GHOST -> PROJECT = yupay.resolve("gang-comisiones", "sandbox.tmp");
        }
    }

    /**
     * Resolves {@code user.home/.yupay} path, which is the common
     * path for infoyupay projects files.
     *
     * @return infoyupay files path.
     */
    @Contract(pure = true)
    public static @NotNull Path yupay() {
        return USER_HOME.resolve(".yupay");
    }

    /**
     * Retrieves the resolved project path.
     *
     * @return the resolved project path.
     * @see #init(AppMode, Path)
     */
    @Contract(pure = true)
    public static Path project() {
        return PROJECT;
    }

    /**
     * Resolves the path {@link #project()}/persistence.properties which contains
     * connection strings and other JPA configuration which is not included in persistence.xml
     *
     * @return the resolved path.
     */
    @Contract(pure = true)
    public static @NotNull Path jpaProperties() {
        return PROJECT.resolve("persistence.properties");
    }

    /**
     * Resolves the path {@link #project()}/logs
     *
     * @return the resolved path.
     */
    @Contract(pure = true)
    public static @NotNull Path logs() {
        return PROJECT.resolve("logs");
    }
}
