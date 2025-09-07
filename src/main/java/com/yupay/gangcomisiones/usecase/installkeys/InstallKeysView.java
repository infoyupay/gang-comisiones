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
package com.yupay.gangcomisiones.usecase.installkeys;

import java.nio.file.Path;

/**
 * Abstraction of the InstallKeys user interface.
 * Provides only the operations the controller needs.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface InstallKeysView {

    /**
     * Opens a file chooser for selecting a ZIP archive.
     *
     * @return the selected path, or {@code null} if the user cancels.
     */
    Path showOpenDialogForZip();

}

