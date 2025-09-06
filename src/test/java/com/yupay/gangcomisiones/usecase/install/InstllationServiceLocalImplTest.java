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

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.LocalFiles;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the local installation service behavior related to ZIP unpacking.
 * <br/>
 * These tests verify that:
 * <ul>
 *   <li>Valid ZIP archives are unpacked into the project folder, creating expected directories and files.</li>
 *   <li>ZIP Slip attempts are prevented (entries with path traversal are ignored).</li>
 *   <li>Asynchronous unpack operations complete successfully and produce the same results as the synchronous variant.</li>
 * </ul>
 * dvidal tested and passed 3 test in 68ms at 2025-09-05T12:34-05:00 (UTC-5).
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class InstallationServiceLocalImplTest extends AbstractPostgreIntegrationTest {

    private InstallationService installationService;

    /**
     * Initializes the installation service from the application context prior to each test execution.
     * <br/>
     * This ensures a fresh service instance and a consistent environment across test cases.
     */
    @BeforeEach
    void setUp() {
        installationService = AppContext.getInstance().getInstallationService();
    }

    /**
     * Verifies that unpacking a valid ZIP file creates the expected directory structure and files
     * inside the project folder.
     * <br/>
     * The test performs the following steps:
     * <ol>
     *   <li>Redirects the user home to a temporary directory and resets path holders.</li>
     *   <li>Creates the project directory and a small ZIP containing a folder and a file.</li>
     *   <li>Invokes the unpack operation.</li>
     *   <li>Asserts that the expected directory and file exist within the project path.</li>
     * </ol>
     *
     * @param tempDir a JUnit-managed temporary directory used to isolate filesystem side effects.
     * @throws IOException if an I/O error occurs while creating the test ZIP file.
     */
    @Test
    void testUnpackZip_createsDirectoriesAndFiles(@TempDir @NotNull Path tempDir) throws IOException {
        String originalUserHome = System.getProperty("user.home");
        try {
            // Redirect LocalFiles to use tempDir
            System.setProperty("user.home", tempDir.toString());
            LocalFiles.YUPAY.reset();
            LocalFiles.PROJECT.reset();
            LocalFiles.LOGS.reset();
            LocalFiles.JPA_PROPERTIES.reset();
            Files.createDirectories(LocalFiles.PROJECT.asPath());


            Path zip = createTestZip(tempDir);

            // Act
            installationService.unpackZip(zip);

            // Assert
            Path projectDir = LocalFiles.PROJECT.asPath();
            assertTrue(Files.exists(projectDir.resolve("subdir")));
            assertTrue(Files.exists(projectDir.resolve("subdir/file.txt")));
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    /**
     * Ensures that the unpack operation prevents ZIP Slip (path traversal) attempts.
     * <br/>
     * The test:
     * <ul>
     *   <li>Builds a ZIP archive with an entry attempting to escape the project directory (e.g., "../outside.txt").</li>
     *   <li>Runs the unpack routine.</li>
     *   <li>Asserts that no file is created outside the project directory and the project folder remains intact.</li>
     * </ul>
     *
     * @param tempDir a JUnit-managed temporary directory used to isolate filesystem side effects.
     * @throws IOException if an I/O error occurs while creating the malicious ZIP file.
     */
    @Test
    void testUnpackZip_preventsZipSlip(@TempDir @NotNull Path tempDir) throws IOException {
        String originalUserHome = System.getProperty("user.home");
        try {
            // Redirect LocalFiles to use tempDir
            System.setProperty("user.home", tempDir.toString());
            LocalFiles.YUPAY.reset();
            LocalFiles.PROJECT.reset();
            LocalFiles.LOGS.reset();
            LocalFiles.JPA_PROPERTIES.reset();
            Files.createDirectories(LocalFiles.PROJECT.asPath());


            Path maliciousZip = createMaliciousZip(tempDir);

            // Act
            installationService.unpackZip(maliciousZip);

            // Assert: file outside project not created
            Path outsideFile = LocalFiles.PROJECT.asPath().getParent().resolve("outside.txt");
            assertFalse(Files.exists(outsideFile), "Malicious entry should be skipped");

            // Also, check PROJECT folder is still intact
            assertTrue(Files.exists(LocalFiles.PROJECT.asPath()));
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    /**
     * Confirms that the asynchronous unpack operation completes successfully and produces
     * the expected directory and file within the project folder.
     * <br/>
     * The test performs:
     * <ol>
     *   <li>Environment redirection to a temporary user home and path resets.</li>
     *   <li>Creation of a valid test ZIP archive.</li>
     *   <li>Invocation of the asynchronous unpack and awaiting completion.</li>
     *   <li>Assertions verifying directory and file creation in the project path.</li>
     * </ol>
     *
     * @param tempDir a JUnit-managed temporary directory used to isolate filesystem side effects.
     * @throws Exception if waiting for the asynchronous operation fails unexpectedly.
     */
    @Test
    void testUnpackZipAsync_completesSuccessfully(@TempDir @NotNull Path tempDir) throws Exception {
        String originalUserHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tempDir.toString());
            LocalFiles.YUPAY.reset();
            LocalFiles.PROJECT.reset();
            LocalFiles.LOGS.reset();
            LocalFiles.JPA_PROPERTIES.reset();
            Files.createDirectories(LocalFiles.PROJECT.asPath());


            Path zip = createTestZip(tempDir);

            // Act
            CompletableFuture<Void> future = installationService.unpackZipAsync(zip);
            future.join(); // wait for task to end

            // Assert
            Path projectDir = LocalFiles.PROJECT.asPath();
            assertTrue(Files.exists(projectDir.resolve("subdir")));
            assertTrue(Files.exists(projectDir.resolve("subdir/file.txt")));
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    /* --------------------- Helpers --------------------- */

    /**
     * Creates a temporary ZIP file containing a predefined directory and file structure
     * for use in testing scenarios. The generated ZIP includes a folder and a text file
     * with sample content.
     *
     * @param tempDir the temporary directory where the ZIP file will be created
     * @return the path to the generated ZIP file
     * @throws IOException if an error occurs while creating or writing to the ZIP file
     */
    private @NotNull Path createTestZip(@NotNull Path tempDir) throws IOException {
        Path zipPath = tempDir.resolve("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            // Folder
            zos.putNextEntry(new ZipEntry("subdir/"));
            zos.closeEntry();

            // File
            zos.putNextEntry(new ZipEntry("subdir/file.txt"));
            zos.write("Hello, world!".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return zipPath;
    }

    /**
     * Creates a malicious ZIP file containing an entry that attempts to perform a path traversal attack.
     * The generated ZIP file includes an entry with a file path outside the target directory structure.
     *
     * @param tempDir the temporary directory where the ZIP file will be created
     * @return the path to the malicious ZIP file
     * @throws IOException if an error occurs while creating or writing to the ZIP file
     */
    private @NotNull Path createMaliciousZip(@NotNull Path tempDir) throws IOException {
        Path zipPath = tempDir.resolve("malicious.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            // Zip Slip mock
            zos.putNextEntry(new ZipEntry("../outside.txt"));
            zos.write("Malicious content".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return zipPath;
    }
}

