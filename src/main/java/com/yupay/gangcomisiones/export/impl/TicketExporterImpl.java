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
package com.yupay.gangcomisiones.export.impl;

import com.yupay.gangcomisiones.export.OutputType;
import com.yupay.gangcomisiones.export.TicketExporter;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Default implementation of {@link TicketExporter}. Delegates the actual rendering
 * to specialized renderers and executes work asynchronously on the provided executor.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class TicketExporterImpl implements TicketExporter {
    private final Executor executor;
    private final HtmlTicketRenderer html = new HtmlTicketRenderer();
    private final PdfTicketRenderer pdf = new PdfTicketRenderer();
    private final EscPosTicketRenderer esc = new EscPosTicketRenderer();

    /**
     * Creates a new exporter using the given executor for async work.
     *
     * @param executor the executor to run tasks; if null, a default common pool will be used
     */
    public TicketExporterImpl(@NotNull Executor executor) {
        this.executor = Objects.requireNonNull(executor, "An executor must be provided.");
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<byte[]> export(Transaction tx, OutputType type, GlobalConfig cfg) {
        Objects.requireNonNull(tx, "tx");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(cfg, "cfg");
        return CompletableFuture.supplyAsync(() -> switch (type) {
            case PREVIEW_HTML -> html.render(cfg, tx);
            case PDF -> pdf.render(cfg, tx);
            case PRINTER_TICKET -> esc.render(cfg, tx);
        }, executor);
    }
}
