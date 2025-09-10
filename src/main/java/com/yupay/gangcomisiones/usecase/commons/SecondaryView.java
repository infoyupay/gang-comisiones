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

/**
 * Represents a secondary view in a user interface with capabilities to display
 * and close the view. This interface defines the basic operations that can be
 * performed on a secondary view component.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface SecondaryView {
    /**
     * Displays the secondary view in the user interface. This method is used
     * to make the associated view visible to the user and is typically invoked
     * when opening or activating the view.
     */
    void showView();

    /**
     * Closes the secondary view and performs any necessary cleanup operations.
     * Typically invoked when the view is no longer needed or should be removed
     * from the user interface.
     */
    void closeView();
}
