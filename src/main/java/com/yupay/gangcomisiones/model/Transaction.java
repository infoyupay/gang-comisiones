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
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents a financial transaction within the system.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction", schema = "public")
public class Transaction {
    /**
     * Represents the unique identifier of the Transaction entity.
     * <br/>
     * This field is annotated as the primary key of the entity and is
     * auto-generated using a sequence strategy. The sequence generator
     * is defined with the name "transaction_id_gen", which uses the sequence
     * "sq_transaction_id" with an allocation size of 1. The corresponding
     * database column is named "id" and does not allow null values.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "transaction_id_gen")
    @SequenceGenerator(name = "transaction_id_gen",
            sequenceName = "sq_transaction_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Represents the bank associated with the transaction.
     * The relationship is a mandatory many-to-one association where each transaction must be linked
     * to an existing bank. It is lazily loaded, meaning the associated bank data will only be fetched
     * when explicitly accessed.
     * <br/>
     * This field is mapped to the "bank" column in the database
     * and does not allow null values.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank", nullable = false)
    @Setter
    private Bank bank;

    /**
     * Represents the concept associated with the transaction.
     * The relationship is a mandatory many-to-one association where each transaction
     * must be linked to a specific concept. It is lazily loaded, meaning the associated
     * concept data will only be fetched when explicitly accessed.
     * This field is mapped to the "concept" column in the database
     * and does not allow null values.
     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept", nullable = false)
    private Concept concept;

    /**
     * Represents the cashier responsible for processing the transaction.
     * This field establishes a mandatory many-to-one association between transactions
     * and users, where each transaction is linked to exactly one cashier.
     * The association is configured to load lazily, meaning the cashier details
     * are only retrieved from the database when explicitly accessed.
     * Mapped to the "cashier" column in the database, it does not allow null values.
     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cashier", nullable = false)
    private User cashier;

    /**
     * Represents the monetary amount associated with the transaction.
     * This field is mandatory and must be specified for each transaction.
     * <br/>
     * It is mapped to the "amount" column in the database with a precision of 14 and a scale of 2, which
     * is standard for currency quantities.
     */
    @Setter
    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    /**
     * Represents the commission value associated with the transaction.
     * This field stores the monetary commission for the transaction as a non-nullable decimal value.
     * It is mapped to the "commission" column in the database with a precision of 14 and a scale of 2, which
     * is standard for currency quantities.
     */
    @Setter
    @Column(name = "commission", nullable = false, precision = 14, scale = 2)
    private BigDecimal commission;

    /**
     * Timestamp of when the transaction was created.
     * <br/>
     * Managed automatically by the database:
     * <ul>
     *   <li>Set to the current timestamp on insert.</li>
     *   <li>Read-only from the application side; never updated manually.</li>
     * </ul>
     * Stored in the "moment" column of the database table.
     */
    @Column(name = "moment",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime moment;

    /**
     * Represents the current status of the transaction.
     * The status is an enumerated value from the {@link TransactionStatus} enum.
     * It indicates whether the transaction is registered, requested for reversion, or reversed.
     * <br>
     * This field is non-null and stored in the database as a string with the column
     * definition specified as "transaction_status not null".
     */
    @Setter
    @Column(name = "status", columnDefinition = "transaction_status not null")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Transaction that)) return false;

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