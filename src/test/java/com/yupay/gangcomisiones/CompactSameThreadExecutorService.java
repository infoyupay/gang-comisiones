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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/// A compact implementation of the [ExecutorService] that runs all tasks
/// on the caller's thread, without creating additional threads, meant to
/// be used on testing only.
/// This class is useful when concurrency is not required, and all tasks
/// need to execute sequentially in a thread-safe manner. It ignores
/// timeout features for certain methods, adhering to a minimalistic design.
///
/// ## Thread-safety:
///
/// - This class is thread-safe in the sense that shared state like
///   the shutdown status is safely published using volatile fields.
/// - However, tasks executed by this service are run synchronously
///   on the calling thread. Therefore, any thread-safety concerns
///   related to the tasks themselves remain the client's responsibility.
///
/// ## Features:
///
/// - Executes tasks immediately on the calling thread.
/// - Implements standard [ExecutorService] methods, including
///   `submit()`, `invokeAll()`, and `invokeAny()`.
/// - Minimal resource usage, as it does not create or manage threads.
/// - Ignores timeout configurations for methods that support them.
///
/// ## Limitations:
///
/// - Tasks are not executed concurrently as all tasks run on the caller's thread.
/// - Designed for simplicity and does not support advanced scheduling or
///   thread-pool features.
/// - Methods like `invokeAny()` and `invokeAll()` use a best-effort
///   approach to process tasks sequentially and do not enforce timeouts.
///
/// @author InfoYupay SACS
/// @version 1.0
public class CompactSameThreadExecutorService implements ExecutorService {
    private volatile boolean shutdown;

    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }

    @Override
    public <T> @NotNull Future<T> submit(@NotNull Callable<T> task) {
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public <T> @NotNull Future<T> submit(@NotNull Runnable task, T result) {
        return submit(() -> {
            task.run();
            return result;
        });
    }

    @Override
    public @NotNull Future<?> submit(@NotNull Runnable task) {
        return submit(task, null);
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        shutdown = true;
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) {
        return true;
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) {
        return tasks.stream().map(this::submit).collect(Collectors.toList());
    }

    @Override
    public <T> @NotNull List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks,
                                                  long timeout, @NotNull TimeUnit unit) {
        return invokeAll(tasks); // Ignores the timeout
    }

    @Override
    public <T> @NotNull T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws ExecutionException {
        for (Callable<T> task : tasks) {
            try {
                return task.call();
            } catch (Exception ignored) {
            }
        }
        throw new ExecutionException("All tasks failed", null);
    }

    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks,
                           long timeout, @NotNull TimeUnit unit) throws ExecutionException {
        return invokeAny(tasks); // Ignores the timeout
    }
}

