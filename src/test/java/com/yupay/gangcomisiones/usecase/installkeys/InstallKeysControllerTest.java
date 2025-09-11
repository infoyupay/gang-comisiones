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

package com.yupay.gangcomisiones.usecase.installkeys;

import com.yupay.gangcomisiones.*;
import com.yupay.gangcomisiones.exceptions.AppInstalationException;
import com.yupay.gangcomisiones.logging.LogConfig;
import com.yupay.gangcomisiones.services.ZipInstallerService;
import com.yupay.gangcomisiones.services.impl.ZipInstallerServiceLocalImpl;
import com.yupay.gangcomisiones.usecase.registry.DefaultViewRegistry;
import com.yupay.gangcomisiones.usecase.task.TaskMonitor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Integration-style unit tests for {@code InstallKeysController}, exercising the
 * end-to-end flow around ZIP selection, asynchronous unpacking, progress reporting,
 * and application context refresh.
 * <br/>
 * Scenarios covered:
 * <ol>
 *   <li>Successful installation when a valid JPA ZIP is provided.</li>
 *   <li>Graceful abort when the view returns a {@code null} path (user cancellation).</li>
 *   <li>Failure path when a corrupted ZIP is provided, ensuring error propagation.</li>
 * </ol>
 * <br/>
 * Environment handling:
 * <ul>
 *   <li>Temporarily overrides {@code user.home} to isolate filesystem side effects.</li>
 *   <li>Resets and prepares local directories used by the application.</li>
 *   <li>Initializes logging and validates JPA properties on success.</li>
 * </ul>
 * <br/>
 * <div style="border: 1px solid black; padding: 1px;">
 * <b>Execution note:</b> dvidal@infoyupay.com passed 3 tests in 2.199s at 2025-09-11 11:01 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class InstallKeysControllerTest {

    String originalHome;
    ZipInstallerService zipInstallerService;
    ExecutorService mockExecutor;

    /**
     * Prepares an isolated environment before each test:
     * <ol>
     *   <li>Overrides {@code user.home} to a temporary directory.</li>
     *   <li>Resets application-specific paths and logging directories.</li>
     *   <li>Instantiates a single-threaded executor and a local ZIP installer implementation.</li>
     *   <li>Ensures the project directory structure exists.</li>
     * </ol>
     *
     * @param target the temporary directory injected by JUnit for filesystem isolation
     * @throws IOException if directory creation fails during environment setup
     */
    @BeforeEach
    void takeoff(@TempDir @NotNull Path target) throws IOException {
        //Modify user home
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", target.toString());
        LocalFiles.YUPAY.reset();
        LocalFiles.PROJECT.reset();
        LocalFiles.LOGS.reset();
        LocalFiles.JPA_PROPERTIES.reset();

        //Setup services
        mockExecutor = spy(new CompactSameThreadExecutorService());
        zipInstallerService = new ZipInstallerServiceLocalImpl(mockExecutor);

        //Prepare the directories structure
        Files.createDirectories(LocalFiles.PROJECT.asPath());
    }

    /**
     * Restores the environment after each test:
     * <ul>
     *   <li>Reverts {@code user.home} to its original value.</li>
     *   <li>Resets cached local file references.</li>
     *   <li>Shuts down the executor and clears installer references.</li>
     *   <li>Shuts down the application context if it was initialized.</li>
     * </ul>
     */
    @AfterEach
    void landing() {
        //Restore user home
        System.setProperty("user.home", originalHome);
        LocalFiles.YUPAY.reset();
        LocalFiles.PROJECT.reset();
        LocalFiles.LOGS.reset();
        LocalFiles.JPA_PROPERTIES.reset();

        //Shutdown services
        zipInstallerService = null;
        mockExecutor.shutdownNow();

        //Shutdown context
        if (AppContext.isInitialized()) AppContext.shutdown();
    }

    /**
     * Verifies the happy path:
     * <ol>
     *   <li>The view returns a valid ZIP path.</li>
     *   <li>The controller executes the asynchronous installation.</li>
     *   <li>The task monitor shows and updates progress and then hides it.</li>
     *   <li>The application context is reloaded and JPA URL is validated.</li>
     *   <li>The final result is {@code SUCCESS}.</li>
     * </ol>
     */
    @Test
    void testSuccessfulInstallation() {
        /*=========*
         * ARRANGE *
         *=========*/
        var installKeyView = TestViews.installKeysView(DummyHelpers.getDummyJpaZip());
        var viewRegistry = new DefaultViewRegistry();
        viewRegistry.registerInstance(InstallKeysView.class, installKeyView);

        var taskMonitor = mock(TaskMonitor.class);
        doAnswer(invocation -> {
            System.out.println("Updated message: " + invocation.getArgument(0));
            return null;
        }).when(taskMonitor).updateMessage(anyString());
        doAnswer(invocation -> {
            System.out.printf("Updated progress %d of %d%n",
                    invocation.getArgument(0, Long.class),
                    invocation.getArgument(1, Long.class));
            return null;
        }).when(taskMonitor).updateProgress(anyLong(), anyLong());
        doAnswer(invocation -> {
            invocation.getArgument(0, Exception.class).printStackTrace();
            return null;
        }).when(taskMonitor).showError(any(Exception.class));

        /*=====*
         * ACT *
         *=====*/
        var useCaseController = new InstallKeysController(viewRegistry, taskMonitor, zipInstallerService);
        var result = useCaseController.run().join();

        /*========*
         * ASSERT *
         *========*/
        verify(mockExecutor).execute(any(Runnable.class));
        assertEquals(InstallKeysResult.SUCCESS, result);

        LogConfig.initLogging();
        AppContext.getInstance(LocalFiles.JPA_PROPERTIES.asPath(), new DefaultViewRegistry());

        assertEquals("jdbc:postgresql://localhost:5432/gang_comision_test?stringtype=unspecified&ApplicationName=gangcomision_test",
                AppContext.getInstance()
                        .getEntityManagerFactory()
                        .getProperties()
                        .get("jakarta.persistence.jdbc.url"));

        verify(installKeyView).showOpenDialogForZip();
        verify(taskMonitor).showProgress();
        verify(taskMonitor, atLeastOnce()).updateProgress(anyLong(), anyLong());
        verify(taskMonitor, atLeastOnce()).updateMessage(anyString());
        verify(taskMonitor).hideProgress();
        verify(taskMonitor, never()).showError(any());
    }

    /**
     * Verifies the controller returns {@code ABORT} when the user cancels the ZIP selection
     * (i.e., the view returns {@code null}) and that no monitor interactions occur.
     */
    @Test
    void testAbortedInstallationWhenViewReturnsNullPath() {
        var installKeyView = TestViews.installKeysView(null);
        var viewRegistry = new DefaultViewRegistry();
        viewRegistry.registerInstance(InstallKeysView.class, installKeyView);

        var taskMonitor = mock(TaskMonitor.class);

        var useCaseController = new InstallKeysController(viewRegistry, taskMonitor, zipInstallerService);
        var result = useCaseController.run().join();

        assertEquals(InstallKeysResult.ABORT, result);

        verify(installKeyView).showOpenDialogForZip();
        verifyNoInteractions(taskMonitor);
    }

    /**
     * Verifies the failure path using a corrupted ZIP:
     * <ol>
     *   <li>The asynchronous task runs on the executor.</li>
     *   <li>The final result is {@code FAILURE}.</li>
     *   <li>An {@code AppInstalationException} is surfaced to the monitor with the expected message.</li>
     * </ol>
     */
    @Test
    void testFailedInstallationDueCorruptedZip() {
        var installKeyView = TestViews.installKeysView(DummyHelpers.getCorruptZip());
        var viewRegistry = new DefaultViewRegistry();
        viewRegistry.registerInstance(InstallKeysView.class, installKeyView);

        var taskMonitor = mock(TaskMonitor.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Exception.class).printStackTrace();
            return null;
        }).when(taskMonitor).showError(any(Exception.class));

        var useCaseController = new InstallKeysController(viewRegistry, taskMonitor, zipInstallerService);
        var result = useCaseController.run().join();

        verify(mockExecutor).execute(any(Runnable.class));
        assertEquals(InstallKeysResult.FAILURE, result);

        verify(installKeyView).showOpenDialogForZip();

        // Capturamos la excepción y comprobamos su mensaje
        ArgumentCaptor<AppInstalationException> captor = ArgumentCaptor.forClass(AppInstalationException.class);
        verify(taskMonitor).showError(captor.capture());
        assertEquals("No files found in the zip.", captor.getValue().getMessage());
    }
}
