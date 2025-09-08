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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Services for {@link Concept} entities.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ConceptService {
    /**
     * Lists all concepts persisted in the database.
     *
     * @return a future that completes with the list of all concepts.
     */
    CompletableFuture<List<Concept>> listAllConcepts();

    /**
     * Lists all concepts with active flag set to true.
     *
     * @return a future that completes with the list of all active concepts.
     */
    CompletableFuture<List<Concept>> listAllActiveConcepts();

    /**
     * Creates a new concept with the provided data. The concept will be created with the active flag set to true.
     * The database will assign the identifier using its sequence.
     *
     * @param name  the concept name to assign.
     * @param type  the concept type (FIXED or RATE).
     * @param value the numeric value associated with the concept.
     * @return a future that completes with the persisted {@link Concept} entity (with id populated).
     */
    CompletableFuture<Concept> createConcept(@NotNull String name,
                                             @NotNull ConceptType type,
                                             @NotNull BigDecimal value);

    /**
     * Updates the basic information of an existing concept.
     * It uses {@code EntityManager.getReference} to obtain a managed instance and sets the new values.
     * Changes are flushed on transaction commit.
     *
     * @param id     the identifier of the concept to update.
     * @param name   the new name to set (must be non-null).
     * @param type   the new type to set (must be non-null).
     * @param value  the new value to set (must be non-null).
     * @param active the new active flag to set (must be non-null).
     * @return a future that completes when the update has been committed.
     */
    CompletableFuture<Void> updateConcept(long id,
                                          @NotNull String name,
                                          @NotNull ConceptType type,
                                          @NotNull BigDecimal value,
                                          @NotNull Boolean active);
}
