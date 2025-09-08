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
 * Represents the different modes in which controls in a form may be arranged,
 * depending upon intentionality.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum FormMode {
    /**
     * Represents the mode in which a form is set to facilitate creating new records.
     */
    CREATE,
    /**
     * Represents the mode in which a form is set for editing existing records.
     */
    EDIT,
    /**
     * Represents the mode in which a form is set for viewing existing data
     * without making any modifications. (Read-only mode).
     */
    VIEW
}
