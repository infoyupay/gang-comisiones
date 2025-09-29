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

package com.yupay.gangcomisiones.usecase.transaction.reversal.request;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.ReversalRequest;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.yupay.gangcomisiones.services.impl.ReversalRequestServiceImpl.Errors.*;

/**
 * Controller for the "Request Reversion" use case. It orchestrates:
 * <ul>
 *     <li>Authentication and privilege validation (CASHIER or higher) against DB via {@code UserService}.</li>
 *     <li>Prompting the user for a textual reason.</li>
 *     <li>Delegation to the {@code ReversalRequestService} to create the request.</li>
 *     <li>Mapping service errors to Spanish messages. Controller does not perform state changes nor audit.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class RequestReversionController {
    private final Logger LOG = LoggerFactory.getLogger(RequestReversionController.class);
    private final AppContext context;
    private final RequestReversionView view;

    /**
     * Constructs the controller resolving its view from the {@link AppContext} view registry.
     *
     * @param context the application context
     */
    public RequestReversionController(@NotNull AppContext context) {
        this.context = context;
        this.view = context.getViewRegistry().resolve(RequestReversionView.class);
    }

    /**
     * Returns true if any of the fragments are contained in the given message (null-safe).
     *
     * @param message   the message to search
     * @param fragments substrings to search for
     * @return true if any fragment is found
     */
    @Contract("null, _ -> false")
    private static boolean containsAny(String message, String... fragments) {
        if (message == null) return false;
        for (var f : fragments) if (message.contains(String.valueOf(f))) return true;
        return false;
    }

    /**
     * Starts the use case flow.
     *
     * @param transactionId the target transaction id
     * @return a future with the {@link Result} wrapping the created {@link ReversalRequest} or the outcome
     */
    public CompletableFuture<Result<ReversalRequest>> startUseCase(long transactionId) {
        try {
            var user = context.getUserSession().getCurrentUser();
            if (user == null) {
                LOG.error("User must be authenticated to request a reversal.");
                view.showError("Para solicitar una reversión debes iniciar sesión primero.");
                return Result.errorCompleted();
            }

            // First privilege check for UX before showing any dialog
            if (!PrivilegeChecker.checkPrivileges(
                    context.getEntityManagerFactory(),
                    context.getUserService(),
                    user,
                    UserRole.CASHIER,
                    view)) {
                // Do not show dialog on privilege failure
                return Result.errorCompleted();
            }

            // Ask user for reason
            var reasonOpt = view.showReasonDialog();
            if (reasonOpt.isEmpty()) {
                view.showInfo("Operación cancelada.");
                return Result.cancelCompleted();
            }
            var sanitizedReason = reasonOpt.map(String::strip).orElse("");
            if (sanitizedReason.isBlank()) {
                view.showInfo("Operación cancelada.");
                return Result.cancelCompleted();
            }

            // Safety: re-check privileges just before delegating to service (authoritative validation is in service)
            if (!PrivilegeChecker.checkPrivileges(
                    context.getEntityManagerFactory(),
                    context.getUserService(),
                    user,
                    UserRole.CASHIER,
                    view)) {
                return Result.errorCompleted();
            }

            // Delegate to service
            long userId = user.getId();
            return context.getReversalRequestService()
                    .createReversalRequest(transactionId, userId, sanitizedReason)
                    .thenApply(req -> {
                        view.showSuccess("Solicitud de reversión registrada. Un administrador revisará su caso.");
                        return new Result<>(UseCaseResultType.OK, req);
                    })
                    .exceptionally(t -> {
                        mapAndShowError(t, transactionId, userId);
                        return Result.error();
                    });

        } catch (RuntimeException e) {
            LOG.error("Request Reversion failed before sending to persistence.", e);
            view.showError("Ocurrió un error al registrar la solicitud. Intente nuevamente.");
            return Result.errorCompleted();
        }
    }

    /**
     * Maps service exceptions into Spanish user-friendly messages and logs the context in English.
     *
     * @param t             the throwable to map
     * @param transactionId the transaction id
     * @param userId        the user id
     */
    private void mapAndShowError(@NotNull Throwable t, long transactionId, long userId) {
        var msg = String.valueOf(t.getMessage());
        LOG.error("Cannot request reversal. txId={}, userId={}", transactionId, userId, t);
        // Service is authoritative, controller performs best-effort mapping by message hints.
        if (containsAny(msg, TRANSACTION_NOT_FOUND.description(), "Transaction not found")) {
            view.showError("No se encontró la transacción.");
        } else if (containsAny(msg, TRANSACTION_NOT_OWNED.description(), "not owned")) {
            view.showError("La transacción no le pertenece.");
        } else if (containsAny(msg, TRANSACTION_STATUS_INVALID.description(), "status")) {
            view.showError("La transacción no está en un estado válido para solicitar reversión.");
        } else if (containsAny(msg, REQUEST_ALREADY_EXISTS.description(), "Ya existe")) {
            view.showError("Ya existe una solicitud de reversión pendiente para esta transacción.");
        } else if (containsAny(msg, "AppSecurityException", "has not privileges", "not active")) {
            view.showError("No tiene permisos para solicitar reversión.");
        } else {
            view.showError("Ocurrió un error al registrar la solicitud. Intente nuevamente.");
        }
    }
}
