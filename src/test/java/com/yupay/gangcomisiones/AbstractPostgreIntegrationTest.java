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

import com.yupay.gangcomisiones.logging.LogConfig;
import com.yupay.gangcomisiones.usecase.registry.DefaultViewRegistry;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Initialize the application context and logging configuration.
     */
    @BeforeAll
    static void init() {
        LogConfig.initLogging();
        viewRegistry = new DefaultViewRegistry();
        LOGGER = LoggerFactory.getLogger(AbstractPostgreIntegrationTest.class);
        ctx = AppContext.getInstance(DummyHelpers.getDummyJpaProperties(), viewRegistry);
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
