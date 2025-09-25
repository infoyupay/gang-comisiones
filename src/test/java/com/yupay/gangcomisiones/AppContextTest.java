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
import com.yupay.gangcomisiones.usecase.registry.DefaultViewRegistry;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

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
    private static final Path DUMMY_PATH = DummyHelpers.getDummyJpaProperties();

    /**
     * Initializes the logging environment for the test context. <br/>
     * This method sets up the necessary file configurations and logging setup required for running test cases.
     * It ensures that all test-specific paths and logging configurations are initialized correctly.
     * <br/><br/>
     * The initialization involves:
     * <ol>
     *     <li>Configuring application-specific paths using {@link LocalFiles#init(AppMode, Path)}
     *     with <strong>TEST</strong> mode as context.</li>
     *     <li>Initializing the logging system using {@link LogConfig#initLogging()}.</li>
     * </ol>
     *
     * @param tempDir The temporary directory path provided by the test framework where
     *                test-specific files and logs will be configured.
     *                <ul>
     *                    <li>Must not be {@code null}.</li>
     *                    <li>Generated dynamically for the duration of the test lifecycle.</li>
     *                </ul>
     */
    @BeforeAll
    static void initializeLogging(@TempDir Path tempDir) {
        LocalFiles.init(AppMode.TEST, tempDir);
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
     * Tests the initialization of the {@link AppContext} instance.
     * <br/><br/>
     * This method verifies that the {@link AppContext#getInstance(Path, ViewRegistry)} method:
     * <ul>
     *     <li>Succeeds in creating a non-null instance of {@link AppContext} when valid parameters are provided.</li>
     *     <li>
     *         Correctly initializes essential components of the {@link AppContext}, including:
     *         <ul>
     *              <li>{@link AppContext#getEntityManagerFactory()}</li>
     *              <li>{@link AppContext#getJdbcExecutor()}</li>
     *              <li>{@link AppContext#getTaskExecutor()}</li>
     *         </ul>
     *     </li>
     * </ul>
     * <br/>
     * Assertions ensure that:
     * <ol>
     *     <li>An instance of {@link AppContext} is not null after initialization.</li>
     *     <li>All core components of the context are initialized and functional, verified by non-null checks.</li>
     * </ol>
     */
    @Test
    void testGetInstanceInitializes() {
        var ctx = AppContext.getInstance(DUMMY_PATH, new DefaultViewRegistry());
        assertNotNull(ctx);
        assertNotNull(ctx.getEntityManagerFactory());
        assertNotNull(ctx.getJdbcExecutor());
        assertNotNull(ctx.getTaskExecutor());
    }

    /**
     * Tests that {@link AppContext#getInstance()} throws an exception
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
     * Tests that {@link AppContext#restart(Path)} replaces the instance.
     */
    @Test
    void testRestartReplacesInstance() {
        var first = AppContext.getInstance(DUMMY_PATH, new DefaultViewRegistry());
        var second = AppContext.restart(DUMMY_PATH);
        assertNotSame(first, second);
        // Old instance was shut down.
        assertTrue(first.getJdbcExecutor().isShutdown());
        assertTrue(first.getTaskExecutor().isShutdown());
    }

    /**
     * Tests that {@link AppContext#getInstance()} returns the same instance
     * when called concurrently.
     *
     * @throws InterruptedException if test threads interrupted.
     */
    @Test
    void testSingletonConcurrent() throws InterruptedException {
        final var results = new AppContext[10];
        var threads = new Thread[10];

        for (var i = 0; i < 10; i++) {
            var idx = i;
            threads[i] = new Thread(() -> results[idx] = AppContext.getInstance(DUMMY_PATH, new DefaultViewRegistry()));
            threads[i].start();
        }
        for (var t : threads) t.join();

        for (var i = 1; i < results.length; i++) {
            assertSame(results[0], results[i]);
        }
    }

    /**
     * Tests that AppContext.shutdown() shuts down the executors.
     */
    @Test
    void testShutdownExecutors() {
        var ctx = AppContext.getInstance(DUMMY_PATH, new DefaultViewRegistry());
        var jdbc = ctx.getJdbcExecutor();
        var task = ctx.getTaskExecutor();

        AppContext.shutdown();

        assertTrue(jdbc.isShutdown());
        assertTrue(task.isShutdown());
    }
}
