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

import com.yupay.gangcomisiones.security.PasswordUtil;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;

/**
 * User entity class.<br/>
 * This user is not the database user, nor the computer's user. This user
 * is the system user and only resides in this table.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@NoArgsConstructor
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
    @Getter
    private Long id;
    /**
     * Represents the username of the User entity. This username
     * is the way users identify themselves within the system.
     */
    @Column(name = "username", nullable = false, unique = true)
    @Setter
    @Getter
    private String username;
    /**
     * Represents the hashed password of the User entity.
     * This field is used to securely store the user's password
     * in an irreversible hashed format within the database.
     * It is mapped to the "password_hash" column in the "user" table
     * and cannot be null nor empty.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    /**
     * Represents the salt for password of the User entity.
     * This field is used to securely store the user's password
     * in an irreversible hashed format within the database.
     * It is mapped to the "password_salt" column in the "user" table
     * and cannot be null nor empty.
     */
    @Column(name = "password_salt", nullable = false)
    private String passwordSalt;
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
    @Enumerated(EnumType.STRING)
    @Setter
    @Getter
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
    @Column(name = "active", nullable = false)
    @Setter
    @Getter
    private Boolean active = false;

    /**
     * Safe constructor, which only sets username, role, and active status.
     *
     * @param username the username.
     * @param role     the user role.
     * @param active   the active flag (if not specified, it's false).
     */
    @Contract(pure = true)
    @Builder(builderClassName = "UserBuilder")
    public User(String username, UserRole role, Boolean active) {
        this.username = username;
        this.role = role;
        this.active = active;
    }

    /**
     * Creates a {@link User} instance for testing purposes with the specified ID.
     *
     * @param id the unique identifier to set for the {@link User} instance
     * @return a {@link User} instance with the provided ID
     */
    @VisibleForTesting
    static @NotNull User forTest(Long id) {
        var r = new User();
        r.id = id;
        return r;
    }

    /**
     * Sets the password for this user. Plain password minimum length is 8, and not blank.
     * <br/>
     * This method generates a new cryptographic salt and hashes the provided plain text password.
     * The resulting salt and hash are stored internally in the entity.
     *
     * @param plainPassword the plain text password to be set
     */
    public void setPassword(@NotNull String plainPassword) {
        Objects.requireNonNull(plainPassword, "Password cannot be null.");
        if (plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }
        if (plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);
        this.passwordSalt = salt;
        this.passwordHash = hash;
    }

    /**
     * Verifies the provided plain text password against the stored salt and hash.
     *
     * @param plainPassword the plain text password to be verified.
     * @return true if the password matches, false otherwise.
     */
    public boolean verifyPassword(@NotNull String plainPassword) {
        return PasswordUtil.verifyPassword(plainPassword, passwordSalt, passwordHash);
    }

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

    /**
     * Safe builder: password is managed unly via a plainpassword.
     */
    public static class UserBuilder {
        private String plainPassword;

        /**
         * Sets the plain text password for the user being built. This password will be stored
         * and processed securely within the {@code build} method, which will internally
         * generate a salt and hash for the password.
         *
         * @param plainPassword the plain text password to be assigned to the user
         * @return the current instance of {@code UserBuilder} for method chaining
         */
        public UserBuilder password(String plainPassword) {
            this.plainPassword = plainPassword;
            return this;
        }

        /**
         * Constructs a new {@code User} instance using the specified builder properties.
         * The method initializes the {@code username}, {@code role}, and {@code active} fields
         * to the values provided in the builder. If a plain text password is specified,
         * it automatically processes the password to generate a salt and hash, securely
         * storing them in the resulting {@code User} instance.
         *
         * @return a fully constructed {@code User} instance with the specified properties and,
         * if applicable, a securely hashed password and generated salt.
         */
        public User build() {
            User user = new User();
            user.username = this.username;
            user.role = this.role;
            user.active = this.active != null && this.active;

            if (plainPassword != null && !plainPassword.isBlank()) {
                user.setPassword(plainPassword); // creates salt + hash internally.
            }

            return user;
        }
    }
}