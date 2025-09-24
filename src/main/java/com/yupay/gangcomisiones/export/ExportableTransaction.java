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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

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
    /**
     * Creates a new {@link ExportableTransaction} instance that represents an erroneous outcome.
     * <br/>
     * This method creates an exportable transaction with:
     * <ol>
     *   <li>A {@link Result} instance indicating an error.</li>
     *   <li>A {@code null} export payload.</li>
     * </ol>
     * <br/>
     * This is typically used to signal a failure in the context of transaction export processes.
     *
     * @return a new {@link ExportableTransaction} instance representing an error with no export payload.
     */
    @Contract(" -> new")
    public static @NotNull ExportableTransaction error() {
        return new ExportableTransaction(Result.error(), null);
    }

    /**
     * Asynchronously creates a completed {@link CompletableFuture} containing a {@link ExportableTransaction}
     * instance that represents an erroneous outcome.
     * <br/>
     * This method is useful for signaling a failure in export processes without initiating asynchronous computations.
     * <br/>
     * The created {@link ExportableTransaction} encapsulates:
     * <ul>
     *   <li>A {@link Result} instance indicating an error.</li>
     *   <li>A {@code null} export payload.</li>
     * </ul>
     *
     * @return a {@link CompletableFuture} that is already completed with a {@link ExportableTransaction}
     * representing an error with no export payload.
     */
    public static @NotNull CompletableFuture<ExportableTransaction> errorCompleted() {
        return CompletableFuture.completedFuture(error());
    }

    /**
     * Asynchronously creates a completed {@link CompletableFuture} containing a {@link ExportableTransaction}
     * instance that represents a cancellation outcome.
     * <br/>
     * This method is useful for signaling a cancellation in export processes without initiating asynchronous computations.
     * <br/>
     * The created {@link ExportableTransaction} encapsulates:
     * <ul>
     *   <li>A {@link Result} instance indicating a cancellation.</li>
     *   <li>A {@code null} export payload.</li>
     * </ul>
     *
     * @return a {@link CompletableFuture} that is already completed with a {@link ExportableTransaction}
     * representing a cancellation with no export payload.
     */
    public static @NotNull CompletableFuture<ExportableTransaction> cancelCompleted() {
        return CompletableFuture.completedFuture(cancel());
    }

    /**
     * Creates a new {@link ExportableTransaction} instance that represents a cancellation outcome.
     * <br/>
     * This method creates an exportable transaction with:
     * <ol>
     *   <li>A {@link Result} instance indicating a cancellation.</li>
     *   <li>A {@code null} export payload.</li>
     * </ol>
     * <br/>
     * This is typically used to signal a cancellation in the context of transaction export processes.
     *
     * @return a new {@link ExportableTransaction} instance representing a cancellation with no export payload.
     */
    @Contract(" -> new")
    public static @NotNull ExportableTransaction cancel() {
        return new ExportableTransaction(Result.cancel(), null);
    }

    /**
     * Creates a new {@link ExportableTransaction} instance representing a successful outcome with an associated export payload.
     * <br/>
     * This method initializes the {@link ExportableTransaction} with:
     * <ul>
     *   <li>A {@link Result} instance indicating success, wrapping the provided {@link Transaction}.</li>
     *   <li>A non-null {@link ExportPayload} instance containing the specified {@link OutputType} and payload data.</li>
     * </ul>
     * <br/>
     * This is typically used to generate a representation of a transaction that includes additional export metadata or content.
     *
     * @param tx         the {@link Transaction} to be included in the result. Must not be {@code null}.
     * @param outputType the type of output format for the export payload. Must not be {@code null}.
     * @param payload    the byte array containing the payload data for export. Must not be {@code null}.
     * @return a new {@link ExportableTransaction} instance encapsulating the successful transaction and the associated export payload.
     */
    @Contract(" _,_,_ -> new")
    public static @NotNull ExportableTransaction ok(Transaction tx, OutputType outputType, byte[] payload) {
        return new ExportableTransaction(Result.ok(tx), new ExportPayload(outputType, payload));
    }

    /**
     * Creates a new {@link ExportableTransaction} instance representing a successful outcome
     * without any associated export payload.
     * <br/>
     * This method initializes the {@link ExportableTransaction} with:
     * <ul>
     *   <li>A {@link Result} instance indicating success, wrapping the provided {@link Transaction}.</li>
     *   <li>A {@code null} export payload, as there is no additional data to export.</li>
     * </ul>
     * <br/>
     * This is typically used in cases where an operation succeeds but does not require exporting
     * additional payload data.
     *
     * @param tx the {@link Transaction} to be included in the result. Must not be {@code null}.
     * @return a new {@link ExportableTransaction} instance encapsulating the successful transaction
     * without any export payload.
     */
    @Contract("_ -> new")
    public static @NotNull ExportableTransaction okWithoutPayload(Transaction tx) {
        return new ExportableTransaction(Result.ok(tx), null);
    }

    /**
     * Asynchronously creates a completed {@link CompletableFuture} containing an {@link ExportableTransaction} instance
     * that represents a successful outcome without any associated export payload.
     * <br/>
     * This method is useful for signaling a success in export processes where no additional data needs to be exported,
     * without initiating asynchronous computations.
     * <br/>
     * The created {@link ExportableTransaction} encapsulates:
     * <ul>
     *   <li>A {@link Result} instance indicating success, wrapping the provided {@link Transaction}.</li>
     *   <li>A {@code null} export payload, as there is no additional data for export.</li>
     * </ul>
     *
     * @param tx the {@link Transaction} to be included in the result. Must not be {@code null}.
     * @return a {@link CompletableFuture} that is already completed with an {@link ExportableTransaction}
     * representing a successful outcome without any export payload.
     */
    @Contract("_ -> new")
    public static @NotNull CompletableFuture<ExportableTransaction> okWithoutPayloadCompleted(Transaction tx) {
        return CompletableFuture.completedFuture(okWithoutPayload(tx));
    }


}
