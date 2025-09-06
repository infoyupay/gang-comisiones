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

package com.yupay.gangcomisiones.usecase.install;

import com.yupay.gangcomisiones.exceptions.AppInstallationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Defines installation-related operations for the application lifecycle, such as unpacking
 * installation bundles and verifying persistence prerequisites.
 * <br/>
 * Typical responsibilities include:
 * <ul>
 *   <li>Providing an I/O-oriented executor for background tasks.</li>
 *   <li>Unpacking ZIP archives into the designated installation directory with path traversal protection.</li>
 *   <li>Checking whether the persistence configuration already exists.</li>
 *   <li>Offering a convenience asynchronous wrapper for blocking operations.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface InstallationService {

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
    void unpackZip(Path zipPath) throws IOException;

    /**
     * Indicates whether the persistence configuration is already present on the local machine.
     *
     * @return {@code true} if the persistence configuration resource exists; {@code false} otherwise.
     */
    boolean persistenceExists();

    /**
     * Asynchronously unpacks the provided ZIP archive using the I/O executor.
     * <br/>
     * Any {@link IOException} thrown by the underlying unpack operation is wrapped into an
     * {@link AppInstallationException} and completes the returned future exceptionally.
     *
     * @param zipPath the path to the ZIP file to unpack; must point to a readable file.
     * @return a {@link CompletableFuture} that completes when unpacking finishes, or completes exceptionally on failure.
     */
    default CompletableFuture<Void> unpackZipAsync(Path zipPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                unpackZip(zipPath);
            } catch (IOException e) {
                throw new AppInstallationException("Unable to unpack zip file " + zipPath, e);
            }
        }, ioExecutor());
    }
}
