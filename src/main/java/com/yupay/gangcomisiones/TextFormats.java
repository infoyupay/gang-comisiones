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

import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class providing locale-specific formatting for text, especially tailored
 * to the Peruvian locale settings.
 * <br/>
 * <br/>
 * This class is designed to deliver methods and constants that facilitate the generation
 * of formatting objects for currency and percentage values specific to the Peruvian context.
 * It is implemented as a final class with a private constructor to ensure it remains non-instantiable.
 * <br/>
 * <br/>
 * Key features provided by this class:
 * <ol>
 *   <li>Locale-specific configurations for Peru (Spanish language, Peruvian currency).</li>
 *   <li>Methods to obtain instances of {@link DecimalFormat} for formatting currency and percentage values.</li>
 * </ol>
 * <br/>
 * <br/>
 * Please note that objects provided by this class are intended for use in applications
 * where formatting properly complies with the requirements of the Peruvian locale.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class TextFormats {
    /**
     * Defines a {@link Locale} constant tailored for Peruvian locale settings.
     * <br/>
     * This locale is specifically configured for the Spanish language ("es") and the country Peru ("PE").
     * It is primarily utilized in conjunction with methods or operations requiring locale-specific formatting,
     * such as currency and percentage representations.
     * <br/>
     * Features of this locale:
     * <ul>
     *   <li>Language Code: es (Spanish)</li>
     *   <li>Country Code: PE (Peru)</li>
     *   <li>Applied to locale-sensitive operations like number and currency formatting.</li>
     * </ul>
     */
    private static final Locale PERUVIAN_LOCALE = Locale.of("es", "PE");

    /**
     * Private constructor to avoid instanciating this utility class.
     */
    @Contract(pure = true)
    private TextFormats() {
    }

    /**
     * Retrieves a {@link DecimalFormat} instance configured for formatting
     * and parsing currency values specific to the Peruvian locale settings.
     * <br/>
     * This method ensures the {@link DecimalFormat} instance is properly established
     * with the following features:
     * <ul>
     *   <li>Set to use the locale-specific settings for currency representation in Peru.</li>
     *   <li>Configured to parse currency values into {@link java.math.BigDecimal} for precision.</li>
     * </ul>
     * <br/>
     * If the instance acquired from {@link DecimalFormat#getCurrencyInstance(Locale)}
     * is not of type {@link DecimalFormat}, a {@link GangComisionesException} will be thrown.
     * <br/>
     * <br/>
     *
     * @return A {@link DecimalFormat} object tailored for currency formatting in the Peruvian locale.
     * @throws GangComisionesException if the instance obtained is not of type {@link DecimalFormat}.
     */
    @Contract("-> new")
    public static @NotNull DecimalFormat getCurrencyFormat() {
        var z = "";
        var r = DecimalFormat.getCurrencyInstance(PERUVIAN_LOCALE);
        if (r instanceof DecimalFormat df) {
            df.setParseBigDecimal(true);
            return df;
        } else {
            throw new GangComisionesException(
                    "Expected DecimalFormat instance in TextFormats.getCurrencyFormat, found "
                            + r.getClass());
        }
    }

    /**
     * Retrieves a {@link DecimalFormat} instance configured for formatting percentages
     * specific to the Peruvian locale settings.
     * <br/>
     * This method ensures the {@link DecimalFormat} instance is properly established with the
     * following features:
     * <ul>
     *   <li>Formatting pattern: "#0.00%", representing percentage values with two decimal places.</li>
     *   <li>Locale settings tailored for Peru, using {@link DecimalFormatSymbols} specific to the Peruvian locale.</li>
     *   <li>Configured to parse percentage values into {@link java.math.BigDecimal} for precision.</li>
     * </ul>
     * <br/>
     * <br/>
     *
     * @return A {@link DecimalFormat} object tailored for percentage formatting with Peruvian locale settings.
     */
    @Contract("-> new")
    public static @NotNull DecimalFormat getPercentFormat() {
        var r = new DecimalFormat("#0.00%", new DecimalFormatSymbols(PERUVIAN_LOCALE));
        r.setParseBigDecimal(true);
        return r;
    }
}
