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

import java.util.function.Consumer;

/**
 * The {@code SuccessProcessor} is a record designed to handle operation results
 * by modifying a {@link ListPresenter} instance based on specific endpoints. It acts as a
 * {@link Consumer} for {@link Result} objects and processes successful outcomes.
 *
 * @param <T>       the type of elements that the {@code ListPresenter} and {@code Result} operate upon
 * @param presenter the {@link ListPresenter} used to perform operations such as insert or replace
 * @param endpoint  the {@link PresenterEndpoint} that specifies the operation type to be executed
 * @author InfoYupay SACS
 * @version 1.0
 */
public record SuccessProcessor<T>(ListPresenter<T> presenter,
                                  @NotNull PresenterEndpoint endpoint) implements Consumer<Result<T>> {
    /**
     * Creates and returns a {@code SuccessProcessor} configured to perform an insert operation
     * on the specified {@code ListPresenter}.
     *
     * @param <T>       the type of elements managed by the {@code ListPresenter}
     * @param presenter the {@code ListPresenter} instance on which the insert operation will be executed
     * @return a new {@code SuccessProcessor} configured for the insert operation
     */
    @Contract("_ -> new")
    public static <T> @NotNull SuccessProcessor<T> insert(ListPresenter<T> presenter) {
        return new SuccessProcessor<>(presenter, PresenterEndpoint.INSERT);
    }

    /**
     * Creates and returns a {@code SuccessProcessor} configured to perform a replace operation
     * on the specified {@code ListPresenter}.
     *
     * @param <T>       the type of elements managed by the {@code ListPresenter}
     * @param presenter the {@code ListPresenter} instance on which the replace operation will be executed
     * @return a new {@code SuccessProcessor} configured for the replace operation
     */
    @Contract("_ -> new")
    public static <T> @NotNull SuccessProcessor<T> replace(ListPresenter<T> presenter) {
        return new SuccessProcessor<>(presenter, PresenterEndpoint.REPLACE);
    }

    @Override
    public void accept(Result<T> result) {
        if (result != null
                && result.result() == UseCaseResultType.OK
                && result.value() != null) {
            endpoint.apply(presenter, result.value());
        }
    }

    /**
     * Defines the endpoints for modifying a presenter with specific operations
     * to either add new elements or replace existing elements.
     */
    enum PresenterEndpoint {
        /**
         * Represents an operation to add new elements within the context of a presenter.
         */
        INSERT {
            @Override
            <T> void apply(ListPresenter<T> presenter, T value) {
                presenter.insert(value);
            }
        },
        /**
         * Represents an operation to replace existing elements within the context of a presenter.
         */
        REPLACE {
            @Override
            <T> void apply(ListPresenter<T> presenter, T value) {
                presenter.replace(value);
            }
        };

        /**
         * Applies a specific operation to modify a {@link ListPresenter} using the given value of type {@code T}.
         *
         * @param <T>       the type of elements managed by the {@code ListPresenter}
         * @param presenter the {@code ListPresenter} to which the operation should be applied
         * @param value     the value of type {@code T} to be used in the operation
         */
        abstract <T> void apply(ListPresenter<T> presenter, T value);
    }
}
