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

/**
 * Infrastructure for exporting transactions as 32-column tickets in several output formats.
 * <p>
 * Provides:
 * <ul>
 *   <li>A public controller-style API to export a Transaction to bytes asynchronously.</li>
 *   <li>Shared 32-column formatter.</li>
 *   <li>Renderers for HTML preview, PDF and ESC/POS printer ticket.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
package com.yupay.gangcomisiones.export;
