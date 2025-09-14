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

import java.lang.annotation.*;

/**
 * Marks a type, field, method, or parameter as a candidate for a specific future feature
 * (e.g., {@code StableValue}).<br/>
 * <br/>
 * The {@code value} is mandatory and indicates the feature name.<br/>
 * The {@code reason} is optional and may provide extra context.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE) // only visible in source, not in compiled .class files
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER
})
public @interface SuitableFor {
    /**
     * The target feature name, e.g., "stableValue".
     *
     * @return the target feature name
     */
    String value();

    /**
     * Optional explanation of why this element is suitable.
     *
     * @return the reason for suitability
     */
    String reason() default "";
}

