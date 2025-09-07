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

package com.yupay.gangcomisiones.services.impl;

import com.yupay.gangcomisiones.LocalFiles;
import com.yupay.gangcomisiones.services.ZipInstallProgressListener;
import com.yupay.gangcomisiones.services.ZipInstallerService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Implementation of the {@link ZipInstallerService} interface that handles
 * installation-related tasks specific to the local environment.
 * <br/>
 * This record provides methods for unpacking ZIP files into a designated
 * installation directory and checking the existence of persistence configuration files.
 *
 * @param ioExecutor Executor service for I/O operations.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record ZipInstallerServiceLocalImpl(@NotNull ExecutorService ioExecutor)
        implements ZipInstallerService {
    private static final Logger LOG = LoggerFactory.getLogger(ZipInstallerServiceLocalImpl.class);

    @Override
    public void unpackZip(Path zipPath, @NotNull ZipInstallProgressListener listener) throws IOException {
        int processed = 0;
        try (var is = Files.newInputStream(zipPath);
             var zis = new ZipInputStream(is)) {

            var installDir = LocalFiles.PROJECT.asPath();
            listener.onStart(-1); // con ZipInputStream no sabemos el total de entradas

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                var outPath = installDir.resolve(entry.getName()).normalize();
                // zip slip protection
                if (!outPath.startsWith(installDir)) {
                    LOG.warn("Skipped potentially unsafe entry: {}", entry.getName());
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                    LOG.info("Create directory: {}", outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                    LOG.info("Uncompressed file: {}", outPath);
                }

                processed++;
                listener.onEntryProcessed(entry.getName(), outPath, processed);
            }

            listener.onComplete();
        } catch (IOException | RuntimeException e) {
            listener.onError(e);
            throw e;
        }
    }

    @Override
    public boolean persistenceExists() {
        return Files.exists(LocalFiles.JPA_PROPERTIES.asPath());
    }
}
