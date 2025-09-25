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
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * The {@code AppMode} enum represents different operational modes of an application.
 * These modes determine how the application interacts with data and its underlying structure.
 * <br/><br/>
 * Available modes:
 * <ul>
 *     <li>{@link #WORK} - The application operates on production data.</li>
 *     <li>
 *         {@link #TOY} - The application interacts with a test database,
 *         simulating real workflow without affecting production.
 *     </li>
 *     <li>
 *         {@link #GHOST} - The application works with temporary test data that is deleted after execution,
 *         giving a fresh state for each session.
 *     </li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum AppMode {
    /**
     * Represents the real work application mode, it'll make point
     * the app to real - production data.
     */
    WORK,
    /**
     * Represents the toy application mode, it'll make point
     * the app to test data, persisted in a real database, but
     * such database is a test database, so any input won't impact
     * the real workflow.
     */
    TOY,
    /**
     * Represents the ghost application mode, it'll make point
     * the app to test data, persisted in a real database, but
     * such database and any persisted file will be deleted after
     * app execution. Each time the app is started in this mode
     * is like the first time.
     */
    GHOST,
    /**
     * Represents the test mode, for JUnit testing purposes.
     * DON'T USE IN REAL LIFE.
     */
    @TestOnly
    @VisibleForTesting
    TEST;

    /**
     * Converts a given string to its corresponding {@link AppMode} enumeration constant.
     * The input string is normalized by removing leading and trailing whitespaces and converting it to uppercase.
     * If the conversion fails (e.g., due to an invalid value), the default {@link AppMode#WORK} is returned.
     *
     * @param value The string representation of the desired {@link AppMode}.
     *              <ul>
     *                  <li>This value is case-insensitive.</li>
     *                  <li>Leading and trailing whitespaces are ignored.</li>
     *              </ul>
     *              <br/>
     *              If the value does not match any {@link AppMode}, the method will return {@link AppMode#WORK}.
     * @return The corresponding {@link AppMode} constant if the conversion is successful.
     * <ul>
     *     <li>Returns {@link AppMode#WORK} if the input is invalid or null.</li>
     * </ul>
     * @throws GangComisionesException if the input value is "TEST" and the method is called in a non-test environment.
     */
    public static AppMode from(String value) {
        if (value == null) return WORK;
        var normalized = value.strip().toUpperCase();
        if ("TEST".equals(normalized)) {
            throw new GangComisionesException("TEST mode is not allowed for real app execution.");
        }
        try {
            return AppMode.valueOf(normalized);
        } catch (IllegalArgumentException _) {
            return WORK;
        }
    }
}
