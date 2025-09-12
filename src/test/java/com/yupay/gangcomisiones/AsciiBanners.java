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

package com.yupay.gangcomisiones;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class with predefined ASCII banners for use cases and tests.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class AsciiBanners {

    /**
     * Banner shown at the start of a use case.
     */
    public static final String WELCOME_USE_CASE_BANNER = """
            +------------------------------------------------------------+
            |                                                            |
            |                 Welcome to this use case!                  |
            |              Please follow the steps below.                |
            |                                                            |
            +------------------------------------------------------------+
            """.stripIndent();

    /**
     * Banner shown at the end of a use case.
     */
    public static final String GOODBYE_USE_CASE_BANNER = """
            +------------------------------------------------------------+
            |                                                            |
            |             Thanks for completing this use case!           |
            |                  Your work is appreciated.                 |
            |                                                            |
            +------------------------------------------------------------+
            """.stripIndent();

    /**
     * Alert shown when a role is propagated.
     */
    public static final String PRIVILEGE_PROPAGATION_ALERT =
            "🛡️⚔️ Hold it right there, cowboy! You’ve been assigned the role %s%n";

    /**
     * Prevents instantiation.
     */
    @Contract(pure = true)
    private AsciiBanners() {
    }

    /**
     * Builds a banner with a custom message, boxed in ASCII format.
     *
     * @param message the message to display inside the banner
     * @return a formatted ASCII banner
     */
    @Contract(pure = true)
    public static @NotNull String box(String message) {
        String line = "+------------------------------------------------------------+";
        return """
                %s
                | %-58s |
                %s
                """.formatted(line, message, line);
    }
}
