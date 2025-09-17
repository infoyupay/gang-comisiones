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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.ReversalRequest;
import com.yupay.gangcomisiones.model.ReversalRequestStatus;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.ReversalRequestService;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.yupay.gangcomisiones.services.impl.ReversalRequestServiceImpl.Errors.RESOLUTION_NOT_DEFINED;
import static com.yupay.gangcomisiones.services.impl.ReversalRequestServiceImpl.Errors.UPDATED_MANY;

/**
 * Controller for the "Review Reversal Requests" use case.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Validate ADMIN+ privileges in DB via UserService (no privilege caching).</li>
 *     <li>Load initial PENDING requests into a board view.</li>
 *     <li>
 *         When user chooses to resolve an item, refresh it from DB, validate input,
 *         delegate to service, and post-fetch to update UI.
 *     </li>
 *     <li>Map exceptions to friendly messages. Do not change domain state nor audit directly.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class ReviewReversionController {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewReversionController.class);

    private final AppContext context;
    private final ReviewReversionView view;

    /**
     * Builds the controller resolving its view from the {@link AppContext} registry.
     *
     * @param context the application context, must not be null
     */
    public ReviewReversionController(@NotNull AppContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.view = context.getViewRegistry().resolve(ReviewReversionView.class);
    }

    /**
     * Determines if the given string contains any of the provided fragments.
     * <br/>
     * This method checks if the input string contains one or more of the specified fragments.
     * If the input string is null, the method immediately returns {@code false}.
     * <br/>
     * The fragments are converted to their string representation before performing the containment check.
     * <br/>
     *
     * <ol>
     * <li>If {@code message} is {@code null}, it immediately returns {@code false}.</li>
     * <li>Iterates over each fragment in {@code fragments}.</li>
     * <li>Returns {@code true} if any fragment is found within {@code message}.</li>
     * <li>Returns {@code false} if no fragments match.</li>
     * </ol>
     *
     * @param message  the string to search for fragments within; may be {@code null}
     * @param fragments an array of fragments to be checked for containment in the {@code message};
     *                  each fragment is converted to its {@code String} representation
     *
     * @return {@code true} if {@code message} contains any of the specified {@code fragments};
     *         {@code false} otherwise, or if {@code message} is {@code null}
     */
    @Contract("null, _ -> false")
    private static boolean containsAny(String message, String... fragments) {
        if (message == null) return false;
        for (var f : fragments) if (message.contains(String.valueOf(f))) return true;
        return false;
    }

    /**
     * Initiates the use case: checks session and ADMIN privileges, wires callbacks and shows the board.
     * On privilege failures the board is not shown and, if already open, it is closed.
     */
    public void startUseCase() {
        try {
            var user = context.getUserSession().getCurrentUser();
            if (user == null) {
                view.showError("Debes iniciar sesión para revisar solicitudes de reversión.");
                LOG.error("Cannot start ReviewReversion: no active user session.");
                return;
            }
            if (!PrivilegeChecker.checkPrivileges(
                    context.getEntityManagerFactory(),
                    context.getUserService(),
                    user,
                    UserRole.ADMIN,
                    view)) {
                // Close the board in case the UI was opened somehow
                view.closeView();
                return;
            }

            // Wire callbacks and show
            view.propagatePrivileges(user);
            view.setOnRefreshIntent(this::loadInitialPending);
            view.setOnUpdateIntent(this::resolveSelected);
            view.setOnInsertIntent(() -> view.showWarning("No hay acciones de inserción en esta bandeja."));
            view.showView();

            // Load initial data
            loadInitialPending();
        } catch (RuntimeException e) {
            LOG.error("Unexpected failure starting ReviewReversion use case.", e);
            view.showError("Ocurrió un error al abrir la Bandeja de Reversiones.");
            try {
                view.closeView();
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * Loads the initial list of requests (defaults to PENDING) using the view's criteria.
     */
    public void loadInitialPending() {
        ReversalRequestService.SearchCriteria criteria = Objects.requireNonNull(view.initialCriteria());
        context.getReversalRequestService()
                .listRequestsBy(criteria)
                .whenComplete((list, error) -> {
                    if (error != null) {
                        LOG.error("Unable to load reversal requests.", error);
                        view.showError("No pudimos cargar la bandeja de reversiones.");
                        return;
                    }
                    view.showList(list);
                });
    }

    /**
     * Resolves the selected request by refreshing it, asking for resolution input and delegating to the service.
     * This method can be used directly in tests.
     *
     * @param selected the item selected by the user from the board
     * @return a future that completes with the refreshed and updated entity, or empty if user cancelled
     */
    public CompletableFuture<Result<ReversalRequest>> resolveSelected(@Nullable ReversalRequest selected) {
        try {
            //The lightest of all checks first to save efforts if selection is null.
            if (selected == null) {
                view.showWarning("Selecciona una solicitud para continuar.");
                return Result.cancelCompleted();
            }
            var user = context.getUserSession().getCurrentUser();
            if (user == null) {
                view.showError("Debes iniciar sesión para resolver solicitudes.");
                return Result.errorCompleted();
            }
            // Soft pre-check for UX
            if (!PrivilegeChecker.checkPrivileges(
                    context.getEntityManagerFactory(), context.getUserService(), user, UserRole.ADMIN, view)) {
                view.closeView();
                return Result.errorCompleted();
            }

            long requestId = selected.getId();
            // Refresh entity before opening form
            return context.getReversalRequestService()
                    .findRequestById(requestId)
                    .thenCompose(opt -> switch (opt) {
                        case Optional<ReversalRequest> o when o.isEmpty() -> {
                            view.showError("La solicitud ya no existe.");
                            loadInitialPending();
                            yield Result.errorCompleted();
                        }
                        case Optional<ReversalRequest> nonEmpty -> {
                            var refreshed = nonEmpty.get();
                            if (refreshed.getStatus() != ReversalRequestStatus.PENDING) {
                                view.showError("La solicitud ya no está pendiente.");
                                loadInitialPending();
                                yield Result.errorCompleted();
                            }

                            var dialogResult = view.showResolveDialog(refreshed);
                            if (dialogResult.isEmpty()) {
                                view.showSuccess("Operación cancelada.");
                                yield Result.cancelCompleted();
                            }

                            var input = dialogResult.get();
                            var justification = Objects.toString(input.justification(), "").strip();
                            if (justification.isBlank() || justification.length() < 10) {
                                view.showSuccess("Operación cancelada.");
                                yield Result.cancelCompleted();
                            }

                            // Safety re-check right before delegating
                            if (!PrivilegeChecker.checkPrivileges(
                                    context.getEntityManagerFactory(), context.getUserService(), user, UserRole.ADMIN, view)) {
                                view.closeView();
                                yield Result.errorCompleted();
                            }

                            var serviceResolution = switch (input.resolution()) {
                                case APPROVED -> ReversalRequestService.Resolution.APPROVED;
                                case REJECTED -> ReversalRequestService.Resolution.DENIED;
                            };

                            yield context.getReversalRequestService()
                                    .resolveRequest(requestId, user.getId(), justification, serviceResolution)
                                    .thenCompose(_ -> context.getReversalRequestService().findRequestById(requestId))
                                    .handle((updatedOpt, thr) -> {
                                        if (thr != null) {
                                            mapAndShowError(thr, requestId, user.getId());
                                            loadInitialPending();
                                            return Result.error();
                                        }
                                        updatedOpt.ifPresent(view::replace);
                                        view.showSuccess("La solicitud de reversión ha sido resuelta correctamente.");
                                        return updatedOpt
                                                .map(val -> new Result<>(UseCaseResultType.OK, val))
                                                .orElseGet(Result::cancel);
                                    });
                        }
                    });

        } catch (RuntimeException e) {
            LOG.error("Unexpected failure while resolving request.", e);
            view.showError("No pudimos resolver la solicitud por un error inesperado.");
            return Result.errorCompleted();
        }
    }

    /**
     * Maps service exceptions to user-friendly Spanish messages and logs technical details.
     *
     * @param t         the throwable to map
     * @param requestId the request id
     * @param userId    the acting user id
     */
    private void mapAndShowError(@NotNull Throwable t, long requestId, long userId) {
        var msg = String.valueOf(t.getMessage());
        LOG.error("Cannot resolve reversal request. reqId={}, userId={}", requestId, userId, t);
        if (containsAny(msg, UPDATED_MANY.description(), "Modified entities count")) {
            view.showError("La solicitud sufrió cambios concurrentes y no pudo resolverse.");
        } else if (containsAny(msg, RESOLUTION_NOT_DEFINED.description(), "resolution")) {
            view.showError("Debes seleccionar un resultado válido.");
        } else if (containsAny(msg, "AppSecurityException", "has not privileges", "not active")) {
            view.showError("No tiene permisos para resolver solicitudes de reversión.");
            try {
                view.closeView();
            } catch (RuntimeException ignored) {
            }
        } else {
            view.showError("No pudimos resolver la solicitud. Intente nuevamente.");
        }
    }

    /**
     * Gets the application context.
     *
     * @return the app context
     */
    @Contract(pure = true)
    public @NotNull AppContext getContext() {
        return context;
    }
}