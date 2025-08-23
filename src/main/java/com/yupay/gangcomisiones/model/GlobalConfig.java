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
@Table(name = "global_config")
public class GlobalConfig {
    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Lob
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "announcement", length = 80)
    private String announcement;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(name = "updated_from", nullable = false, length = 100)
    private String updatedFrom;

}