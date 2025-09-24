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

import java.nio.charset.StandardCharsets;

/**
 * Renders the 32-column ticket as a minimal UTF-8 HTML document
 * containing a single &lt;pre&gt; block with monospaced text. No borders
 * or decorative marks are added.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class HtmlTicketRenderer {
    /**
     * Produces HTML bytes with a single &lt;pre&gt; containing the textual ticket.
     *
     * @param cfg global configuration
     * @param tx  transaction
     * @return UTF-8 bytes of a minimal, valid HTML document
     */
    public byte[] render(GlobalConfig cfg, Transaction tx) {
        var content = TicketFormatter.format(cfg, tx);
        var escaped = escapeHtml(content);
        var html = "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head><meta charset=\"UTF-8\"><title>Ticket</title></head>\n" +
                "<body><" + "pre style=\"font-family:monospace;\">" + escaped + "</" + "pre></body>\n" +
                "</html>";
        return html.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Escapes special HTML characters in the provided string to ensure it can be safely
     * included within an HTML document without breaking the HTML structure or introducing
     * vulnerabilities like cross-site scripting (XSS).
     * <br/>
     * This method replaces the characters `&`, `<`, and `>` with their corresponding
     * HTML entities:
     * <ul>
     *     <li>&amp;amp; - Replaces `&amp;`</li>
     *     <li>&amp;lt; - Replaces `&lt;`</li>
     *     <li>&amp;gt; - Replaces `&gt;`</li>
     * </ul>
     *
     * @param s the input string that may contain special HTML characters; must not be null
     * @return a new string with all special HTML characters safely escaped; never null
     */
    private static @NotNull String escapeHtml(@NotNull String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
