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

import com.yupay.gangcomisiones.model.Transaction;
import com.yupay.gangcomisiones.usecase.commons.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper to transport transaction result and an export representation.
 *
 * @param result        the transaction result
 * @param exportPayload the export payload
 * @author InfoYupay SACS
 * @version 1.0
 */
public record ExportableTransaction(@NotNull Result<Transaction> result,
                                    @Nullable ExportPayload exportPayload) {
}
