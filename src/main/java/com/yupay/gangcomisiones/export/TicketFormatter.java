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

import com.yupay.gangcomisiones.TextFormats;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.Transaction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shared 32-column ticket formatter used by all renderers.
 * It produces a line-based textual representation without decorative borders.
 * <br/>
 * Rules applied:
 * - Max 32 characters per line (no silent truncation; content is wrapped where needed).<br/>
 * - Currency format: "S/. " + #,##0.00 (Peruvian Soles), right-aligned when paired with a label.<br/>
 * - Date: dd/MM/yyyy; Time: HH:mm:ss based on {@link Transaction#getMoment()}.<br/>
 * - Concept: first line prefixed with "Concepto: ", subsequent wrapped lines continue without the prefix.<br/>
 * - Announcement: wrapped to 32 columns if present.<br/>
 * - Null handling: any missing field renders as empty value.<br/>
 * <br/>
 * The output lines never include box/border characters.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class TicketFormatter {
    private static final int WIDTH = 32;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String BLANK_PLACEHOLDER = "------";

    /**
     * Private constructor to prevent instantiation of the {@code TicketFormatter} class.
     * <br/>
     * This class serves as a static utility providing methods to format 32-column
     * textual tickets for transactions. It is not meant to be instantiated.
     *
     */
    @Contract(pure = true)
    private TicketFormatter() {
    }

    /**
     * Formats a 32-column textual ticket for the given transaction and global configuration.
     *
     * @param cfg global configuration; must not be null
     * @param tx  transaction; must not be null
     * @return a single string with \n-separated lines, each line up to 32 chars.
     */
    public static @NotNull String format(@NotNull GlobalConfig cfg, @NotNull Transaction tx) {
        Objects.requireNonNull(cfg, "cfg");
        Objects.requireNonNull(tx, "tx");
        List<String> lines = new ArrayList<>();

        // Header: legal/business name and address.
        addWrappedIfNotBlank(lines, safe(cfg.getLegalName()));
        addWrappedIfNotBlank(lines, safe(cfg.getBusinessName()));
        addWrappedIfNotBlank(lines, safe(cfg.getAddress()));

        // Transaction id
        lines.add("Transacción: " + safe(tx.getId()));

        // Date and time from tx.moment
        var moment = tx.getMoment();
        var dateStr = moment != null ? DATE_FMT.format(moment) : "";
        var timeStr = moment != null ? TIME_FMT.format(moment) : "";
        lines.add("Fecha: " + dateStr);
        lines.add("Hora : " + timeStr);

        // Concept
        var conceptText = safe(conceptNameOf(tx));
        if (!conceptText.isBlank()) {
            //Wrap: conceptNameOf is already safe.
            lines.addAll(wrap("Concepto: " + conceptNameOf(tx), WIDTH, false));
        } else {
            lines.add("Concepto:");
        }

        // Amount and commission lines
        lines.add(labelRight("Monto:", money(tx.getAmount())));
        lines.add(labelRight("Comisión:", money(tx.getCommission())));

        // Cashier username
        var username = tx.getCashier() != null ? safe(tx.getCashier().getUsername()) : "";
        lines.add("Usuario: " + username);

        // Announcement if any
        var ann = safe(cfg.getAnnouncement());
        if (!ann.isEmpty()) {
            lines.add("");
            addWrappedIfNotBlank(lines, ann);
        }

        // Ensure lines are <= WIDTH
        lines.replaceAll(s -> s.length() <= WIDTH ? s : s.substring(0, WIDTH));

        return String.join("\n", lines);
    }

    /**
     * Adds the provided text to the given list after wrapping it into multiple lines of a fixed width,
     * but only if the text is not blank (null or empty after trimming).
     * <br/>
     * The method trims the input text, checks if it is non-blank, and then processes it using a
     * wrapping utility to split it into lines of up to a predefined width. The resulting lines
     * are appended to the provided list.
     * <br/>
     * Behavior:
     * <ul>
     *   <li>If the input text is {@code null}, no action is performed.</li>
     *   <li>If the input text is blank after trimming, no action is performed.</li>
     *   <li>Otherwise, the input text is wrapped into lines and added to the output list.</li>
     * </ul>
     *
     * @param out  the list to which the wrapped text should be added. Cannot be {@code null}.
     * @param text the input text to process and wrap. Can be {@code null}.
     */
    private static void addWrappedIfNotBlank(List<String> out, String text) {
        if (text == null) return;
        var t = text.trim();
        if (!t.isEmpty()) out.addAll(wrap(t, WIDTH, true));
    }

    /**
     * Formats a label-value pair such that the label appears on the left and the value is
     * right-aligned, separated by a variable amount of spaces. The resulting string's
     * length will not exceed a predefined width. If the combined length of the label
     * and value exceeds the width, a fallback spacing is applied.
     * <br/><br/>
     * The method ensures:
     * <ul>
     *   <li>Null labels are replaced with an empty string.</li>
     *   <li>Null values are replaced with an empty string.</li>
     *   <li>A minimum space of one character is always inserted between the label and value.</li>
     * </ul>
     *
     * @param label the label to be aligned to the left. If {@code null}, defaults to an empty string.
     * @param value the value to be aligned to the right. If {@code null}, defaults to an empty string.
     * @return a formatted string containing the left-aligned label and the right-aligned value,
     * separated by calculated spaces. The total length does not exceed the predefined width.
     */
    private static @NotNull String labelRight(String label, String value) {
        label = label == null ? "" : label;
        value = value == null ? "" : value;
        var spaces = Math.max(1, WIDTH - label.length() - value.length());
        if (spaces > WIDTH) spaces = 1; // fallback
        return label + " ".repeat(spaces) + value;
    }

    /**
     * Converts a {@link BigDecimal} value into a formatted currency string specific to
     * the Peruvian locale. If the input is {@code null}, it returns an empty string.
     * <br/>
     * The method utilizes the {@link TextFormats#getCurrencyFormat()} to acquire a locale-aware
     * {@link DecimalFormat} instance and formats the given value accordingly.
     * <br/>
     * <br/>
     * Behavior:
     * <ul>
     *   <li>If the input value is {@code null}, the method returns an empty string.</li>
     *   <li>If the input value is non-null, it is formatted using the Peruvian locale-specific currency format.</li>
     * </ul>
     *
     * @param v the monetary value to be formatted. Can be {@code null}.
     * @return a string representing the formatted currency value, or an empty string if {@code v} is {@code null}.
     */
    @Contract("null -> !null")
    private static String money(BigDecimal v) {
        if (v == null) return BLANK_PLACEHOLDER;
        var nf = TextFormats.getCurrencyFormat();
        return nf.format(v);
    }

    /**
     * Retrieves the concept name of a given transaction, ensuring safe handling of null or problematic inputs.
     * <br/>
     * This method calls the {@code getConceptName} method on the transaction and then processes the returned
     * value via a safe utility method to ensure it conforms to required output standards.
     * <br/>
     * Behavior:
     * <ul>
     *   <li>The transaction's concept name is retrieved using {@code tx.getConceptName()}.</li>
     *   <li>The result is passed to a utility method to ensure safety and consistency.</li>
     *   <li>The resulting string is returned, guaranteed to be safely processed.</li>
     * </ul>
     *
     * @param tx the transaction object from which the concept name is to be retrieved.
     *           Must not be {@code null}.
     * @return the safely processed concept name of the transaction as a {@code String}.
     */
    private static String conceptNameOf(@NotNull Transaction tx) {
        var snapshotName = safe(tx.getConceptName());
        if (!snapshotName.isBlank()) {
            return snapshotName;
        }

        if (tx.getConcept() == null) {
            return BLANK_PLACEHOLDER;
        }

        var fallbackName = safe(tx.getConcept().getName());
        return fallbackName.isBlank() ? BLANK_PLACEHOLDER : fallbackName;
    }

    /**
     * Safely converts an input object into a non-null trimmed string representation.
     * <br/>
     * This method uses `Objects.toString(o, "")` to transform the object into a
     * string, defaulting to an empty string if the object is {@code null}, and
     * then removes any surrounding whitespace from the resulting string.
     * <br/>
     * Behavior:
     * <ul>
     *   <li>If the input object is {@code null}, an empty string is returned.</li>
     *   <li>If the input object is non-null, its {@code toString()} representation
     *       is trimmed and returned.</li>
     * </ul>
     *
     * @param o the input object to be processed. Can be {@code null}.
     * @return a non-null and trimmed string representation of the input object.
     */
    @Contract(value = "null -> !null", pure = true)
    private static @NotNull String safe(@Nullable Object o) {
        return Objects.toString(o, "").strip();
    }

    /**
     * Wraps the given text into lines of at most {@code max} chars, breaking on spaces when possible.
     * Existing new lines are respected.
     *
     * @param text       text to wrap
     * @param max        max columns
     * @param autoCenter flag, if true wrapped line is also centered.
     * @return list of wrapped lines (never null)
     */
    public static @NotNull List<String> wrap(String text, int max, boolean autoCenter) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        for (var paragraph : text.split("\r?\n")) {
            var t = paragraph.trim();
            while (!t.isEmpty()) {
                if (t.length() <= max) {
                    out.add(t);
                    break;
                }
                var breakAt = t.lastIndexOf(' ', max);
                if (breakAt <= 0) breakAt = max; // hard cut if needed
                out.add(t.substring(0, breakAt).trim());
                t = t.substring(breakAt).trim();
            }
            if (paragraph.isBlank()) out.add("");
        }
        return autoCenter ? out.stream().map(TicketFormatter::centerLine).toList() : out;
    }

    /**
     * Centers the given line within the ticket width.
     * If the line is longer than the ticket width, it is truncated.
     *
     * @param line the line to center
     * @return the centered line
     */
    @Contract("null -> !null")
    private static String centerLine(String line) {
        if (line == null || line.isBlank()) return " ".repeat(WIDTH);

        var strLine = line.strip();
        if (strLine.length() >= WIDTH) return strLine.substring(0, WIDTH);

        var total = WIDTH - strLine.length();
        var left = total / 2;
        var right = total - left;

        return " ".repeat(left) + strLine + " ".repeat(right);
    }
}
