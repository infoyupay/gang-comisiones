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
package com.yupay.gangcomisiones.export;

/**
 * Supported output destinations for the transaction ticket export.
 *
 * <ul>
 *   <li>PREVIEW_HTML: UTF-8 HTML document with a single &lt;pre&gt; showing the 32-column ticket.</li>
 *   <li>PDF: One-page PDF bytes using a monospaced base font (Courier).</li>
 *   <li>PRINTER_TICKET: ASCII bytes for ESC/POS printers, ending with the paper cut command.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum OutputType {
    /** HTML preview with a monospaced &lt;pre&gt; block. */
    PREVIEW_HTML,
    /** Single-page PDF (Courier). */
    PDF,
    /** ESC/POS ASCII ticket including cut command at the end. */
    PRINTER_TICKET
}
