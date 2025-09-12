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

package com.yupay.gangcomisiones.board;

import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.commons.ListPresenter;
import com.yupay.gangcomisiones.usecase.commons.MessagePresenter;

/**
 * Represents a view component for displaying a board, combining the responsibilities of presenting lists
 * and showing messages or errors. This interface extends {@code ListPresenter} to provide mechanisms for
 * displaying and processing lists of elements, and {@code MessagePresenter} for presenting messages or errors.
 *
 * @param <T> the type of elements that can be displayed on the board
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface BoardView<T> extends ListPresenter<T>, MessagePresenter {
    /**
     * Propagates the privileges of the specified user to associated components or elements.
     * This tells the board view to disable/enable components based on the user's privileges.
     *
     * @param user the user whose privileges are to be propagated
     */
    void propagatePrivileges(User user);

}
