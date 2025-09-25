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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test unit for uncaught exception logger.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UncaughtExceptionLoggerTest {

    private static final Path LOG_DIR = LocalFiles.logs();
    private static final Path LOG_FILE = LOG_DIR.resolve("gang-comisiones.log");

    /**
     * Sets up the test, creating LOG_DIR directories and
     * cleaning log file. Then, will init logging sub-system.
     *
     * @throws IOException if cannot perform files writing.
     */
    @BeforeAll
    static void setup() throws IOException {
        // Ensure log directory exists
        Files.createDirectories(LOG_DIR);

        // Clean old log file
        Files.deleteIfExists(LOG_FILE);

        LogConfig.initLogging();
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
        Thread thread = new Thread(() -> {
            throw new RuntimeException("Test uncaught exception");
        });

        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger());
        thread.start();
        thread.join();

        boolean exists = false;
        int retries = 5;
        for (int i = 0; i < retries; i++) {
            if (Files.exists(LOG_FILE) && Files.size(LOG_FILE) > 0) {
                exists = true;
                break;
            }
            Thread.sleep(200);
        }

        if (!exists) {
            fail("Log file should exist after uncaught exception, but it was not found at: " + LOG_FILE.toAbsolutePath());
        }


        // Read log content
        List<String> lines = Files.readAllLines(LOG_FILE);

        // Assert exception message is present
        boolean containsMessage = lines.stream()
                .anyMatch(line -> line.contains("Test uncaught exception"));
        assertTrue(containsMessage, "Log file should contain the exception message");
    }
}
