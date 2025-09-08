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

package com.yupay.gangcomisiones.usecase;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Encapsulates the result of a single write operation performed by a use case.
 *
 * @param <T>    the type of the value containing the outcome details of the write operation
 * @param result an instance of {@link UseCaseResultType} that represents the outcome of the write operation
 * @param value  the associated value or data related to the result of the write operation
 * @author InfoYupay SACS
 * @version 1.0
 */
public record WriteSingleResult<T>(UseCaseResultType result, T value) {

    /**
     * Constructs a new {@code WriteSingleResult} with the specified result type and a null value.
     *
     * @param result an instance of {@link UseCaseResultType} representing the outcome of the write operation
     */
    public WriteSingleResult(UseCaseResultType result) {
        this(result, null);
    }

    /**
     * Creates a completed {@link CompletableFuture} that wraps a {@link WriteSingleResult} instance,
     * initialized with the specified {@link UseCaseResultType} and value.
     *
     * @param result the result type of the use case operation, represented as an instance of {@link UseCaseResultType}
     * @param value  the associated value or data related to the result of the use case operation, can be null
     * @param <T>    the type of the value associated with the write operation
     * @return a completed {@link CompletableFuture} containing a {@link WriteSingleResult} with the specified result
     * and value
     */
    public static <T> @NotNull CompletableFuture<WriteSingleResult<T>> completed(@NotNull UseCaseResultType result,
                                                                                 @Nullable T value) {
        return CompletableFuture.completedFuture(new WriteSingleResult<>(result, value));
    }

    /**
     * Creates a completed {@link CompletableFuture} wrapping a {@link WriteSingleResult} instance,
     * initialized with the specified {@link UseCaseResultType} and a null value.
     *
     * @param result the result type of the use case operation, represented as an instance of {@link UseCaseResultType}
     * @param <T>    the type of the associated value for the write operation, which is null in this case
     * @return a completed {@link CompletableFuture} containing a {@link WriteSingleResult} with the specified result
     */
    public static <T> @NotNull CompletableFuture<WriteSingleResult<T>> completed(@NotNull UseCaseResultType result) {
        return CompletableFuture.completedFuture(new WriteSingleResult<>(result, null));
    }

    /**
     * Creates a new instance of {@code WriteSingleResult} with an {@code ERROR} result type
     * and no associated value. Useful to return an error result in the pipeline of a future,
     * like with {@code exceptionally}.
     *
     * @param <T> the type of the associated value for the write operation
     * @return a new instance of {@code WriteSingleResult} initialized with an {@code ERROR} result
     */
    @Contract(" -> new")
    public static <T> @NotNull WriteSingleResult<T> error() {
        return new WriteSingleResult<>(UseCaseResultType.ERROR);
    }

    /**
     * Creates a completed {@link CompletableFuture} wrapping a {@link WriteSingleResult} with an error result type.
     * Useful to return a completable that already failed.
     *
     * @param <T> the type of the associated value for the write operation, which is null in this case
     * @return a completed {@link CompletableFuture} containing a {@link WriteSingleResult} with an {@code ERROR} result
     */
    public static <T> @NotNull CompletableFuture<WriteSingleResult<T>> errorCompleted() {
        return completed(UseCaseResultType.ERROR);
    }
}
