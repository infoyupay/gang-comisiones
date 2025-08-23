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
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "reversal_request")
public class ReversalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reversal_request_id_gen")
    @SequenceGenerator(name = "reversal_request_id_gen", sequenceName = "sq_reversal_id", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction", nullable = false)
    private Transaction transaction;

    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "request_stamp", nullable = false)
    private OffsetDateTime requestStamp;

    @Lob
    @Column(name = "answer")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(name = "answer_stamp")
    private OffsetDateTime answerStamp;

    @Column(name = "status", columnDefinition = "reversal_request_status not null")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

}