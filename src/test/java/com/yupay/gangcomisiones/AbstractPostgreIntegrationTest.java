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

import com.yupay.gangcomisiones.usecase.registry.ControllerRegistries;
import com.yupay.gangcomisiones.usecase.registry.DefaultViewRegistry;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for integration tests using local real PostgreSQL.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public abstract class AbstractPostgreIntegrationTest {

    protected static ViewRegistry viewRegistry;
    protected static AppContext ctx;
    protected static Logger LOGGER;
    protected static Path tmp;

    /**
     * Initializes the testing environment before all tests are executed.
     * <br/>
     * This method performs the following key operations:
     * <ul>
     *     <li>Sets up a temporary directory for testing purposes using the {@code @TempDir} annotation.</li>
     *     <li>Bootstraps the application's local file structure in test mode by invoking {@link TestBootstrap#bootstrap(Path)}.</li>
     *     <li>Initializes a default view registry instance using {@link DefaultViewRegistry}.</li>
     *     <li>Configures a logger for the integration testing process using {@link LoggerFactory#getLogger(Class)}.</li>
     *     <li>Initializes the application context using {@link AppContext#getInstance(Path, ViewRegistry)} with
     *         dummy JPA properties.</li>
     *     <li>Registers all default controller registries using {@link ControllerRegistries#registerAllDefaults()} and
     *         outputs a status message if already initialized.</li>
     * </ul>
     *
     * @param tempDir A temporary directory provided by JUnit for configuring the application's testing environment.
     *                <ul>
     *                    <li>Must not be {@code null}.</li>
     *                    <li>Automatically managed and cleaned up by JUnit after the test execution.</li>
     *                </ul>
     * @throws IOException If an I/O error occurs during the bootstrapping process.
     */
    @BeforeAll
    static void init(@TempDir Path tempDir) throws IOException {
        tmp = tempDir;
        TestBootstrap.bootstrap(tmp);
        viewRegistry = new DefaultViewRegistry();
        LOGGER = LoggerFactory.getLogger(AbstractPostgreIntegrationTest.class);
        ctx = AppContext.getInstance(DummyHelpers.getDummyJpaProperties(), viewRegistry);
        if (!ControllerRegistries.registerAllDefaults()) {
            System.out.println("Controller Registry already initialized. Using shared registry.");
        }
    }

    /**
     * Shutdown the application context.
     */
    @AfterAll
    static void close() {
        AppContext.shutdown();
    }

    /**
     * Expect a commit failure, asserts that an EntityTransaction
     * throws an exception on commit, then rollsback transaction
     * and asserts that thrown exception is IllegalState or Persistence.
     *
     * @param et the entity transaction to test.
     */
    protected static void expectCommitFailure(@NotNull EntityTransaction et) {
        var ex = assertThrows(RuntimeException.class, et::commit);
        if (et.isActive()) et.rollback();
        assertTrue(ex instanceof IllegalStateException
                        || ex instanceof PersistenceException,
                "Expected a persistence-related failure but got: " + ex);
    }
}
