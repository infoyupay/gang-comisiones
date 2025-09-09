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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A functional interface for consuming and processing lists of elements. The {@code ListPresenter} interface
 * ensures that implementing classes will provide a mechanism to display or process lists, while gracefully handling
 * potential {@code null} inputs by substituting them with an empty list.
 * <br/>
 * Extends the {@link Consumer} interface to facilitate functional usage by accepting a {@code List} of elements.
 * Implementing classes must define the {@link #showList(List)} method to specify how lists are displayed or processed.
 *
 * @param <T> the type of elements contained within the list
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ListPresenter<T> extends Consumer<List<T>> {

    /**
     * Consumes a list of elements and ensures it is displayed or processed, even if the input is null.
     * If the input list is {@code null}, an empty list is provided to the {@link  #showList(List)}} method instead.
     *
     * @param ts the list of elements to be processed, can be {@code null}
     */
    @Override
    default void accept(@Nullable List<T> ts) {
        showList(ts == null ? List.of() : ts);
    }

    /**
     * Displays the provided list to the user or processes it in a specific manner as defined
     * by the implementing class.
     *
     * @param list the non-null list of elements to be displayed or processed
     */
    void showList(@NotNull List<T> list);

    /**
     * Clears the current list of elements managed or processed by the implementing class.
     * This method should reset the state of the list to empty, removing all previously
     * added or processed elements.
     */
    void clearList();

    /**
     * Replaces an existing instance of the type {@code T} with the specified {@code item}.
     *
     * @param item the new instance of type {@code T} to be used as a replacement
     */
    void replace(T item);

    /**
     * Inserts a new instance of type {@code T} into the implementing class's managed structure.
     *
     * @param item the instance of type {@code T} to be inserted
     */
    void insert(T item);
}
