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

public interface InstallationService {

    ExecutorService ioExecutor();

    void unpackZip(Path zipPath) throws IOException;

    boolean persistenceExists();

    default CompletableFuture<Void> unpackZipAsync(Path zipPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                unpackZip(zipPath);
            } catch (IOException e) {
                throw new AppInstallationException("Unable to unpack zip file " + zipPath, e);
            }
        });
    }
}
