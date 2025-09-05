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
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Contract;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents the global configuration settings for the application.
 * <br/>
 * This entity stores the application's general settings, such as the
 * legal name, business name, RUC, etc. It should only be modified by
 * a {@link UserRole#ROOT} user.
 * <br/>
 * There is a single instance of this entity, identified by ID = 1, and
 * enforced by a check constraint at database level. To strengthen the
 * integrity and security of this record, an additional unique constraint
 * is applied to the {@link #updatedBy} field. This ensures that:
 * <ul>
 *   <li>Even if the ID constraint is bypassed, the same {@link User}
 *       cannot be reused to update multiple records.</li>
 *   <li>The system preserves the one-to-one relationship between
 *       {@link GlobalConfig} and {@link User}.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "global_config", schema = "public")
public class GlobalConfig {
    /**
     * The unique identifier for the global configuration.
     * It always should be 1.
     */
    @Id
    @Column(name = "id", nullable = false)
    @Builder.Default
    private Short id = 1;

    /**
     * Represents the unique Tax Identification Number (RUC) of a business entity.
     * This field is mandatory and has a fixed length of 11 characters as per the
     * Peruvian legal and tax regulations. It is used to uniquely identify the
     * legal entity in the system and is stored in the "ruc" column of the
     * "global_config" database table.
     */
    @Setter
    @Column(name = "ruc", nullable = false, length = 11)
    @Pattern("\\d{11}")
    private String ruc;

    /**
     * Represents the registered legal name of a business entity.
     * This field is mandatory and is stored in the "legal_name" column
     * of the "global_config" database table. It is used to uniquely identify
     * the formal name associated with the organization's legal documentation.
     */
    @Setter
    @Column(name = "legal_name", nullable = false)
    private String legalName;

    /**
     * Represents the registered business name of a business entity.
     * This field is mandatory and is stored in the "business_name" column
     * of the "global_config" database table. It is used to uniquely identify
     * the commercial name associated with the organization's business operations.
     */
    @Setter
    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    /**
     * Represents the registered address of a business entity.
     * This field is mandatory and is stored in the "address" column
     * of the "global_config" database table. It is used to uniquely identify
     * the physical location associated with the organization's business operations.
     */
    @Setter
    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    /**
     * Represents the registered announcement of a business entity.
     * This field is optional and is stored in the "announcement" column
     * of the "global_config" database table. It is used to print a message
     * to the end customers in their comission information tickets.
     */
    @Setter
    @Column(name = "announcement", length = 80)
    private String announcement;

    /**
     * Timestamp of the last update for this entity.
     * <br/>
     * This value is automatically managed by the database:
     * <ul>
     *   <li>On insertion, the default value is set to the current timestamp.</li>
     *   <li>On updates, a database trigger automatically updates the timestamp.</li>
     * </ul>
     * <br/>
     * The application must not modify this field directly; it is read-only in the domain model.
     * <br/>
     * This field is mandatory and is stored in the "updated_at" column of the "global_config" table.
     */
    @Column(name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedAt;

    /**
     * References the {@link User} who last updated the global configuration.
     * <br/>
     * This field enforces a one-to-one relationship between {@link GlobalConfig}
     * and {@link User}, guaranteed by a unique constraint at the database level.
     * <br/>
     * Design considerations:
     * <ul>
     *   <li>Ensures that each configuration record is always tied to a single,
     *       unique {@link User}.</li>
     *   <li>Prevents the same {@link User} from being reused to update multiple
     *       {@link GlobalConfig} records, preserving system integrity.</li>
     *   <li>Acts as a secondary safeguard in case the check constraint on
     *       {@code id = 1} were ever bypassed.</li>
     * </ul>
     */
    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    /**
     * Represents the hostname of the client that last updated the global configuration record.
     * This field is mandatory and is stored in the "updated_from" column of the
     * "global_config" database table.
     */
    @Setter
    @Column(name = "updated_from", nullable = false, length = 100)
    private String updatedFrom;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof GlobalConfig that)) return false;

        //If one of the two objects isn't persisted yet (id==null), they aren't equals.
        if (getId() == null || that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        //If entity has an id -> use id hash.
        //If doesn't have an id yet (transient) -> use identityHashCode to distinguish different instances.
        return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
    }
}