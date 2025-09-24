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

package com.yupay.gangcomisiones.usecase.commons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the result of executing a particular use case, which includes
 * a result type indicating the outcome and an optional associated value.
 *
 * @param result the result type indicating the outcome of a use case process, must not be {@code null}.
 * @param value  the value associated with the result, can be {@code null}
 * @param <T>    the type of the value associated with the result
 * @author InfoYupay SACS
 * @version 1.0
 */
public record Result<T>(@NotNull UseCaseResultType result,
                        @Nullable T value) {
    /**
     * Constructs a new {@code Result} instance with the specified result type
     * and null value.
     *
     * @param result the result type indicating the outcome of a use case process, must not be {@code null}
     */
    @Contract(pure = true)
    public Result(@NotNull UseCaseResultType result) {
        this(result, null);
    }

    /**
     * Creates a completed {@link CompletableFuture} containing a {@link Result}
     * with the specified result type and a null value.
     *
     * @param <T>    the type of the value associated with the result
     * @param result the result type indicating the outcome of a use case process
     * @return a completed {@link CompletableFuture} containing a {@link Result}
     * with the specified result type and a null value
     */
    public static <T> @NotNull CompletableFuture<Result<T>> completed(@NotNull UseCaseResultType result) {
        return completed(result, null);
    }

    /**
     * Creates a completed {@link CompletableFuture} containing a {@link Result}
     * with the specified result type and associated value.
     *
     * @param <T>    the type of the value associated with the result
     * @param result the result type indicating the outcome of a use case process
     * @param value  the value associated with the result, can be {@code null}
     * @return a completed {@link CompletableFuture} containing a {@link Result}
     * with the specified result type and value
     */
    public static <T> @NotNull CompletableFuture<Result<T>> completed(@NotNull UseCaseResultType result,
                                                                      @Nullable T value) {
        return CompletableFuture.completedFuture(new Result<>(result, value));
    }

    /**
     * Creates a new {@link Result} instance representing a successful outcome
     * of a use case process, with the specified value.
     * <br/>
     *
     * <ul>
     *    <li>If the operation successfully completes, this method wraps the provided value
     *        within a {@link Result} object, with the result type set to {@code OK}.</li>
     *    <li>The associated value may be {@code null}, depending on the use case scenario.</li>
     * </ul>
     *
     * @param <T> the type of the value associated with the result
     * @param value the value to be associated with the {@link Result}, can be {@code null}.
     *
     * @return a new {@link Result} instance with the result type {@code OK} and the given value.
     */
    @Contract("_ -> new")
    public static <T> @NotNull Result<T> ok(@Nullable T value) {
        return new Result<>(UseCaseResultType.OK, value);
    }

    /**
     * Creates a completed CompletableFuture with a result indicating an error outcome.
     *
     * @param <T> the type of the value associated with the result
     * @return a completed CompletableFuture containing a Result with a result type of ERROR
     */
    public static <T> @NotNull CompletableFuture<Result<T>> errorCompleted() {
        return completed(UseCaseResultType.ERROR);
    }

    /**
     * Creates a new {@link Result} instance representing an erroneous outcome.
     *
     * @param <T> the type of the value associated with the result
     * @return a new {@link Result} instance with a result type of {@code ERROR}
     */
    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull Result<T> error() {
        return new Result<>(UseCaseResultType.ERROR);
    }

    /**
     * Creates a new result instance representing a canceled outcome.
     *
     * @param <T> the type of the value associated with the result
     * @return a new {@link Result} instance with a result type of {@code CANCEL}
     */
    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull Result<T> cancel() {
        return new Result<>(UseCaseResultType.CANCEL);
    }

    /**
     * Creates a completed {@link CompletableFuture} with a result indicating a canceled outcome.
     *
     * @param <T> the type of the value associated with the result
     * @return a completed {@link CompletableFuture} containing a {@link Result} with a result type of {@code CANCEL}
     */
    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull CompletableFuture<Result<T>> cancelCompleted() {
        return completed(UseCaseResultType.CANCEL);
    }


    /**
     * Retrieves the result type of the use case process.
     *
     * @return the result type, represented as a {@link UseCaseResultType}, indicating
     * the outcome of the use case process (e.g., OK, ERROR, CANCEL)
     */
    @Override
    @NotNull
    public UseCaseResultType result() {
        return result;
    }

    /**
     * Returns the value associated with the result, if present.
     *
     * @return the value of type T if available, or null if no value is present
     */
    @Override
    @Nullable
    public T value() {
        return value;
    }

}
