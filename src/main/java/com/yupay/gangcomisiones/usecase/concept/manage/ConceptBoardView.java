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

package com.yupay.gangcomisiones.usecase.concept.manage;

import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.usecase.commons.BoardView;
import com.yupay.gangcomisiones.usecase.commons.SecondaryView;
import com.yupay.gangcomisiones.usecase.concept.ConceptView;
import org.jetbrains.annotations.NotNull;

/**
 * Board view specialized for {@link Concept} entities. It combines list presentation, message reporting,
 * and lifecycle hooks of a secondary view while exposing a dedicated {@link ConceptView} for form interactions.
 * <br/>
 * This mirrors the contract provided by {@link com.yupay.gangcomisiones.usecase.bank.manage.BankBoardView}
 * but targets the Concept domain.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ConceptBoardView extends BoardView<Concept>, SecondaryView {
    /**
     * Returns the {@link ConceptView} to be used for create/edit dialogs.
     *
     * @return a non-null {@link ConceptView} instance bound to this board.
     */
    @NotNull ConceptView getConceptView();
}
