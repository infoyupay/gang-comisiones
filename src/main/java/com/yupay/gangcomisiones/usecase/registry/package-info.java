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

/**
 * Registry utilities and semantic aliases for use case components (controllers and views).
 * <br/>
 * This package offers domain-oriented registry types and helpers that sit closer to use case abstractions while
 * remaining compatible with the generic registry infrastructure.
 * <br/>
 * Provided building blocks:
 * <ul>
 *   <li>
 *       {@link com.yupay.gangcomisiones.usecase.registry.UseCaseControllerRegistry}:
 *       alias to a type-based registry specialized for use case controllers.
 *   </li>
 *   <li>
 *       {@link com.yupay.gangcomisiones.usecase.registry.DefaultUseCaseControllerRegistry}:
 *       default implementation that adapts the shared registry behavior.
 *   </li>
 *   <li>
 *       {@link com.yupay.gangcomisiones.usecase.registry.ControllerRegistries}:
 *       convenience accessor exposing a lazily created default controller registry.
 *   </li>
 *   <li>
 *       {@link com.yupay.gangcomisiones.usecase.registry.ViewRegistry}:
 *       alias to emphasize intent for view components.
 *   </li>
 * </ul>
 * <br/>
 * Design notes:
 * <ul>
 *   <li>
 *   This package is intentionally separated from commons to keep generic utilities lightweight and avoid mixing them
 *   with use case–specific registries.
 *   </li>
 *   <li>
 *   Aliases preserve semantic clarity in the domain without introducing additional behavior beyond the underlying
 *   generic registry.
 *   </li>
 *   <li>
 *   The default controller registry is obtained on first access via a lazy holder. If multi-threaded access is expected,
 *   consider a thread-safe initialization strategy.
 *   </li>
 * </ul>
 * <br/>
 * Typical usage:
 * <ol>
 *   <li> Register controller and view types in their corresponding registries. </li>
 *   <li> Resolve instances by their declared type when wiring use case handlers and UI elements. </li>
 *   <li> Swap registry implementations in tests to isolate behavior and simplify setup. </li>
 * </ol>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
package com.yupay.gangcomisiones.usecase.registry;