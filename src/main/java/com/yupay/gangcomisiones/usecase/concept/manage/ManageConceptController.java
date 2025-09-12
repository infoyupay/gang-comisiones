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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.exceptions.BankUseCasesException;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.bank.manage.ManageBankController;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.SuccessProcessor;
import com.yupay.gangcomisiones.usecase.concept.create.CreateConceptController;
import com.yupay.gangcomisiones.usecase.concept.edit.EditConceptController;
import com.yupay.gangcomisiones.usecase.registry.UseCaseControllerRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Orchestrates the "Manage Concept" use case, coordinating list, create, and edit flows for {@link Concept}
 * entities through the application services and the board view.<br/>
 * Mirrors the navigation and composition approach used by {@link ManageBankController} to keep interaction patterns
 * consistent across use cases.<br/>
 * This controller wires UI intents to their corresponding use case controllers and ensures the view reflects the
 * current state of the data.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class ManageConceptController {
    private static final Logger LOG = LoggerFactory.getLogger(ManageConceptController.class);
    private final Supplier<AppContext> context;
    private final UseCaseControllerRegistry controllersRegistry;
    private final ConceptBoardView view;

    /**
     * Creates a new controller instance, wiring the app context supplier and the controllers registry, and resolving the
     * board view from the context.<br/>
     * The view is resolved immediately during construction to fail fast if the application context is not available.
     *
     * @param context             the application context supplier; must not be {@code null}
     * @param controllersRegistry the registry used to resolve other use case controllers; must not be {@code null}
     * @throws BankUseCasesException if the supplied context returns {@code null} while resolving the view
     */
    public ManageConceptController(@NotNull Supplier<AppContext> context,
                                   @NotNull UseCaseControllerRegistry controllersRegistry) {
        this.context = Objects.requireNonNull(context, "Context supplier must not be null.");
        this.controllersRegistry = Objects.requireNonNull(controllersRegistry, "Controllers registry must not be null.");
        this.view = getContext().getViewRegistry().resolve(ConceptBoardView.class);
    }

    /**
     * Starts the Manage Concept flow: validates the active session, propagates privileges to the view, wires UI
     * callbacks, shows the view, and loads the initial list of concepts.<br/>
     * Any unexpected runtime error is logged, surfaced to the user, and the view is closed to avoid leaving the UI in
     * an inconsistent state.
     */
    public void startUseCase() {
        try {
            var user = getActiveUserOrShowError(getContext());
            if (user == null) return;

            propagatePrivileges(user);

            view.setOnInsertIntent(this::createConcept);
            view.setOnRefreshIntent(this::listConcept);
            view.setOnUpdateIntent(this::editConcept);
            view.showView();
            listConcept();
        } catch (RuntimeException e) {
            LOG.error("Unable to start ManageConcept use case.", e);
            view.showError("No pudimos iniciar la administración de conceptos debido a un error inesperado.");
            view.closeView();
        }
    }

    /**
     * Ensures there is an active and valid user session before proceeding.<br/>
     * If there is no current user, an error is shown on the view and {@code null} is returned. If the user exists but
     * is no longer active, an error is shown and a {@link BankUseCasesException} is thrown.
     *
     * @param ctx the application context used to resolve session and user services; must not be {@code null}
     * @return the active {@link User} if present and still valid; {@code null} if no user is logged in
     * @throws BankUseCasesException if the user exists but is no longer active
     */
    private @Nullable User getActiveUserOrShowError(@NotNull AppContext ctx) {
        var user = ctx.getUserSession().getCurrentUser();
        if (user == null) {
            view.showError("No puedes iniciar la Administración de Conceptos sin haber iniciado sesión primero.");
            LOG.error("There wasn't an active user session when starting Manage Concept use case.");
            return null;
        }

        if (!PrivilegeChecker.isUserStillActive(ctx.getUserService(), user, view)) {
            view.showError("El usuario ya no está activo.");
            throw new BankUseCasesException("User " + user.getId() + " is no longer active.");
        }

        return user;
    }

    /**
     * Propagates the privileges of the given user to the view so UI actions reflect the user's capabilities.
     *
     * @param user the authenticated user whose privileges will be applied; must not be {@code null}
     */
    private void propagatePrivileges(User user) {
        view.propagatePrivileges(user);
    }

    /**
     * Triggers the "create concept" flow by delegating to {@link CreateConceptController}.<br/>
     * On success, the newly created concept is inserted into the current board view asynchronously.
     *
     */
    public void createConcept() {
        controllersRegistry
                .resolve(CreateConceptController.class)
                .run()
                .thenAcceptAsync(SuccessProcessor.insert(view));
    }

    /**
     * Triggers the "edit concept" flow by delegating to {@link EditConceptController}.<br/>
     * On success, the edited concept replaces the corresponding row in the board view asynchronously.
     *
     * @param concept the concept to edit; must not be {@code null} and should represent an existing record
     */
    public void editConcept(Concept concept) {
        controllersRegistry
                .resolve(EditConceptController.class)
                .run(concept)
                .thenAcceptAsync(SuccessProcessor.replace(view));
    }

    /**
     * Loads all available concepts and updates the view accordingly.<br/>
     * The retrieval is performed asynchronously and the UI is updated on completion.
     * <ul>
     *   <li>On success, the list of concepts is displayed in the view.</li>
     *   <li>On failure, an error is logged and an error message is shown to the user.</li>
     *   <li>If the result is {@code null}, the board is cleared.</li>
     * </ul>
     */
    public void listConcept() {
        getContext().getConceptService()
                .listAllConcepts()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Unable to load concepts list.", throwable);
                        view.showError("No pudimos cargar el listado de conceptos. Por favor, intente nuevamente.");
                        return;
                    }

                    if (result != null) {
                        view.showList(result);
                    } else {
                        view.clearList();
                    }
                });
    }

    /**
     * Returns the current application context provided by the configured supplier.<br/>
     * This method fails fast when the supplier yields {@code null} to prevent continuing with an invalid setup.
     *
     * @return the non-{@code null} {@link AppContext} instance
     * @throws BankUseCasesException if the supplier returns {@code null}
     */
    private @NotNull AppContext getContext() throws BankUseCasesException {
        var r = context.get();
        if (r == null) {
            throw new BankUseCasesException(
                    "Provided context is null. Manage concepts cannot be started while in bootstrap.");
        } else {
            return r;
        }
    }

}
