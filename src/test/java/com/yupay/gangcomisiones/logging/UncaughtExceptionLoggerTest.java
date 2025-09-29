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

package com.yupay.gangcomisiones.logging;

import com.yupay.gangcomisiones.LocalFiles;
import com.yupay.gangcomisiones.TestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for validating the functionality of {@link UncaughtExceptionLogger}.
 * <br/>
 * Specifically, it ensures that uncaught exceptions are appropriately logged
 * when the logger is used as the uncaught exception handler for threads.
 * <br/>
 * This test suite comprises setup operations and behavior validation through test cases.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px;">
 *     <strong>Execution Note:</strong> dvidal@infoyupay.com passed 1 test in 1.104 at 2025-09-28 19:55 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UncaughtExceptionLoggerTest {

    /**
     * Sets up the test environment prior to the execution of all test cases.
     * This method initializes application-specific paths and configurations
     * required for the test mode.
     * <br/>
     * Invokes {@link TestBootstrap#bootstrap(Path)} to perform directory creation,
     * file setup, and logging configuration.
     *
     * @param tmp A temporary directory provided by the {@link TempDir} annotation.
     *            <ul>
     *                <li>Must not be {@code null}.</li>
     *                <li>Acts as a base directory for creating application-specific paths during tests.</li>
     *            </ul>
     * @throws IOException If an I/O error occurs during the setup process.
     */
    @BeforeAll
    static void setup(@TempDir Path tmp) throws IOException {
        TestBootstrap.bootstrap(tmp);
    }

    /**
     * Throws a RuntimeException in a test thread, and checks that
     * said exception was correctly logged with {@link UncaughtExceptionLogger}
     *
     * @throws IOException          if cannot perform files writing.
     * @throws InterruptedException if thread cannot be joined.
     */
    @Test
    void testUncaughtExceptionIsLogged() throws IOException, InterruptedException {
        var LOG_DIR = LocalFiles.logs();
        var LOG_FILE = LOG_DIR.resolve("gang-comisiones.log");

        var thread = new Thread(() -> {
            throw new RuntimeException("Test uncaught exception");
        });

        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger());
        thread.start();
        thread.join();

        var exists = false;
        var retries = 5;
        for (var i = 0; i < retries; i++) {
            if (Files.exists(LOG_FILE) && Files.size(LOG_FILE) > 0) {
                exists = true;
                break;
            }
            Thread.sleep(200);
        }

        assertThat(exists)
                .as("Log file should exist after uncaught exception, but it was not found at: "
                        + LOG_FILE.toAbsolutePath())
                .isTrue();


        // Read log content
        var lines = Files.readAllLines(LOG_FILE);

        // Assert exception message is present
        assertThat(lines)
                .as("Log file should contain the test exception message.")
                .anyMatch(line -> line.contains("Test uncaught exception"));
    }
}
