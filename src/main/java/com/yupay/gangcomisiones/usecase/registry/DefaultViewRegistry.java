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

package com.yupay.gangcomisiones.usecase.registry;

import com.yupay.gangcomisiones.registry.DefaultTypeRegistry;

/**
 * Backwards-compatible adapter extending {@link DefaultTypeRegistry} for use case views.
 * <br/>
 * Prefer using {@link DefaultTypeRegistry} directly for generic components.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class DefaultViewRegistry extends DefaultTypeRegistry implements ViewRegistry {
    // Inherits all behavior from DefaultTypeRegistry.
}
