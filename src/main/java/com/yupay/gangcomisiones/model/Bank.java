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
 * Bank entity class.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bank", schema = "public")
public class Bank {
    /**
     * Represents the identifier of the Bank entity.
     * <br/>
     * This field is annotated as the primary key of the entity and is
     * auto-generated using a sequence strategy. The sequence generator
     * is defined with the name "bank_id_gen", which uses the sequence
     * "sq_bank_id" with an allocation size of 1. The corresponding
     * database column is named "id" and does not allow null values.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "bank_id_gen")
    @SequenceGenerator(name = "bank_id_gen",
            sequenceName = "sq_bank_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;
    /**
     * Represents the name of the Bank entity.
     * This field is mapped to the "name" column in the "bank" database table
     * and is a required field (cannot be null).
     */
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Represents the meta-status of the Bank entity. If it is false, it means that the
     * bank has been deleted, but is only maintained in database due to historical consistency.
     * This field is mapped to the "active" column in the "bank" database table
     * and is a required field (cannot be null).
     */
    @Setter
    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Bank bank)) return false;

        //If one of the two objects isn't persisted yet (id==null), they aren't equals.
        if (getId() == null || bank.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), bank.getId());
    }

    @Override
    public int hashCode() {
        //If entity has an id -> use id hash.
        //If doesn't have an id yet (transient) -> use identityHashCode to distinguish different instances.
        return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
    }
}