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
package com.yupay.gangcomisiones.usecase.task;

/**
 * Monitors the execution of long-running tasks by providing progress,
 * status messages, and error reporting hooks.
 * <br/>
 * Implementations may update a user interface, log progress, or delegate
 * notifications elsewhere.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface TaskMonitor {

    /**
     * Shows or initializes the progress indicator for a task.
     */
    void showProgress();

    /**
     * Hides or disposes of the progress indicator.
     */
    void hideProgress();

    /**
     * Updates the task's progress value.
     *
     * @param workDone  the number of units of work already processed.
     * @param totalWork the total number of units of work to be processed.
     */
    void updateProgress(long workDone, long totalWork);

    /**
     * Updates the task's user-facing status message.
     *
     * @param message the message to display to the user.
     */
    void updateMessage(String message);

    /**
     * Reports an error that occurred during task execution.
     * <br/>
     * Implementations should not be responsible for logging. Services or
     * controllers invoking this monitor should handle logging and provide
     * a meaningful exception here for user feedback.
     *
     * @param ex the exception that caused the failure.
     */
    void showError(Throwable ex);
}
