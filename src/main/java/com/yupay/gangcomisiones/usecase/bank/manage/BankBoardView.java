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

package com.yupay.gangcomisiones.usecase.bank.manage;

import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.BoardView;
import com.yupay.gangcomisiones.usecase.commons.ListPresenter;
import com.yupay.gangcomisiones.usecase.commons.MessagePresenter;
import com.yupay.gangcomisiones.usecase.commons.SecondaryView;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a specific type of {@link BoardView} for managing and displaying
 * {@link Bank} entities. This view component incorporates functionalities for rendering
 * lists of {@link Bank} entities and presenting messages or errors, as defined in the
 * extended interfaces {@link ListPresenter} and {@link MessagePresenter}.
 * <br/>
 * By extending {@link BoardView<Bank>}, this interface specializes the generic board
 * functionalities to work exclusively with {@link Bank} objects, facilitating their
 * visualization and interaction in the context of the application's user interface.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface BankBoardView extends BoardView<Bank>, SecondaryView {
    /**
     * Retrieves the {@link BankView} instance associated with this {@link BankBoardView}.
     * The returned {@link BankView} is used to interact with and display the details
     * of a specific {@link Bank} entity within the context of the application's user interface.
     *
     * @return a non-null instance of {@link BankView} to manage and present {@link Bank} details
     */
    @NotNull BankView getBankView();

    /**
     * How BankBoard privileges must be propagated according to level:
     * <ul>
     *     <li>At least {@link UserRole#ADMIN}: User can add, view and edit banks</li>
     *     <li>Only {@link UserRole#CASHIER}: User can view but not create or edit banks</li>
     * </ul>
     *
     * @param user the user whose privileges are to be propagated
     */
    @Override
    void propagatePrivileges(User user);
}
