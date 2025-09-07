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
 * Provides infrastructure for monitoring the execution of long-running use cases.<br/>
 * <br/>
 * The classes and interfaces in this package define a generic contract to report
 * progress, status messages, and lifecycle events during task execution. This
 * allows use cases to decouple their business logic from the user interface,
 * while still giving feedback about ongoing operations.<br/>
 * <br/>
 * Typical responsibilities include:
 * <ul>
 *   <li>Publishing progress updates with a current/total ratio.</li>
 *   <li>Reporting status messages to inform the user of the current activity.</li>
 *   <li>Signaling completion or failure of a monitored task.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
package com.yupay.gangcomisiones.usecase.task;