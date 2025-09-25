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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.LocalFiles;
import com.yupay.gangcomisiones.services.ZipInstallProgressListener;
import com.yupay.gangcomisiones.services.ZipInstallerService;
import com.yupay.gangcomisiones.usecase.registry.ViewRegistry;
import com.yupay.gangcomisiones.usecase.task.TaskMonitor;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinates the "Install Keys" use case by:
 * <ol>
 *   <li>Prompting the user to select a ZIP file containing the keys.</li>
 *   <li>Delegating the asynchronous unpacking to a ZIP installer service while tracking progress.</li>
 *   <li>Updating the task monitor with progress and status messages.</li>
 *   <li>Reloading the application context upon successful completion.</li>
 *   <li>Propagating a succinct outcome through a {@link CompletableFuture}.</li>
 * </ol>
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>UI orchestration via a minimal {@code InstallKeysView} abstraction.</li>
 *   <li>Progress reporting and error surfacing through {@code TaskMonitor}.</li>
 *   <li>Lifecycle refresh of the application context after installation.</li>
 * </ul>
 * <p>
 * Concurrency notes:
 * <ul>
 *   <li>The controller triggers an asynchronous install and returns immediately.</li>
 *   <li>It is not designed for concurrent re-use during an ongoing installation.</li>
 * </ul>
 * <p>
 * Result mapping:
 * <ul>
 *   <li>{@code SUCCESS}: ZIP processed without exception and post-actions completed.</li>
 *   <li>{@code ABORT}: User canceled file selection.</li>
 *   <li>{@code FAILURE}: Any exception during unpacking or subsequent stages.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class InstallKeysController implements ZipInstallProgressListener {

    private final InstallKeysView view;
    private final ViewRegistry viewRegistry;
    private final TaskMonitor monitor;
    private final ZipInstallerService installer;
    private int totalEntries;

    /**
     * Creates a new controller instance for the "Install Keys" flow.
     *
     * @param viewRegistry the registry used to resolve views
     * @param monitor      the task monitor used to show progress and report errors
     * @param installer    the service responsible for asynchronously unpacking the ZIP
     */
    @Contract(pure = true)
    public InstallKeysController(ViewRegistry viewRegistry,
                                 TaskMonitor monitor,
                                 ZipInstallerService installer) {
        this.viewRegistry = viewRegistry;
        this.monitor = monitor;
        this.installer = installer;
        this.view = viewRegistry.resolve(InstallKeysView.class);
    }

    @Override
    public void onStart(int totalEntries) {
        this.totalEntries = totalEntries;
        monitor.updateProgress(0, totalEntries);
    }

    @Override
    public void onEntryProcessed(String entryName, Path outputPath, int entriesProcessed) {
        monitor.updateMessage("Unpacking: " + entryName);
        monitor.updateProgress(entriesProcessed, totalEntries);
    }

    @Override
    public void onComplete() {
        monitor.hideProgress();
        reloadAppContext();
    }

    @Override
    public void onError(Exception error) {
        monitor.hideProgress();
        monitor.showError(error);
    }

    /**
     * Initiates the keys installation workflow by asking the user for a ZIP file and
     * kicking off an asynchronous unpack operation.
     * <ul>
     *   <li>If the user cancels selection, the returned future completes with {@code ABORT}.</li>
     *   <li>On successful unpack, the future completes with {@code SUCCESS}.</li>
     *   <li>On error during unpacking, the future completes with {@code FAILURE}.</li>
     * </ul>
     * <p>
     * This method never blocks; progress and messages are reported via the provided monitor.
     *
     * @return a future that completes with the final outcome of the installation
     */
    public CompletableFuture<InstallKeysResult> run() {
        Path zipPath = view.showOpenDialogForZip();
        if (zipPath == null) {
            return CompletableFuture.completedFuture(InstallKeysResult.ABORT);
        }

        monitor.showProgress();
        return installer.unpackZipAsync(zipPath, this)
                .thenApply(_ -> InstallKeysResult.SUCCESS)
                .exceptionally(_ -> InstallKeysResult.FAILURE);
    }

    /**
     * Refreshes the application context after a successful installation.
     * <ul>
     *   <li>If the context already exists, it is restarted using the resolved configuration.</li>
     *   <li>Otherwise, a new initialized instance is obtained.</li>
     * </ul>
     * Intended to be invoked once the ZIP processing has completed successfully.
     */
    private void reloadAppContext() {
        Path jpaProps = LocalFiles.jpaProperties();
        if (AppContext.isInitialized()) {
            AppContext.restart(jpaProps);
        } else {
            AppContext.getInstance(jpaProps, viewRegistry);
        }
    }
}

