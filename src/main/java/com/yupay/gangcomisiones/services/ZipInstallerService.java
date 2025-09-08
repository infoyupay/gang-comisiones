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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.exceptions.AppInstalationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Service interface responsible for managing the installation process of ZIP archives
 * into a designated installation directory. This includes unpacking ZIP files, ensuring
 * safety against ZIP Slip vulnerabilities, and notifying progress via a listener.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ZipInstallerService {

    /**
     * Returns the executor intended for I/O-bound installation tasks.
     *
     * @return the {@link ExecutorService} used to run background I/O operations.
     */
    ExecutorService ioExecutor();

    /**
     * Unpacks the provided ZIP archive into the installation directory.
     * <br/>
     * Implementations must guard against ZIP Slip by rejecting entries whose normalized
     * output path would escape the installation root directory.
     *
     * @param zipPath the path to the ZIP file to unpack; must point to a readable file.
     * @throws IOException if an I/O error occurs while reading the archive or writing entries.
     */
    default void unpackZip(Path zipPath) throws IOException {
        unpackZip(zipPath, ZipInstallProgressListener.noOp());
    }

    /**
     * Unpacks the provided ZIP archive into the installation directory, notifying the listener
     * of progress and completion.
     * <br/>
     * Implementations must guard against ZIP Slip by rejecting entries whose normalized
     * output path would escape the installation root directory.
     *
     * @param zipPath  the path to the ZIP file to unpack; must point to a readable file.
     * @param listener the listener to notify of progress and completion.
     * @throws IOException if an I/O error occurs while reading the archive or writing entries.
     */
    void unpackZip(Path zipPath, @NotNull ZipInstallProgressListener listener) throws IOException;

    /**
     * Indicates whether the persistence configuration is already present on the local machine.
     *
     * @return {@code true} if the persistence configuration resource exists; {@code false} otherwise.
     */
    boolean persistenceExists();

    /**
     * Asynchronously unpacks the provided ZIP archive into the installation directory, notifying the
     * given listener of progress and completion. This method wraps the blocking {@code unpackZip}
     * operation in a {@link CompletableFuture}, allowing it to execute in a background thread
     * provided by {@code ioExecutor()}.
     *
     * @param zipPath  the path to the ZIP file to unpack; must point to a readable file.
     * @param listener the listener to notify of progress and completion, or {@code null} if no
     *                 notifications are required.
     * @return a {@link CompletableFuture} that completes when the operation finishes, or completes
     * exceptionally if an error occurs.
     */
    default CompletableFuture<Void> unpackZipAsync(Path zipPath, @NotNull ZipInstallProgressListener listener) {
        return CompletableFuture.runAsync(() -> {
            try {
                unpackZip(zipPath, listener);
            } catch (IOException | RuntimeException e) {
                throw new AppInstalationException("Unable to unpack zip file " + zipPath, e);
            }
        }, ioExecutor());
    }

    /**
     * Asynchronously unpacks the provided ZIP archive into the installation directory
     * without specifying a listener for progress or completion notifications. This method
     * wraps the blocking {@code unpackZip} operation in a {@link CompletableFuture},
     * allowing it to execute in a background thread provided by {@code ioExecutor()}.
     *
     * @param zipPath the path to the ZIP file to unpack; must point to a readable file.
     * @return a {@link CompletableFuture} that completes when the operation finishes, or completes
     * exceptionally if an error occurs.
     */
    default CompletableFuture<Void> unpackZipAsync(Path zipPath) {
        return unpackZipAsync(zipPath, ZipInstallProgressListener.noOp());
    }
}
