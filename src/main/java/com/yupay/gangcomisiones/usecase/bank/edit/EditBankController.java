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

package com.yupay.gangcomisiones.usecase.bank.edit;

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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates the Edit Bank use case, coordinating validation, privilege checks, user interaction,
 * and persistence calls.
 * <br/>
 * Responsibilities include:
 * <ul>
 *   <li>Validating preconditions (non-null {@link Bank} and authenticated user)</li>
 *   <li>Verifying that the current user has at least {@link UserRole#ADMIN} privileges via {@link PrivilegeChecker}</li>
 *   <li>Prompting the UI through {@link BankView} to edit the entity in {@link FormMode#EDIT}</li>
 *   <li>Persisting changes using {@link BankService}</li>
 *   <li>Returning a {@link Result} wrapped in a {@link CompletableFuture} that reflects the final outcome</li>
 * </ul>
 * Typical execution flow:
 * <ol>
 *   <li>Reject when the input {@code bank} is {@code null}</li>
 *   <li>Reject when there is no authenticated user</li>
 *   <li>Reject when the user lacks {@link UserRole#ADMIN} privileges</li>
 *   <li>Open the edit form and, if confirmed, persist the update</li>
 * </ol>
 *
 * @version 1.0
 * @author InfoYupay SACS
 */
public class EditBankController {
    private final BankView view;
    private final UserService userService;
    private final BankService bankService;
    private final UserSession userSession;
    private final Logger LOG = LoggerFactory.getLogger(EditBankController.class);

    /**
     * Creates a controller bound to a UI {@link BankView} and an application {@link AppContext}.<br/>
     * The context supplies required services such as {@link UserService}, {@link BankService}, and the {@link UserSession}.
     *
     * @param view the UI view used to interact with the user; must not be {@code null}
     * @param ctx  the application context providing services and session; must be {@link NotNull}
     */
    public EditBankController(BankView view, @NotNull AppContext ctx) {
        this.view = view;
        this.userService = ctx.getUserService();
        this.bankService = ctx.getBankService();
        this.userSession = ctx.getUserSession();
    }

    /**
     * Executes the edit workflow for the provided {@link Bank}.<br/>
     * This method validates input, checks privileges, opens an edit form, and upon confirmation,
     * persists the changes asynchronously.
     * <br/>
     * Behavior summary:
     * <ul>
     *   <li>If {@code bank} is {@code null}, an error is shown and the returned future completes with {@link Result#errorCompleted()}.</li>
     *   <li>If there is no current user, an error is shown and the returned future completes with {@link Result#errorCompleted()}.</li>
     *   <li>If the user lacks {@link UserRole#ADMIN} privileges, an error is shown and the returned future completes with {@link Result#errorCompleted()}.</li>
     *   <li>If the user cancels the form, the returned future completes with {@link Result#cancelCompleted()}.</li>
     *   <li>If the update succeeds, the future completes with {@link UseCaseResultType#OK} and the edited {@link Bank}.</li>
     *   <li>If persistence fails, an error is shown and the future completes with {@link Result#error()}.</li>
     * </ul>
     *
     * @param bank the bank to edit; must not be {@code null}
     * @return a {@link CompletableFuture} completing with a {@link Result} that reflects one of:
     * <ul>
     *   <li>{@link UseCaseResultType#OK} when the update is successful</li>
     *   <li>Cancellation when the user aborts the form</li>
     *   <li>Error when preconditions or persistence fail</li>
     * </ul>
     * @implNote This method delegates UI interactions to {@link BankView} and persistence to {@link BankService}.
     * It relies on {@link PrivilegeChecker} to validate {@link UserRole#ADMIN} privileges.
     */
    public CompletableFuture<Result<Bank>> run(@Nullable Bank bank){
        try{
            //1. Bank cannot be null
            if (bank==null) {
                view.showError("Primero tienes que seleccionar el banco que quieres editar.");
                return Result.errorCompleted();
            }
            //2. User cannot be null
            var currentUser = userSession.getCurrentUser();
            if (currentUser==null) {
                view.showError("Inicia sesión con privilegios de ADMIN para poder editar un banco.");
                return Result.errorCompleted();
            }
            //3. User MUST be ADMIN at least.
            if (!PrivilegeChecker.checkPrivileges(
                    AppContext.getInstance().getEntityManagerFactory(),
                    userService,
                    currentUser,
                    UserRole.ADMIN,
                    view)) {
                LOG.error("User {} does not have ADMIN privileges.", currentUser.getId());
                return Result.errorCompleted();
            }
            //4. ask user
            var input = view.showUserForm(bank, FormMode.EDIT);
            return input.map(value -> bankService.updateBank(bank.getId(), bank.getName(), bank.getActive())
                            .thenApply(_ -> {
                                LOG.info("Bank updated: {} with id {}", bank.getName(), bank.getId());
                                view.showSuccess("El banco %s (código: %d) ha sido actualizado exitosamente."
                                        .formatted(value.getName(), value.getId()));
                                return new Result<>(UseCaseResultType.OK, bank);
                            })
                            .exceptionally(e -> {
                                LOG.error("Unable to update bank after sending to service.", e);
                                view.showError("Error al actualizar un Banco.\n" + e.getMessage());
                                return Result.error();
                            }))
                    .orElseGet(Result::cancelCompleted);
        }catch (RuntimeException e){
            LOG.error("Unable to prepare update invocation to BankService", e);
            view.showError("No pudimos enviar tu solicitud de actualización del banco al servicio de persistencia.\n"
                    + e.getMessage());
            return Result.errorCompleted();
        }
    }

}
