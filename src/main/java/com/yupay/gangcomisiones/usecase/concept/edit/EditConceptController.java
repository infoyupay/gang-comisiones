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

package com.yupay.gangcomisiones.usecase.concept.edit;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.UserSession;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.ConceptService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import com.yupay.gangcomisiones.usecase.concept.ConceptView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Controller orchestrating the Edit Concept use case.
 * <br/>
 * Responsibilities:
 * <ul>
 *   <li>Validate preconditions (non-null Concept and authenticated user).</li>
 *   <li>Verify {@link UserRole#ADMIN} privileges via {@link PrivilegeChecker}.</li>
 *   <li>Open the edit form in {@link FormMode#EDIT} and persist changes through {@link ConceptService}.</li>
 *   <li>Return a {@link Result} with outcome and edited value.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class EditConceptController {
    private final ConceptView view;
    private final UserService userService;
    private final ConceptService conceptService;
    private final UserSession userSession;
    private static final Logger LOG = LoggerFactory.getLogger(EditConceptController.class);

    /**
     * Creates a controller bound to the UI {@link ConceptView} and services from the {@link AppContext}.
     *
     * @param ctx application context; must not be null.
     */
    public EditConceptController(@NotNull AppContext ctx) {
        this.view = ctx.getViewRegistry().resolve(ConceptView.class);
        this.userService = ctx.getUserService();
        this.conceptService = ctx.getConceptService();
        this.userSession = ctx.getUserSession();
    }

    /**
     * Executes the edit workflow for the provided {@link Concept}.
     *
     * @param concept the concept to edit; must not be {@code null}
     * @return a future with a {@link Result} indicating OK/CANCEL/ERROR and the edited value on success
     */
    public CompletableFuture<Result<Concept>> run(@Nullable Concept concept) {
        try {
            if (concept == null) {
                view.showError("Primero tienes que seleccionar el concepto que quieres editar.");
                return Result.errorCompleted();
            }
            var currentUser = userSession.getCurrentUser();
            if (currentUser == null) {
                view.showError("Inicia sesión con privilegios de ADMIN para poder editar un concepto.");
                return Result.errorCompleted();
            }
            if (!PrivilegeChecker.checkPrivileges(
                    AppContext.getInstance().getEntityManagerFactory(),
                    userService,
                    currentUser,
                    UserRole.ADMIN,
                    view)) {
                LOG.error("User {} does not have ADMIN privileges.", currentUser.getId());
                return Result.errorCompleted();
            }

            var input = view.showUserForm(concept, FormMode.EDIT);
            return input.map(value -> conceptService.updateConcept(value.getId(), value.getName(), value.getType(), value.getValue(), value.getActive())
                            .thenApply(_ -> {
                                LOG.info("Concept updated: {} with id {}", value.getName(), value.getId());
                                view.showSuccess("El concepto %s (código: %d) ha sido actualizado exitosamente."
                                        .formatted(value.getName(), value.getId()));
                                return new Result<>(UseCaseResultType.OK, value);
                            })
                            .exceptionally(e -> {
                                LOG.error("Unable to update concept after sending to service.", e);
                                view.showError("Error al actualizar un Concepto.\n" + e.getMessage());
                                return Result.error();
                            }))
                    .orElseGet(Result::cancelCompleted);
        } catch (RuntimeException e) {
            LOG.error("Unable to prepare update invocation to ConceptService", e);
            view.showError("No pudimos enviar tu solicitud de actualización del concepto al servicio de persistencia.\n"
                    + e.getMessage());
            return Result.errorCompleted();
        }
    }
}
