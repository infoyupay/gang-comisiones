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

import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Renders ASCII bytes for ESC/POS printers. Adds the paper cut command at the end
 * (GS V B 0 -> bytes 0x1D, 'V', 66, 0). Also prefixes with ESC @ (initialize).
 * No decorative borders are included.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class EscPosTicketRenderer {
    private static final byte ESC = 0x1B; // 27
    private static final byte GS = 0x1D;  // 29

    /**
     * Converts a given string into a byte array using the US-ASCII encoding.
     * Non-ASCII characters are replaced with a specified placeholder byte ('*').
     * <br/>
     * This method handles characters that are unmappable or malformed by replacing
     * them to ensure compatibility with US-ASCII encoded environments.
     * In case of encoding failure, a simple fallback replacement is applied.
     *
     * @param s the input string to be converted to a US-ASCII encoded byte array
     *          <ul>
     *             <li>Characters outside the ASCII range are replaced with '*'</li>
     *          </ul>
     * @return a byte array containing the US-ASCII encoded representation of the input string with
     * non-ASCII characters replaced
     */
    private static byte @NotNull [] toAsciiReplacing(String s) {
        var enc = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .onMalformedInput(CodingErrorAction.REPLACE)
                .replaceWith(new byte[]{'*'});
        try {
            var bb = enc.encode(CharBuffer.wrap(s));
            var arr = new byte[bb.remaining()];
            bb.get(arr);
            return arr;
        } catch (Exception e) {
            // Fallback simple replace
            return s.replaceAll("[^\\x00-\\x7F]", "*").getBytes(StandardCharsets.US_ASCII);
        }
    }

    /**
     * Produces ESC/POS bytes in US-ASCII with an initialization prefix and a cut command suffix.
     * Any non-ASCII characters are replaced by '?'.
     *
     * @param cfg global configuration
     * @param tx  transaction
     * @return bytes suitable for sending to a ticket printer
     */
    public byte[] render(GlobalConfig cfg, Transaction tx) {
        var content = TicketFormatter.format(cfg, tx) + "\n"; // ensure trailing newline before cut
        var ascii = toAsciiReplacing(content);
        var prefix = new byte[]{ESC, '@'}; // ESC @
        var cut = new byte[]{GS, 'V', 66, 0}; // GS V B 0

        var out = new byte[prefix.length + ascii.length + cut.length];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(ascii, 0, out, prefix.length, ascii.length);
        System.arraycopy(cut, 0, out, prefix.length + ascii.length, cut.length);
        return out;
    }
}
