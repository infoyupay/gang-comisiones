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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Concept entity class.
 * <br/>
 * The concept entity represents a financial concept for which a payment is received.
 * For instance, if a customer pays a telephone bill, the concept would be "Telephone Bill",
 * which is associated by each bank to a comission earned by the company (the software owner).
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "concept", schema = "public")
public class Concept {
    /**
     * Represents the identifier of the Concept entity.
     * This field is annotated as the primary key of the entity and is
     * auto-generated using a sequence strategy. The sequence generator
     * is defined with the name "concept_id_gen", which uses the sequence
     * "sq_concept_id" with an allocation size of 1. The corresponding
     * database column is named "id" and does not allow null values.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "concept_id_gen")
    @SequenceGenerator(name = "concept_id_gen",
            sequenceName = "sq_concept_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Represents the name of the Concept entity.
     * This field is mapped to the "name" column in the "concept" database table
     * and is a required field (cannot be null).
     */
    @Column(name = "name", nullable = false)
    @Setter
    private String name;

    /**
     * Represents the type of the concept value, which can be either {@link ConceptType#FIXED} or
     * {@link ConceptType#RATE}.
     * <br/>
     * This field is mapped to the "type" column in the "concept" database table.
     * The column is defined as "concept_type" which is an enum, and does not allow null values.
     */
    @Column(name = "type", columnDefinition = "concept_type not null")
    @Enumerated(EnumType.STRING)
    @Setter
    private ConceptType type;

    /**
     * Represents the value associated with the Concept entity. If the concept type is FIXED,
     * then this value is the fixed amount. If the concept type is RATE, then this value is
     * the rate percentage to be applied to the paid amount in order to compute the Comission value.
     * <br/>
     * This value is stored as a BigDecimal and is mapped to the "value" column
     * in the "concept" database table. The column has a precision of 6 digits,
     * with 4 digits reserved for the fractional part. This field is required
     * and cannot be null.
     */
    @Setter
    @Column(name = "value", nullable = false, precision = 6, scale = 4)
    private BigDecimal value;

    /**
     * Represents the value associated with the Concept entity.<br/>
     * If the concept type is {@link ConceptType#FIXED}, this value represents a fixed monetary amount.<br/>
     * If the concept type is {@link ConceptType#RATE}, this value represents a percentage (rate) to be applied
     * to the paid amount in order to compute the commission value.<br/>
     * <br/>
     * This field is mapped to the "value" column in the "concept" database table and is stored as a BigDecimal
     * with a precision of 6 digits and 4 digits reserved for the fractional part.<br/>
     * <br/>
     * Additionally, the database enforces a {@code CHECK} constraint on this column to validate that the
     * percentage value does not exceed the allowed maximum when {@code type = RATE}. This ensures consistency
     * and prevents invalid values from being persisted directly at the database level.<br/>
     * <br/>
     * This field is required and cannot be null.
     */
    @Column(name = "active", nullable = false)
    @Setter
    private Boolean active = false;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Concept concept)) return false;

        //If one of the two objects isn't persisted yet (id==null), they aren't equals.
        if (getId() == null || concept.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), concept.getId());
    }

    @Override
    public int hashCode() {
        //If entity has an id -> use id hash.
        //If doesn't have an id yet (transient) -> use identityHashCode to distinguish different instances.
        return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
    }
}