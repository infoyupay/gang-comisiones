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

import com.yupay.gangcomisiones.export.TicketFormatter;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.Transaction;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Produces a minimal single-page PDF using the base 14 font Courier to render
 * the 32-column ticket text. No decorative borders are added.
 * <br/>
 * This implementation avoids external libraries and writes a compact PDF 1.4
 * by hand, adequate for simple text content.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class PdfTicketRenderer {
    /**
     * Generates a single-page PDF document using the provided text lines.
     * <br/>
     * Each line of text from the input is added to the PDF, displayed in Courier font, and rendered
     * on a standard A4 page with a fixed layout.
     * <br/>
     * The PDF structure is manually constructed, adhering to PDF version 1.4, and includes:
     * <ul>
     *     <li>A catalog object as the root of the document hierarchy.</li>
     *     <li>A pages object listing the single page in the document.</li>
     *     <li>A page object describing the page layout and its content stream.</li>
     *     <li>A font object referencing the Courier font.</li>
     *     <li>A content stream object containing the text rendering commands.</li>
     * </ul>
     * In the event of an error, a minimal empty PDF document is returned as a fallback.
     * <br/>
     *
     * @param lines a list of strings representing the lines to be written into the PDF; must not be null
     *              <ul>
     *                  <li>Each string is rendered on a new line, starting at the top-left corner.</li>
     *                  <li>
     *                      Non-ASCII characters in the text are escaped or replaced to ensure
     *                      compatibility with the PDF format.
     *                  </li>
     *              </ul>
     * @return a byte array containing the complete PDF file content; begins with the string "%PDF"
     */
    private static byte[] buildSimplePdf(List<String> lines) {
        try {
            var out = new ByteArrayOutputStream();
            var header = "%PDF-1.4\n";
            out.write(header.getBytes(StandardCharsets.US_ASCII));

            // Objects indices
            var xref1 = out.size();
            // 1: Catalog
            writeObj(out, 1, "<< /Type /Catalog /Pages 2 0 R >>\n");
            var xref2 = out.size();
            // 2: Pages
            writeObj(out, 2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n");

            // Prepare content stream
            var cs = new StringBuilder();
            cs.append("BT\n");
            cs.append("/F1 10 Tf\n");
            // Start position (left margin 50, top 800)
            cs.append("50 800 Td\n");
            for (var i = 0; i < lines.size(); i++) {
                var ln = escapePdf(lines.get(i));
                if (i == 0) {
                    cs.append("(").append(ln).append(") Tj\n");
                } else {
                    cs.append("0 -12 Td (").append(ln).append(") Tj\n");
                }
            }
            cs.append("ET\n");
            var csBytes = cs.toString().getBytes(StandardCharsets.US_ASCII);

            var xref3 = out.size();
            // 3: Page (A4 portrait)
            writeObj(out, 3, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] " +
                    "/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\n");

            var xref4 = out.size();
            // 4: Contents stream
            var contentHeader = "<< /Length " + csBytes.length + " >>\nstream\n";
            writeRawObj(out, 4, contentHeader, csBytes, "endstream\n");

            var xref5 = out.size();
            // 5: Font (Courier)
            writeObj(out, 5, "<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\n");

            var xrefStart = out.size();
            // xref table for 5 objects
            var xref = "xref\n0 6\n" +
                    String.format("%010d %05d f \n", 0, 65535) +
                    String.format("%010d %05d n \n", xref1, 0) +
                    String.format("%010d %05d n \n", xref2, 0) +
                    String.format("%010d %05d n \n", xref3, 0) +
                    String.format("%010d %05d n \n", xref4, 0) +
                    String.format("%010d %05d n \n", xref5, 0);
            out.write(xref.getBytes(StandardCharsets.US_ASCII));

            // trailer
            var trailer = "trailer\n" +
                    "<< /Size 6 /Root 1 0 R >>\n" +
                    "startxref\n" + xrefStart + "\n%%EOF";
            out.write(trailer.getBytes(StandardCharsets.US_ASCII));

            return out.toByteArray();
        } catch (Exception e) {
            // Fallback minimal PDF on unexpected error
            return ("""
                    %PDF-1.4
                    %ERR
                    1 0 obj<<>>endobj
                    trailer<< /Size 1 >>
                    startxref
                    0
                    %%EOF""").getBytes(StandardCharsets.US_ASCII);
        }
    }

    /**
     * Writes an object to a {@link ByteArrayOutputStream} in PDF format.
     * <br/>
     * The method constructs the object as a string in the following order:
     * <ul>
     *     <li>Appends the object number followed by " 0 obj\n".</li>
     *     <li>Adds the provided dictionary string.</li>
     *     <li>Appends "endobj\n" to mark the end of the object.</li>
     * </ul>
     * The resulting string is encoded in US-ASCII and written to the output stream.
     * <br/>
     *
     * @param out  the {@link ByteArrayOutputStream} where the object will be written; must not be null
     * @param num  the object number used in the PDF structure
     * @param dict the dictionary content to be included as part of the object; must not be null
     * @throws Exception if an I/O error occurs while writing to the output stream
     */
    private static void writeObj(@NotNull ByteArrayOutputStream out, int num, String dict) throws Exception {
        var s = num + " 0 obj\n" + dict + "endobj\n";
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Writes a raw object to a {@link ByteArrayOutputStream} in PDF format using the specified
     * parameters. The object is structured with a header, raw content, and a footer, and it
     * adheres to the format conventions used in PDF documents.
     * <br/>
     * The method constructs the object as follows:
     * <ol>
     *     <li>Writes the object number followed by " 0 obj\\n".</li>
     *     <li>Appends the provided prefix string as-is.</li>
     *     <li>Inserts the raw byte content.</li>
     *     <li>Appends the suffix string, followed by a newline and "endobj\\n".</li>
     * </ol>
     *
     * @param out    the {@link ByteArrayOutputStream} to which the object will be written; must not be null
     * @param num    the object number used in the PDF structure
     * @param prefix a string to be inserted before the raw byte content; must not be null
     * @param raw    the raw byte content to include in the object
     * @param suffix a string to be appended after the raw byte content
     * @throws Exception if an I/O error occurs while writing to the output stream
     */
    @SuppressWarnings("SameParameterValue")
    private static void writeRawObj(@NotNull ByteArrayOutputStream out,
                                    int num,
                                    @NotNull String prefix,
                                    byte[] raw,
                                    String suffix) throws Exception {
        out.write((num + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
        out.write(prefix.getBytes(StandardCharsets.US_ASCII));
        out.write(raw);
        out.write(("\n" + suffix + "\nendobj\n").getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Escapes a given string to make it safe for inclusion in a PDF document.
     * <br/>
     * The method performs the following operations:
     * <ol>
     *     <li>Replaces backslashes (`\`) with double backslashes (`\\`).</li>
     *     <li>Escapes parentheses by replacing `(` with `\(` and `)` with `\)`. </li>
     *     <li>Replaces non-ASCII characters (outside the range of 32-126) with the character `?`.</li>
     * </ol>
     *
     * @param s the input string to be escaped, can be null; if null, an empty string is returned
     * @return a new string that is safe for inclusion in a PDF document, never null
     */
    private static @NotNull String escapePdf(String s) {
        if (s == null) return "";
        var r = s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
        // Replace non-ASCII with '?'
        var sb = new StringBuilder(r.length());
        for (var i = 0; i < r.length(); i++) {
            var c = r.charAt(i);
            if (c < 32 || c > 126) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Renders the ticket to a PDF byte array (single page, Courier font).
     *
     * @param cfg global configuration
     * @param tx  transaction
     * @return PDF bytes beginning with %PDF
     */
    public byte[] render(GlobalConfig cfg, Transaction tx) {
        var text = TicketFormatter.format(cfg, tx);
        var lines = List.of(text.split("\n", -1));
        return buildSimplePdf(lines);
    }
}
