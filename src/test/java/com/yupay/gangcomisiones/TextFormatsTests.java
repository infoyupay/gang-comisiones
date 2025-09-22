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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for validating text format utilities provided by the {@link TextFormats} class,
 * specifically tailored for the Peruvian locale.
 * <br/>
 * <br/>
 * This class contains test methods for verifying the formatting of:
 * <ul>
 *   <li>Currency values with Peruvian standards.</li>
 *   <li>Percentage values with locale-specific settings.</li>
 * </ul>
 * <br/>
 * The tests use parameterized inputs to ensure accurate and consistent results when formatting
 * numerical values into strings, following Peruvian locale conventions.
 *<br/>
 * <div style="border: solid 1px black; padding: 2px">
 *  <strong>Execution note:</strong> dvidal@infoyupay.com passed 8 tests in 0.162s at 2025-09-22 07:14 UTC-5
 * </div>
 * @author InfoYupay SACS
 * @version 1.0
 */
class TextFormatsTest {

    /**
     * Tests the formatting of currency values for the Peruvian locale to ensure proper compliance
     * with format standards, including the correct use of the currency symbol "S/", the thousands separator ',',
     * and the decimal separator '.'.
     * <br/>
     * This test validates that the {@link DecimalFormat} instance returned by
     * {@link TextFormats#getCurrencyFormat()} is configured correctly for the Peruvian locale and produces
     * the expected output when formatting various numeric inputs.
     * <br/>
     * Additionally, it verifies the underlying {@link DecimalFormatSymbols} to ensure:
     * <ul>
     *   <li>The currency code is "PEN".</li>
     *   <li>The currency symbol is "S/".</li>
     *   <li>The grouping separator is ','.</li>
     *   <li>The decimal separator is '.'.</li>
     * </ul>
     *
     * @param input The numeric value to be formatted into Peruvian currency.
     * @param expected The expected formatted string output for the given {@code input}.
     */
    @ParameterizedTest(name = "Format for {0} must be {1}")
    @CsvSource({
            // value, expected
            "1, 'S/ 1.00'",
            "1000.5, 'S/ 1,000.50'",
            "1234567.89, 'S/ 1,234,567.89'",
            "0.99, 'S/ 0.99'"
    })
    void testCurrencyFormatForPeru(double input, String expected) {
        DecimalFormat format = TextFormats.getCurrencyFormat();

        String result = format.format(input).replace('\u00A0', ' ');

        // Contract validation.
        assertThat(result)
                .as("Peruvian currency format must use S/, comma for thousands and point for decimal")
                .isEqualTo(expected);

        // Also validate symbols.
        var symbols = format.getDecimalFormatSymbols();
        assertThat(symbols.getCurrency().getCurrencyCode()).isEqualTo("PEN");
        assertThat(symbols.getCurrencySymbol()).isEqualTo("S/");
        assertThat(symbols.getGroupingSeparator()).isEqualTo(',');
        assertThat(symbols.getDecimalSeparator()).isEqualTo('.');
    }

    /**
     * Tests the formatting of percentage values for the Peruvian locale to ensure they are
     * represented correctly according to defined format standards.
     * <br/>
     * This test validates that the {@link DecimalFormat} instance obtained from
     * {@link TextFormats#getPercentFormat()} produces the expected output for various numeric inputs
     * by transforming them into correctly formatted percentage strings.
     * <br/>
     * The test checks the following:
     * <ul>
     *   <li>Percentage values are formatted with two decimal places.</li>
     *   <li>Correct formatting for both fractional and whole numbers.</li>
     *   <li>Precision in representing percentage values as strings.</li>
     * </ul>
     *
     * @param input The numeric value (in decimal form) to be formatted as a percentage.
     * @param expected The expected formatted string output for the given {@code input}.
     */
    @ParameterizedTest(name = "Percentage format for {0} must be {1}")
    @CsvSource({
            "0.01, 1.00%",
            "0.1234, 12.34%",
            "1, 100.00%",
            "12.3456, 1234.56%"
    })
    void testPercentageFormatForPeru(double input, String expected) {
        DecimalFormat format = TextFormats.getPercentFormat();
        String result = format.format(input);

        assertThat(result).isEqualTo(expected);
    }

}

