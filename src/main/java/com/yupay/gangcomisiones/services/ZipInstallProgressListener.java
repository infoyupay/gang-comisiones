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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Listener interface for receiving progress notifications during ZIP installation.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ZipInstallProgressListener {

    /**
     * Creates a no-operation implementation of {@link ZipInstallProgressListener}.
     * This implementation does nothing and is intended for use as a default or placeholder.
     *
     * @return a new instance of {@link ZipInstallProgressListener} that performs no actions.
     */
    @Contract(value = " -> new", pure = true)
     static @NotNull ZipInstallProgressListener noOp() {
        return new ZipInstallProgressListener() {
            @Override
            public void onStart(int totalEntries) {

            }

            @Override
            public void onEntryProcessed(String entryName, Path outputPath, int entriesProcessed) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Exception error) {

            }
        };
    }

    /**
     * Called when the installation process starts.
     *
     * @param totalEntries the total number of entries in the ZIP archive, or {@code -1}
     *                     if the number of entries is not known in advance.
     */
    void onStart(int totalEntries);

    /**
     * Called after each entry has been successfully processed.
     *
     * @param entryName        the name of the entry just processed.
     * @param outputPath       the resolved output path on the local filesystem.
     * @param entriesProcessed how many entries have been processed so far.
     */
    void onEntryProcessed(String entryName, Path outputPath, int entriesProcessed);

    /**
     * Called when the installation process completes successfully.
     */
    void onComplete();

    /**
     * Called if the installation process fails with an exception.
     *
     * @param error the exception that caused the failure.
     */
    void onError(Exception error);
}
