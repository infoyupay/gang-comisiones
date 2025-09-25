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

import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import com.yupay.gangcomisiones.logging.LogConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

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
     * Initializes the application environment based on the specified {@link AppMode}.
     * This method ensures the proper setup of directories required for the chosen mode and
     * initializes the logging configuration. If an invalid or unsupported mode is provided,
     * it throws a {@link GangComisionesException}.
     * <br/><br/>
     * Operational behavior:
     * <ul>
     *     <li>For {@link AppMode#WORK} or {@link AppMode#TOY}: Prepares the standard application directories
     *         by calling {@code prepareStandardDirectories()}.</li>
     *     <li>For {@link AppMode#GHOST}: Prepares temporary directories specific to the "ghost" mode
     *         by calling {@code prepareGhostDirectories()}, ensuring a clean state.</li>
     *     <li>For {@code null} or unsupported modes: Throws a {@link GangComisionesException}.</li>
     * </ul>
     * Once directory preparation is completed, it configures log settings using {@code LogConfig.initLogging()}.
     * <br/><br/>
     *
     * @param mode The operational mode of the application. Accepted values are:
     *             <ul>
     *                 <li>{@link AppMode#WORK} - Production mode with real data.</li>
     *                 <li>{@link AppMode#TOY} - Test mode with persistent test data.</li>
     *                 <li>{@link AppMode#GHOST} - Test mode with temporary data reset for each session.</li>
     *             </ul>
     *             <br/>
     *             Providing {@code null} or an unsupported mode will result in an exception.
     * @throws IOException If an I/O error occurs while preparing the directories.
     * @throws GangComisionesException If the provided {@code mode} is {@code null} or unsupported.
     */
    public static void bootstrap(AppMode mode) throws IOException {
        LocalFiles.init(mode);
        switch (mode) {
            case WORK, TOY -> prepareStandardDirectories();
            case GHOST -> prepareGhostDirectories();
            case null, default -> throw new GangComisionesException("Unexpected mode in bootstrap: " + mode);
        }
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
    ///                                                                                 directories.
    private static void prepareStandardDirectories() throws IOException {
        createDirIfMissing(yupay());
        createDirIfMissing(project());
        createDirIfMissing(logs());
    }

    /**
     * Prepares the required "ghost" mode directories by ensuring a clean and isolated environment
     * for temporary application data. This involves clearing any existing files under the ghost
     * directory, recreating it, and setting up subdirectories like logs.
     * <br/><br/>
     * The directory structure for "ghost" mode is reset to mimic a fresh state:
     * <ol>
     *     <li>The root "ghost" directory is identified using {@link LocalFiles#project()}.</li>
     *     <li>If the directory exists, its contents (files and subdirectories) are deleted recursively.</li>
     *     <li>The root "ghost" directory is recreated to ensure it exists.</li>
     *     <li>
     *         The "logs" directory, a subdirectory under the ghost root,
     *         is also created using {@link LocalFiles#logs()}.
     *     </li>
     * </ol>
     * This ensures no residual data or logs are retained between execution sessions in "ghost" mode.
     * <br/><br/>
     *
     *
     * Operational Notes:
     * <ul>
     *     <li>
     *         The method uses {@link Files#walk(Path, FileVisitOption...)} to traverse the directory tree for cleanup.
     *     </li>
     *     <li>The traversal is sorted in reverse order to ensure that child files/directories
     *         are deleted before the parent directory.</li>
     *     <li>Non-critical I/O errors during deletion are ignored to minimize impact on overall
     *         directory preparation.</li>
     * </ul>
     * @throws IOException if an error occurs during file traversal, deletion or directory creation ops.
     */
    private static void prepareGhostDirectories() throws IOException {
        var ghostRoot = project();
        if (Files.exists(ghostRoot)) {
            try(var walk = Files.walk(ghostRoot)){
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p->{
                            try{
                                Files.delete(p);
                            }catch (IOException _){}
                        });
            }
        }else{
            Files.createDirectories(ghostRoot);
        }
        createDirIfMissing(logs());
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
    private static void createDirIfMissing(@NotNull Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}

