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

import com.yupay.gangcomisiones.exceptions.AppContextException;
import com.yupay.gangcomisiones.logging.LogConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the AppContext class.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class AppContextTest {
    /**
     * Path to Dummy-jpa
     */
    private static final Path DUMMY_PATH;

    //Creates the dummy path to point to dummy-jpa.properties
    static {
        try {
            DUMMY_PATH = Path.of(Objects.requireNonNull(
                            AppContextTest.class.getResource("dummy-jpa.properties"))
                    .toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes logging for the test class.
     */
    @BeforeAll
    static void initializeLogging() {
        LogConfig.initLogging();
    }

    /**
     * Cleans up the test environment.
     */
    @BeforeEach
    void cleanup() {
        // Clean previous instances.
        try {
            AppContext.restart(DUMMY_PATH);
        } catch (AppContextException _) {
            // ignore, there wasn't an instance.
        }
    }

    /**
     * Tests that AppContext.getInstance(Path) initializes the context.
     */
    @Test
    void testGetInstanceInitializes() {
        AppContext ctx = AppContext.getInstance(DUMMY_PATH);
        assertNotNull(ctx);
        assertNotNull(ctx.getEntityManagerFactory());
        assertNotNull(ctx.getJdbcExecutor());
        assertNotNull(ctx.getTaskExecutor());
    }

    /**
     * Tests that AppContext.getInstance() throws an exception
     * when the context has not been initialized.
     */
    @Test
    void testGetInstanceWithoutInitThrows() {
        // Restart to nullify and simulate not initialized.
        AppContext.restart(DUMMY_PATH); // initialize
        AppContext.shutdown();          // close
        Exception ex = assertThrows(AppContextException.class, AppContext::getInstance);
        assertTrue(ex.getMessage().contains("not been initialized"));
    }

    /**
     * Tests that AppContext.restart(Path) replaces the instance.
     */
    @Test
    void testRestartReplacesInstance() {
        AppContext first = AppContext.getInstance(DUMMY_PATH);
        AppContext second = AppContext.restart(DUMMY_PATH);
        assertNotSame(first, second);
        // Old instance was shut down.
        assertTrue(first.getJdbcExecutor().isShutdown());
        assertTrue(first.getTaskExecutor().isShutdown());
    }

    /**
     * Tests that AppContext.getInstance() returns the same instance
     * when called concurrently.
     *
     * @throws InterruptedException if test threads interrupted.
     */
    @Test
    void testSingletonConcurrent() throws InterruptedException {
        final AppContext[] results = new AppContext[10];
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            int idx = i;
            threads[i] = new Thread(() -> results[idx] = AppContext.getInstance(DUMMY_PATH));
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        for (int i = 1; i < results.length; i++) {
            assertSame(results[0], results[i]);
        }
    }

    /**
     * Tests that AppContext.shutdown() shuts down the executors.
     */
    @Test
    void testShutdownExecutors() {
        AppContext ctx = AppContext.getInstance(DUMMY_PATH);
        ExecutorService jdbc = ctx.getJdbcExecutor();
        ExecutorService task = ctx.getTaskExecutor();

        AppContext.shutdown();

        assertTrue(jdbc.isShutdown());
        assertTrue(task.isShutdown());
    }
}
