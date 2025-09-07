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
import com.yupay.gangcomisiones.services.*;
import com.yupay.gangcomisiones.services.impl.*;
import com.yupay.gangcomisiones.services.ZipInstallerService;
import com.yupay.gangcomisiones.services.impl.ZipInstallerServiceLocalImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the application context, including the lifecycle of shared resources such as
 * {@link EntityManagerFactory}, and thread pools for JDBC operations and general tasks.
 * Provides a singleton instance for managing application-level context and facilitates
 * graceful resource cleanup during shutdown.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class AppContext {
    /**
     * Logging facility.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AppContext.class);
    /**
     * Single thread-safe instance.
     */
    private static final AtomicReference<AppContext> INSTANCE = new AtomicReference<>();
    /**
     * Flag indicating whether the shutdown hook responsible for
     * gracefully shutting down the application context has been registered.
     *
     * @implNote This flag will be migrated to StableValue (JEP 502) once
     * it becomes production-ready in a future JDK release.
     */
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);
    /**
     * Timeout to await for executor services termination on shutdown.
     */
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SEC = 5;
    /**
     * A shutdown hook, memory leak-proof.
     */
    private static final Thread SHUTDOWN_HOOK = new Thread(AppContext::shutdown, "AppContext-ShutdownHook");
    /**
     * The shared entity manager factory.
     */
    private final EntityManagerFactory emf;
    /**
     * Executor for JDBC (JPA) operations.
     */
    private final ExecutorService jdbcExecutor;
    /**
     * Executor for other tasks.
     */
    private final ExecutorService taskExecutor;
    /**
     * User session container.
     */
    private final UserSession userSession;
    /**
     * User service backed by JPA.
     */
    private final UserService userService;
    /**
     * Bank service backed by JPA.
     */
    private final BankService bankService;
    /**
     * Concept service backed by JPA.
     */
    private final ConceptService conceptService;
    /**
     * Transaction service backed by JPA.
     */
    private final TransactionService transactionService;
    /**
     * ReversalRequest service backed by JPA.
     */
    private final ReversalRequestService reversalRequestService;
    /**
     * GlobalConfig service backed by JPA.
     */
    private final GlobalConfigService globalConfigService;
    /**
     * GlobalConfig cache.
     */
    private final GlobalConfigCache globalConfigCache;
    /**
     * Global installation service backed by java.nio.Path.
     */
    private final ZipInstallerService zipInstallerService;

    /**
     * Constructs an instance of {@code AppContext} with the provided JPA properties.
     * Initializes the {@code EntityManagerFactory}, a single-threaded executor for JDBC operations,
     * and a virtual-thread-based executor for task execution.
     *
     * @param jpaProperties the path to the JPA properties file used for configuring
     *                      the {@code EntityManagerFactory}.
     */
    private AppContext(Path jpaProperties) {
        this.emf = buildEntityManagerFactory(jpaProperties);
        this.jdbcExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AppContext-JDBC");
            t.setDaemon(true);
            return t;
        });
        this.taskExecutor = Executors.newThreadPerTaskExecutor(Thread
                .ofVirtual()
                .name("AppContext-Task-", 0)
                .factory());
        //User session container.
        this.userSession = new UserSession();
        // Persistence services.
        this.userService = new UserServiceImpl(emf, jdbcExecutor);
        this.bankService = new BankServiceImpl(emf, jdbcExecutor);
        this.conceptService = new ConceptServiceImpl(emf, jdbcExecutor);
        this.transactionService = new TransactionServiceImpl(emf, jdbcExecutor);
        this.reversalRequestService = new ReversalRequestServiceImpl(emf, jdbcExecutor);
        this.globalConfigService = new GlobalConfigServiceImpl(emf, jdbcExecutor);
        this.globalConfigCache = new GlobalConfigCache(this);
        this.zipInstallerService = new ZipInstallerServiceLocalImpl(taskExecutor);
    }

    /**
     * Obtains the unique instance, lazily creating it if necessary.
     * If multiple threads race, only one succeeds; others see the winner.
     *
     * @param jpaProperties the path of .properties file.
     * @return the instance, initializes if needed.
     */
    public static AppContext getInstance(Path jpaProperties) {
        AppContext ctx = INSTANCE.get();
        if (ctx == null) {
            ctx = new AppContext(jpaProperties);
            if (!INSTANCE.compareAndSet(null, ctx)) {
                // Lost the race, discard the one we just created
                ctx = INSTANCE.get();
            } else {
                //Just if the shutdown hook has not been registered yet
                if (shutdownHookRegistered
                        .compareAndSet(false, true)) { // ✅ atomic
                    try {
                        //Try to register.
                        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
                    } catch (IllegalStateException _) {
                        // JVM is already shutting down, ignore.
                    }
                }
            }
        }
        return ctx;
    }

    /**
     * Retrieves the unique instance of {@code AppContext}.
     * This method ensures that the global context has been properly initialized
     * before returning it. If the context has not been initialized, an
     * {@code AppContextException} will be thrown.
     *
     * @return the singleton instance of {@code AppContext}.
     * @throws AppContextException if the context has not been initialized prior to this call.
     */
    public static @NotNull AppContext getInstance() {
        AppContext ctx = INSTANCE.get();
        if (ctx == null) {
            throw new AppContextException("AppContext has not been initialized. " +
                    "Call getInstance(Path jpaProperties) first.");
        }
        return ctx;
    }


    /**
     * Restarts the context with a new JPA properties file.
     * This is safe to call at runtime; it will replace the global instance.
     *
     * @param jpaProperties the path to .properties file.
     * @return the new instance (restarted).
     */
    public static synchronized @NotNull AppContext restart(Path jpaProperties) {
        AppContext old = INSTANCE.getAndSet(null);
        if (old == null) {
            throw new AppContextException("Cannot restart AppContext because it was not initialized.");
        }
        AppContext.shutdown(old);
        AppContext fresh = new AppContext(jpaProperties);
        INSTANCE.set(fresh);
        return fresh;
    }

    /**
     * Gracefully shuts down the given context, closing and freeing all
     * asociated resources.
     *
     * @param ctx the app context to shutdown.
     */
    private static void shutdown(AppContext ctx) {
        if (ctx == null) return;

        if (ctx.emf != null) {
            try {
                if (ctx.emf.isOpen()) {
                    ctx.emf.close();
                }
            } catch (Exception e) {
                LOG.error("Error while closing EntityManagerFactory", e);
            }
        }

        shutdownExecutor(ctx.jdbcExecutor, "JDBC");
        shutdownExecutor(ctx.taskExecutor, "Task");

        LOG.info("AppContext shutdown completed.");
    }

    /**
     * Gracefully shuts down the provided {@code ExecutorService} after attempting
     * to terminate it within a predefined timeout. If the executor cannot be
     * terminated within the timeout, it is forcibly shut down.
     * <br/>
     * This method also logs warnings or errors if the shutdown process
     * encounters any interruptions or unexpected issues.
     *
     * @param executor the {@code ExecutorService} to shut down. If {@code null}, this method does nothing.
     * @param name     a string representing the name of the executor, used for logging purposes.
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    LOG.warn("{} executor did not terminate in time, forcing shutdownNow()", name);
                    executor.shutdownNow();
                }
            } catch (InterruptedException _) {
                LOG.warn("{} executor interrupted during shutdown, forcing shutdownNow()", name);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("Unexpected error shutting down {} executor", name, e);
            }
        }
    }

    /**
     * Builds and initializes an {@code EntityManagerFactory} using the specified JPA properties file.
     * This factory is responsible for creating {@code EntityManager} instances that interact with
     * the persistence context.
     *
     * @param jpaProperties the path to the JPA properties file. This file should contain the necessary
     *                      configuration properties for the persistence unit.
     * @return a configured {@code EntityManagerFactory} instance ready for use.
     * @throws AppContextException if the JPA properties file cannot be read or loaded properly.
     */
    private static EntityManagerFactory buildEntityManagerFactory(Path jpaProperties) {
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(jpaProperties)) {
            props.load(reader);
        } catch (IOException e) {
            throw new AppContextException("Unable to load JPA properties from: " + jpaProperties, e);
        }
        return Persistence.createEntityManagerFactory("GangComisionesPU", props);
    }

    /**
     * Gracefully shuts down the global context (if any).
     * Called from Application.stop() or the JVM shutdown hook.
     */
    public static synchronized void shutdown() {
        AppContext ctx = INSTANCE.getAndSet(null);
        shutdown(ctx);
    }

    /**
     * Retrieves the {@code EntityManagerFactory} instance associated with this context.
     * This factory is responsible for creating {@code EntityManager} instances, which handle
     * persistence operations within the application.
     *
     * @return the {@code EntityManagerFactory} instance managed by this context.
     */
    @Contract(pure = true)
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    /**
     * Retrieves the {@code ExecutorService} specifically used for JDBC operations.
     * This executor is typically single-threaded to ensure sequential database access
     * and prevent concurrency issues in database transactions.
     *
     * @return the {@code ExecutorService} dedicated to handling JDBC operations.
     */
    @Contract(pure = true)
    public ExecutorService getJdbcExecutor() {
        return jdbcExecutor;
    }

    /**
     * Retrieves the {@code ExecutorService} dedicated to managing task execution
     * within the application. This ExecutorService is configured to handle
     * general-purpose asynchronous tasks.
     *
     * @return the {@code ExecutorService} used for task execution.
     * @implNote DONT USE IT FOR DATABASE CONNECTIONS.
     */
    @Contract(pure = true)
    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    /**
     * Retrieves the {@code UserService} instance associated with this context.
     * The {@code UserService} provides various operations related to {@code User} entities,
     * such as user creation, password management, and retrieval based on specific criteria.
     *
     * @return the {@code UserService} instance managed by this application context.
     */
    @Contract(pure = true)
    public UserService getUserService() {
        return userService;
    }

    /**
     * Retrieves the {@code BankService} instance associated with this context.
     * The {@code BankService} provides operations related to {@code Bank} entities.
     *
     * @return the {@code BankService} instance managed by this application context.
     */
    @Contract(pure = true)
    public BankService getBankService() {
        return bankService;
    }

    /**
     * Retrieves the {@code ConceptService} instance associated with this context.
     * The {@code ConceptService} provides operations related to {@code Concept} entities.
     *
     * @return the {@code ConceptService} instance managed by this application context.
     */
    @Contract(pure = true)
    public ConceptService getConceptService() {
        return conceptService;
    }

    /**
     * Retrieves the {@code TransactionService} instance associated with this context.
     * The {@code TransactionService} provides operations related to {@code Transaction} entities.
     *
     * @return the {@code TransactionService} instance managed by this application context.
     */
    @Contract(pure = true)
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /**
     * Retrieves the {@code ReversalRequestService} instance associated with this context.
     * The service provides operations related to {@code ReversalRequest} entities.
     *
     * @return the {@code ReversalRequestService} instance managed by this application context.
     */
    @Contract(pure = true)
    public ReversalRequestService getReversalRequestService() {
        return reversalRequestService;
    }

    /**
     * Retrieves the {@code GlobalConfigService} instance associated with this context.
     * The service provides operations related to {@code GlobalConfig} entities.
     *
     * @return the {@code GlobalConfigService} instance managed by this application context.
     */
    @Contract(pure = true)
    public GlobalConfigService getGlobalConfigService() {
        return globalConfigService;
    }

    /**
     * Retrieves the {@code UserSession} instance associated with the current application context.
     * The {@code UserSession} manages the currently logged-in user and allows listeners to track
     * changes to the logged-in user's state.
     *
     * @return the {@code UserSession} instance managed by this context.
     */
    @Contract(pure = true)
    public UserSession getUserSession() {
        return userSession;
    }

    /**
     * Retrieves the {@code InstallationService} instance associated with this context.
     * The {@code InstallationService} provides operations related to installation tasks,
     * such as unpacking installation files, verifying persistence, and managing I/O executions.
     *
     * @return the {@code InstallationService} instance managed by this application context.
     */
    @Contract(pure = true)
    public ZipInstallerService getInstallationService() {
        return zipInstallerService;
    }

    /**
     * Retrieves the {@code GlobalConfigCache} instance associated with the current application context.
     * The {@code GlobalConfigCache} provides a cache for {@code GlobalConfig} entities.
     *
     * @return value of property.
     */
    @Contract(pure = true)
    public GlobalConfigCache getGlobalConfigCache() {
        return globalConfigCache;
    }

}


