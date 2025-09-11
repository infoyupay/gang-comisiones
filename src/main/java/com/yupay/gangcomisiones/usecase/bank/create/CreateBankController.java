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

package com.yupay.gangcomisiones.usecase.bank.create;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.UserSession;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.BankService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.bank.BankView;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the "Create Bank" use case.
 * <br/>
 * This controller coordinates:
 * <ol>
 *   <li>Retrieving the current user from {@link AppContext}'s {@link UserSession}.</li>
 *   <li>
 *       Validating that the user possesses {@link UserRole#ADMIN} privileges
 *   via {@link PrivilegeChecker} and {@link UserService}.
 *   </li>
 *   <li>
 *       Prompting the user for the new bank information
 *   through the {@link BankView} in {@link FormMode#CREATE} mode.
 *   </li>
 *   <li>Delegating the persistence of the new {@link Bank} to {@link BankService}.</li>
 *   <li>
 *       Wrapping the outcome into a {@link Result} delivered
 *   asynchronously in a {@link CompletableFuture}.
 *   </li>
 * </ol>
 * <br/>
 * Result semantics:
 * <ul>
 *   <li>{@code OK}: The bank was created successfully; the {@link Bank} (with id) is returned.</li>
 *   <li>{@code CANCEL}: The user canceled the operation in the view.</li>
 *   <li>{@code ERROR}: The user is not authenticated or lacks privileges, or an unexpected error occurred.</li>
 * </ul>
 * <br/>
 * Threading and errors:
 * <br/>
 * The persistence call is asynchronous. Any runtime failures during the async creation are mapped to {@code ERROR}
 * using {@link CompletableFuture#exceptionally(java.util.function.Function)}. Early validation failures complete
 * immediately.
 * <br/>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class CreateBankController {
    private final BankView view;
    private final UserService userService;
    private final BankService bankService;
    private final UserSession userSession;
    private final Logger LOG = LoggerFactory.getLogger(CreateBankController.class);

    /**
     * Constructs the controller with the view and application context.
     * <br/>
     * The {@link AppContext} provides the required services and the current {@link UserSession}.
     *
     * @param ctx the {@link AppContext} providing {@link UserService}, {@link BankService}, and {@link UserSession}.
     */
    public CreateBankController(@NotNull AppContext ctx) {
        this.userService = ctx.getUserService();
        this.bankService = ctx.getBankService();
        this.userSession = ctx.getUserSession();
        this.view = ctx.getViewRegistry().resolve(BankView.class);
    }

    /**
     * Executes the "Create Bank" flow asynchronously.
     * <br/>
     * Behavior outline:
     * <ol>
     *   <li>Read the current user from the session; if absent, show a message and complete with {@code ERROR}.</li>
     *   <li>Check {@link UserRole#ADMIN} privileges; if missing, complete with {@code ERROR}.</li>
     *   <li>Show the creation form via {@link BankView}; if the user cancels, complete with {@code CANCEL}.</li>
     *   <li>
     *       On user-provided input, call {@link BankService#createBank(String)} and map success to {@code OK}
     *   (with {@link Bank}) or failures to {@code ERROR}.
     *   </li>
     * </ol>
     *
     * @return a {@link CompletableFuture} that completes with a {@link Result}:
     * <ul>
     *   <li>{@code OK} with the created {@link Bank} when persistence succeeds.</li>
     *   <li>{@code CANCEL} when the user aborts at the form.</li>
     *   <li>{@code ERROR} on authentication/authorization failure or unexpected runtime errors.</li>
     * </ul>
     */
    public CompletableFuture<Result<Bank>> run() {
        try {
            var currentUser = userSession.getCurrentUser();
            if (currentUser == null) {
                view.showError("Inicia sesión con privilegios de ADMIN para poder crear un banco.");
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
            return input.map(value -> bankService.createBank(value.getName())
                            .thenApply(bank -> {
                                LOG.info("Bank created: {} with id {}", bank.getName(), bank.getId());
                                view.showSuccess("Banco %s creado exitosamente con código %d."
                                        .formatted(value.getName(), value.getId()));
                                return new Result<>(UseCaseResultType.OK, bank);
                            })
                            .exceptionally(e -> {
                                LOG.error("Unable to create bank after sending to service.", e);
                                view.showError("Error al crear un Banco.\n" + e.getMessage());
                                return Result.error();
                            }))
                    .orElseGet(Result::cancelCompleted);
        } catch (RuntimeException e) {
            LOG.error("Unable to prepare invocation to BankService.", e);
            view.showError("No pudimos enviar tu solicitud de creación del banco al servicio de persistencia.\n"
                    + e.getMessage());
            return Result.errorCompleted();
        }
    }
}
