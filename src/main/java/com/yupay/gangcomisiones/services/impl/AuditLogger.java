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

package com.yupay.gangcomisiones.services.impl;

import com.yupay.gangcomisiones.exceptions.GangComisionesException;
import com.yupay.gangcomisiones.model.AuditLog;
import com.yupay.gangcomisiones.model.User;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * AuditLogger interface provides methods and default implementations for managing audit logs.
 * It defines operation-related information like actions, entities, descriptions, and the ability
 * to create a new audit log entry.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface AuditLogger {
    /**
     * Retrieves the action associated with the current audit log entry.
     *
     * @return a string representing the action.
     */
    String getAction();

    /**
     * Retrieves the name or type of the entity associated with the audit log entry.
     *
     * @return a string representing the entity.
     */
    String getEntity();

    /**
     * Retrieves a description of the action or entity associated with the audit log entry.
     *
     * @return a string representing the description.
     */
    String getDescription();

    /**
     * Creates an audit log entry based on the action, entity, and additional details
     * associated with the operation performed.
     *
     * @param actor    the user who performed the action
     * @param entityId the unique identifier of the entity associated with the action
     * @return a new {@code AuditLog} instance representing the logged event
     */
    default AuditLog createAuditLog(User actor,
                                    long entityId) {
        return AuditLog.builder()
                .user(actor)
                .action(getAction())
                .entity(getEntity())
                .entityId(entityId)
                .details(getDescription())
                .computerName(computerName())
                .build();
    }

    /**
     * Retrieves the name of the computer where the application is running.
     *
     * @return the hostname of the computer.
     * @throws GangComisionesException if the hostname cannot be retrieved.
     */
    default String computerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new GangComisionesException("Cannot retrieve computer name.", e);
        }
    }
}
