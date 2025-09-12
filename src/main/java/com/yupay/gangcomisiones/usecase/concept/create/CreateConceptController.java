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

package com.yupay.gangcomisiones.usecase.concept.create;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.UserSession;
import com.yupay.gangcomisiones.model.Concept;
import com.yupay.gangcomisiones.model.ConceptType;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.ConceptService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.bank.create.CreateBankController;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import com.yupay.gangcomisiones.usecase.concept.ConceptView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the "Create Concept" use case.
 * <br/>
 * Mirrors the behavior of {@link CreateBankController}
 * but targets {@link Concept} entities and delegates persistence to {@link ConceptService}.
 * <ul>
 *   <li>Ensures an authenticated {@code ADMIN} user.</li>
 *   <li>Prompts the UI in {@link FormMode#CREATE} to capture a new {@link Concept}.</li>
 *   <li>Persists via {@link ConceptService#createConcept(String, ConceptType, BigDecimal)}.</li>
 *   <li>Maps success to {@link UseCaseResultType#OK}, cancellation to {@link UseCaseResultType#CANCEL},
 *   and failures to {@link UseCaseResultType#ERROR}.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class CreateConceptController {
    private final ConceptView view;
    private final UserService userService;
    private final ConceptService conceptService;
    private final UserSession userSession;
    private static final Logger LOG = LoggerFactory.getLogger(CreateConceptController.class);

    /**
     * Creates a controller wiring UI and services from the application {@link AppContext}.
     *
     * @param ctx the application context; must be non-null and provide required services and view.
     */
    public CreateConceptController(@NotNull AppContext ctx) {
        this.userService = ctx.getUserService();
        this.conceptService = ctx.getConceptService();
        this.userSession = ctx.getUserSession();
        this.view = ctx.getViewRegistry().resolve(ConceptView.class);
    }

    /**
     * Executes the create flow asynchronously.
     *
     * @return a future with a {@link Result} describing the outcome and value (when successful).
     */
    public CompletableFuture<Result<Concept>> run() {
        try {
            var currentUser = userSession.getCurrentUser();
            if (currentUser == null) {
                view.showError("Inicia sesión con privilegios de ADMIN para poder crear un concepto.");
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

            var input = view.showUserForm(FormMode.CREATE);
            return input.map(value -> conceptService.createConcept(value.getName(), value.getType(), value.getValue())
                            .thenApply(concept -> {
                                LOG.info("Concept created: {} with id {}", concept.getName(), concept.getId());
                                view.showSuccess("Concepto %s creado exitosamente con código %d."
                                        .formatted(concept.getName(), concept.getId()));
                                return new Result<>(UseCaseResultType.OK, concept);
                            })
                            .exceptionally(e -> {
                                LOG.error("Unable to create concept after sending to service.", e);
                                view.showError("Error al crear un Concepto.\n" + e.getMessage());
                                return Result.error();
                            }))
                    .orElseGet(Result::cancelCompleted);
        } catch (RuntimeException e) {
            LOG.error("Unable to prepare invocation to ConceptService.", e);
            view.showError("No pudimos enviar tu solicitud de creación del concepto al servicio de persistencia.\n"
                    + e.getMessage());
            return Result.errorCompleted();
        }
    }
}
