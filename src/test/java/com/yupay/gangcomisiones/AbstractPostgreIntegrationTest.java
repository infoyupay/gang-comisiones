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

import com.yupay.gangcomisiones.assertions.CauseAssertions;
import com.yupay.gangcomisiones.usecase.registry.ControllerRegistries;
import com.yupay.gangcomisiones.usecase.registry.DefaultViewRegistry;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
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
     * Executes a specific set of persistence operations within a transaction, simulates a flush,
     * and verifies that an exception of the expected type occurs during the flush. Rolls back the
     * transaction following the operation while asserting against the content of the encountered exception.
     * <br/>
     * This method is specifically useful for testing scenarios where the database or entity model
     * causes an exception during a flush operation, allowing validation of both the exception type and message.
     *
     * @param <C>               The type of {@code Throwable} that is expected to be thrown during the flush operation.
     * @param expectedException The class of the exception expected to occur during the flush operation.
     *                          <ul>
     *                              <li>Must not be {@code null}.</li>
     *                              <li>Specifies the type or a superclass of the exception to be caught.</li>
     *                          </ul>
     * @param expectedMessage   The expected text that should be part of the message of the thrown exception.
     *                          <ul>
     *                              <li>Must not be {@code null}.</li>
     *                              <li>Used to ensure that the exception's message contains the specified text.</li>
     *                          </ul>
     * @param consumer          A lambda expression or method reference that provides the persistence operations
     *                          within the active transaction using the {@code EntityManager}.
     *                          <ul>
     *                              <li>Must not be {@code null}.</li>
     *                              <li>Includes operations like persisting, merging, or removing entities.</li>
     *                          </ul>
     * @return An assertion object for further verification of properties in the thrown exception.
     * <ul>
     *     <li>Allows validation that the exception message matches the expected content (ignoring case sensitivity).</li>
     *     <li>Enables chaining of additional assertions about the exception beyond the message.</li>
     * </ul>
     */
    @SuppressWarnings("UnusedReturnValue")
    public <C extends Throwable> AbstractStringAssert<?> performAndExpectFlushFailure(
            @NotNull Class<C> expectedException,
            @NotNull String expectedMessage,
            @NotNull Consumer<EntityManager> consumer) {
        return performAndExpectFlushFailure(expectedException, consumer)
                .message()
                .containsIgnoringCase(expectedMessage);
    }

    /**
     * Executes a specific set of persistence operations within a transaction, simulates a flush,
     * and ensures that the expected exception is thrown during the flush. Rolls back the transaction
     * following the operation and provides detailed assertions about the encountered exception.
     * <p>
     * <br/>
     * This method is useful for testing scenarios where an exception is expected to occur
     * during the flush operation of an {@code EntityManager}.
     *
     * @param <C>           The expected type of {@code Throwable} that should be thrown on flush.
     * @param expectedCause The class of the exception to expect during the flush operation.
     *                      <ul>
     *                          <li>Must not be {@code null}.</li>
     *                          <li>The thrown exception must be of this type or a subclass of it.</li>
     *                      </ul>
     * @param consumer      A lambda or method reference that performs the transaction logic
     *                      using the {@code EntityManager}. It is executed before an attempted
     *                      flush operation.
     *                      <ul>
     *                          <li>Must not be {@code null}.</li>
     *                          <li>Includes persistence operations like merge, persist, or remove if applicable.</li>
     *                      </ul>
     * @return An assertion object that enables further checks on the thrown exception.
     * <ul>
     *     <li>Provides tools to assert the type, message, or cause of the exception.</li>
     *     <li>Allows for chaining further exception-related assertions.</li>
     * </ul>
     */
    public <C extends Throwable> AbstractThrowableAssert<?, C> performAndExpectFlushFailure(
            @NotNull Class<C> expectedCause,
            @NotNull Consumer<EntityManager> consumer) {
        EntityTransaction et = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            consumer.accept(em);
            var ex = catchThrowable(em::flush);
            assertThat(ex)
                    .as("Expected exception of type " + expectedCause + " on flush, found null.")
                    .isNotNull();
            return CauseAssertions
                    .assertExpectedCause(expectedCause)
                    .assertCause(ex);
        } finally {
            if (et != null && et.isActive()) et.rollback();
        }
    }

    /**
     * Expect a commit failure, asserts that an EntityTransaction
     * throws an exception on commit, then rollsback transaction
     * and asserts that thrown exception is IllegalState or Persistence.
     *
     * @param et the entity transaction to test.
     */
    public void expectCommitFailure(@NotNull EntityTransaction et) {
        var ex = assertThrows(RuntimeException.class, et::commit);
        if (et.isActive()) et.rollback();
        assertTrue(ex instanceof IllegalStateException
                        || ex instanceof PersistenceException,
                "Expected a persistence-related failure but got: " + ex);
    }

    /**
     * Executes a given operation within a transactional context.
     * An EntityManager is provided to the operation, and the transaction
     * is committed if the operation completes successfully or rolled back
     * in case of a runtime exception.
     *
     * @param <T>       the type of the result produced by the operation performed within the transaction
     * @param ctx       the application context containing the EntityManagerFactory
     * @param performer a function that takes an EntityManager, performs an operation, and produces a result
     * @return the result of the operation performed within the transaction
     * @throws RuntimeException if an exception occurs during the transaction, after rolling back the transaction
     */
    public <T> T performInTransaction(@NotNull AppContext ctx,
                                      @NotNull Function<EntityManager, T> performer) {
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            var r = performer.apply(em);
            tx.commit();
            return r;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
