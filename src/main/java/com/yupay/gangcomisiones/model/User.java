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

import java.util.Objects;

/**
 * User entity class.<br/>
 * This user is not the database user, nor the computer's user. This user
 * is the system user and only resides in this table.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"", schema = "public")
public class User {
    /**
     * Represents the identifier of the User entity.
     * This field is annotated as the primary key of the entity and is automatically
     * generated using a sequence strategy. The sequence generator is defined with
     * the name "user_id_gen", which uses the sequence "sq_user_id" in the default
     * "public" schema with an allocation size of 1.
     * The corresponding database column is named "id" and does not allow null values.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "user_id_gen")
    @SequenceGenerator(name = "user_id_gen",
            sequenceName = "sq_user_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Represents the username of the User entity. This username
     * is the way users identify themselves within the system.
     */
    @Column(name = "username", nullable = false, unique = true)
    @Setter
    private String username;

    /**
     * Represents the hashed password of the User entity.
     * This field is used to securely store the user's password
     * in an irreversible hashed format within the database.
     * It is mapped to the "password_hash" column in the "user" table
     * and cannot be null nor empty.
     */
    @Column(name = "password_hash", nullable = false)
    @Setter
    private String passwordHash;

    /**
     * Represents the role assigned to the User entity within the system.
     * <br/>
     * This field is mapped to the "role" column in the "user" table and
     * is defined as not allowing null values in the database.
     * <br/>
     * It indicates the specific authorization or responsibility level of the user.
     * The column is backed by the PostgreSQL enum type {@code user_role}.
     * <br/>
     * For the distinct levels, see the UserRole enum.
     *
     * @see UserRole
     */
    @Column(name = "role", columnDefinition = "user_role not null")
    @Setter
    private UserRole role;

    /**
     * Represents the status of the User entity within the system.<br/>
     * This field is mapped to the "active" column in the "user" table
     * and is defined as not allowing null values in the database.<br/>
     * It indicates whether the user account is active ({@code true})
     * or inactive ({@code false}).
     * <br/>
     * By design, this column is defined with {@code DEFAULT false} at the database level.
     * This ensures that if a user is inserted without explicitly specifying the "active" value,
     * the account will be inactive by default, which reinforces system security.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    @Setter
    private Boolean active = false;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof User user)) return false;

        //If one of the two objects isn't persisted yet (id==null), they aren't equals.
        if (getId() == null || user.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        //If entity has an id -> use id hash.
        //If doesn't have an id yet (transient) -> use identityHashCode to distinguish different instances.
        return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
    }
}