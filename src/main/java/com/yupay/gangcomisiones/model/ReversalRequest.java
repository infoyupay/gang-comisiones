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
 * ReversalRequest entity. When a cashier makes a mistake in a transaction,
 * they request a reversal to a superior (either {@link UserRole#ADMIN} or {@link UserRole#ROOT}).
 * This entity represents such a request.
 * <br/>
 * The entity keeps track of the transaction, the requesting user, the answer provided by
 * the evaluator, and timestamps of creation and evaluation.
 *
 * @author InfoYupay SACS
 * @version 1.1
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reversal_request", schema = "public")
public class ReversalRequest {

    /**
     * Represents the unique identifier of the ReversalRequest entity.
     * This field is the primary key of the entity and is auto-generated using a sequence strategy.
     * It is mapped to the "id" column in the "reversal_request" database table with a "public" schema.
     * The sequence generator is configured with the name "reversal_request_id_gen", using the
     * sequence "sq_reversal_id" and an allocation size of 1. This field cannot be null.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "reversal_request_id_gen")
    @SequenceGenerator(name = "reversal_request_id_gen",
            sequenceName = "sq_reversal_id",
            schema = "public",
            allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Represents the transaction associated with the reversal request.
     * This field establishes a mandatory one-to-one relationship with the {@link Transaction} entity.
     * It is mapped to the "transaction" column in the database and is non-nullable.
     * The relationship is lazily fetched to optimize performance.
     */
    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction", nullable = false)
    private Transaction transaction;

    /**
     * Represents the message or justification related to the reversal request.
     * The cashier requesting the reversal fills this value.
     * <br/>
     * This field stores detailed textual information explaining the reason
     * for the request made by the user. It is mapped to the "message" column
     * in the database as a text field and is non-nullable.
     */
    @Setter
    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    /**
     * Represents the timestamp when the reversal request was made.
     * This field is automatically set to the current timestamp when a new
     * reversal request is created and never should be updated.
     */
    @Column(name = "request_stamp",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime requestStamp;

    /**
     * Represents the user who initiated the reversal request.
     * This field establishes a mandatory many-to-one relationship with the {@link User} entity.
     * It is mapped to the "requested_by" column in the "reversal_request" database table and cannot be null.
     * The relationship is lazily fetched to optimize performance.
     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    /**
     * Represents the response or evaluation regarding the reversal request.
     * This field is used to store textual information explaining the decision
     * made by the evaluator (an administrator or a root) on the corresponding
     * reversal request.
     * It is mapped to the "answer" column in the database as a text type column.
     * The field can be null initially and may be updated after processing the request.
     */
    @Setter
    @Column(name = "answer", columnDefinition = "text")
    private String answer;

    /**
     * Represents the timestamp when the decision regarding the reversal request
     * (either approval or rejection) was made. This field is used to
     * track the exact time the evaluator (e.g., administrator or root user)
     * recorded their response.
     * <br/>
     * It is mapped to the "answer_stamp" column in the database and may be null
     * if no decision has been made yet. Once the response is provided, this
     * timestamp reflects the corresponding date and time.
     */
    @Column(name = "answer_stamp")
    private OffsetDateTime answerStamp;

    /**
     * Represents the current status of the reversal request in the workflow process.
     * This field stores the state of the request, indicating its progression through
     * defined statuses which are defined in the enum ReversalRequestStatus.
     * <br/>
     * It is a non-nullable field, mapped to the "status" column in the database with
     * a custom SQL type definition "reversal_request_status".
     *
     * @see ReversalRequestStatus
     */
    @Setter
    @Column(name = "status", columnDefinition = "reversal_request_status not null")
    @Enumerated(EnumType.STRING)
    private ReversalRequestStatus status;

    /**
     * Represents the evaluator (a user with an administrator or root role) who
     * processed the reversal request and provided an answer (approval or rejection).
     * This field establishes a many-to-one relationship with the {@link User} entity.
     * It is mapped to the "evaluated_by" column in the "reversal_request" database table.
     * The relationship is lazily fetched to optimize performance. This field can be null
     * initially and is set once the evaluation is performed.
     */
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

    @Override
    @Contract(pure = true, value = "null -> false")
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ReversalRequest that)) return false;

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