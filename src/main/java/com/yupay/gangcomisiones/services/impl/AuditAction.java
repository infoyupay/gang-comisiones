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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Enumeration with actions for {@link com.yupay.gangcomisiones.model.AuditLog},
 * it centralizes the expected values in the action of AuditLog.
 * <br/>
 * Each enum has a description extracted from its Javadoc for more readable logs.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public enum AuditAction implements AuditLogger {
    /**
     * Creation of a new user.
     */
    USER_CREATE("Creation of a new user", "model.User"),

    /**
     * Change of password for a user.
     * A password change is when the user, knowing its own password, decides to change it.
     */
    USER_PASSWORD_CHANGE("Change of password by the user itself", "model.User"),

    /**
     * Password reset for a user. The password reset
     * is when a ROOT user forces a password change of another user.
     */
    USER_PASSWORD_RESET("Password reset forced by a ROOT user", "model.User"),

    /**
     * Change of permissions for a user.
     * A permission change is when a ROOT user changes the permissions of another user, like Linux chmod.
     */
    USER_CHMOD("Change of permissions by a ROOT user", "model.User"),

    /**
     * When a ROOT user changes the user active flag to false,
     * this implies a cleaning of the user random disabling such user to logon until password and flag reset.
     */
    USER_DISABLE("User disabled by a ROOT user, random password assigned", "model.User"),

    /**
     * When a ROOT user changes the user active flag to true,
     * this implies a password reset.
     */
    USER_ENABLE("User enabled by a ROOT user, password reset", "model.User"),

    /**
     * Creation of a new bank.
     */
    BANK_CREATE("User creates a new bank.", "model.Bank"),

    /**
     * Update of a bank's basic information.
     */
    BANK_UPDATE("User updates bank.", "model.Bank"),

    /**
     * Creation of a new concept.
     */
    CONCEPT_CREATE("User creates a new concept.", "model.Concept"),

    /**
     * Update of a concept's basic information.
     */
    CONCEPT_UPDATE("User updates concept.", "model.Concept"),

    /**
     * Creation of a new transaction.
     */
    TRANSACTION_CREATE("User creates tarnsaction.", "model.Transaction"),
    /**
     * Represents an audit action where a user requests the reversal of a transaction.
     * This action is associated with the "ReversalRequest" entity and provides a detailed
     * description for logging and tracking purposes.
     */
    REVERSAL_REQUEST_CREATE("User requests reversal of transaction.", "model.ReversalRequest"),
    /**
     * Represents an audit action where a user resolves a reversal request.
     * This action is associated with the "ReversalRequest" entity and provides a detailed
     * description for logging and tracking purposes.
     */
    REVERSAL_REQUEST_RESOLVE("User resolves reversal request.", "model.ReversalRequest"),
    /**
     * Represents an audit action where a user updates global configuration.
     * This action is associated with the "GlobalConfig" entity and provides a detailed
     * description for logging and tracking purposes.
     */
    GLOBAL_CONFIG_UPDATE("User updates global configuration.", "model.GlobalConfig");

    private final String description;
    private final String entity;

    /**
     * Constructs an AuditAction with the specified description.
     *
     * @param description the human-readable description of the audit action.
     * @param entity      the entity classname related to the action.
     */
    @Contract(pure = true)
    AuditAction(String description, String entity) {
        this.description = description;
        this.entity = entity;
    }

    @Contract(pure = true)
    @Override
    public String getDescription() {
        return description;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getAction() {
        return name();
    }

    @Contract(pure = true)
    @Override
    public String getEntity() {
        return entity;
    }

}
