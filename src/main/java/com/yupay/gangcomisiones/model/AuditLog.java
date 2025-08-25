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

package com.yupay.gangcomisiones.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Contract;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * AuditLog entity class.<br/>
 * When any user performs an action, an AuditLog entry is created to track the event.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log", schema = "public")
public class AuditLog {
    /**
     * Represents the unique identifier for the AuditLog entity. This field is
     * automatically generated and populated using a database sequence.
     * <br/>
     * The sequence generator "audit_log_id_gen" is used to generate values
     * for this field, with the corresponding database sequence being "sq_audit_log_id".
     * <br/>
     * The field is non-nullable and is marked as the primary key of the entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "audit_log_id_gen")
    @SequenceGenerator(name = "audit_log_id_gen",
            sequenceName = "sq_audit_log_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    /**
     * Represents the timestamp of the event associated with the AuditLog entry.
     * This field stores an OffsetDateTime value and is automatically assigned
     * the current timestamp with the time zone when a new record is created.
     * <br/>
     * The field is non-nullable, and its value cannot be modified after insertion.
     * The database column is defined with "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP".
     */
    @Column(name = "event_stamp",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime eventStamp;

    /**
     * Represents the user who performed the action.
     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Represents the specific action performed by a user and logged in the AuditLog.
     * This field is mandatory and stores the brief description of the performed action.
     */
    @Setter
    @Column(name = "action", nullable = false)
    private String action;
    /**
     * Represents the name of the entity associated with the logged action.
     * This field is optional and may store a descriptive name indicating
     * the specific entity involved in the action.
     */
    @Setter
    @Column(name = "entity")
    private String entity;
    /**
     * Represents the unique identifier of the entity associated with the logged action.
     * This field is optional and typically used to track the specific instance
     * of an entity that was affected by the logged action.
     */
    @Setter
    @Column(name = "entity_id")
    private Long entityId;
    /**
     * Represents additional details or information related to the logged action.
     * This field stores a text value and is optional.
     */
    @Setter
    @Column(name = "details", columnDefinition = "text")
    private String details;
    /**
     * Represents the name of the computer or device from which the action was performed.
     * This field is mandatory and stores the host name of the computer or device.
     */
    @Setter
    @Column(name = "computer_name", nullable = false)
    private String computerName;

    @Contract(value = "null -> false", pure = true)
    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof AuditLog auditLog)) return false;

        //If one of the two objects isn't persisted yet (id==null), they aren't equals.
        if (getId() == null || auditLog.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), auditLog.getId());
    }

    @Override
    public int hashCode() {
        //If entity has an id -> use id hash.
        //If doesn't have an id yet (transient) -> use identityHashCode to distinguish different instances.
        return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
    }
}