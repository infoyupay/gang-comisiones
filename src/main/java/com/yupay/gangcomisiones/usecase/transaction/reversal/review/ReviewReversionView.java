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

package com.yupay.gangcomisiones.usecase.transaction.reversal.review;

import com.yupay.gangcomisiones.model.ReversalRequest;
import com.yupay.gangcomisiones.model.ReversalRequestStatus;
import com.yupay.gangcomisiones.usecase.commons.BoardView;
import com.yupay.gangcomisiones.usecase.commons.SecondaryView;
import com.yupay.gangcomisiones.services.ReversalRequestService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Board view contract for the "Review Reversion Requests" use case.
 * The board lists {@link ReversalRequest} items and provides a dialog to resolve a selected one
 * with a resolution and a textual justification. It also acts as a secondary window that can be
 * shown or closed by the controller.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface ReviewReversionView extends BoardView<ReversalRequest>, SecondaryView {
    /**
     * Shows a dialog to resolve a given {@link ReversalRequest}. The dialog must allow selecting a
     * resolution (APPROVED/REJECTED) and entering a justification message.
     *
     * @param request the refreshed request to resolve
     * @return an Optional with the user input when accepted, or empty when cancelled
     */
    Optional<ResolutionInput> showResolveDialog(@NotNull ReversalRequest request);

    /**
     * Returns the initial search criteria to load elements into the board. Defaults to only PENDING.
     * Implementations can override to provide richer filtering.
     *
     * @return the initial criteria for loading items
     */
    @Contract(pure = true)
    default @NotNull ReversalRequestService.SearchCriteria initialCriteria() {
        return ReversalRequestService.SearchCriteria.builder()
                .status(ReversalRequestStatus.PENDING)
                .build();
    }

    /**
     * Small input record representing the user selection for resolving a request.
     *
     * @param resolution APPROVED to accept, REJECTED to deny the request
     * @param justification a non-blank textual justification for the resolution
     */
    record ResolutionInput(@NotNull Resolution resolution, @NotNull String justification) {
        /** Resolution options as presented to the user. */
        public enum Resolution { APPROVED, REJECTED }
    }
}