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

package com.yupay.gangcomisiones.usecase.bank.manage;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.exceptions.BankUseCasesException;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.usecase.bank.create.CreateBankController;
import com.yupay.gangcomisiones.usecase.bank.edit.EditBankController;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.SuccessProcessor;
import com.yupay.gangcomisiones.usecase.registry.UseCaseControllerRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Controller class responsible for managing operations related to the "Manage Bank" use case.
 * This class coordinates user interactions, manages business logic, and communicates with
 * the view layer to render outputs and handle inputs.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public final class ManageBankController {
    private static final Logger LOG = LoggerFactory.getLogger(ManageBankController.class);
    private final Supplier<AppContext> context;
    private final UseCaseControllerRegistry controllersRegistry;
    private final BankBoardView view;

    /**
     * Constructs a new ManageBankController instance.
     * Initializes the controller with the required application context supplier
     * and the registry of use case controllers.
     *
     * @param context             a supplier providing the current application context. Must not be null.
     * @param controllersRegistry the registry containing use case controllers. Must not be null.
     * @throws NullPointerException if either {@code context} or {@code controllersRegistry} is null.
     */
    public ManageBankController(@NotNull Supplier<AppContext> context,
                                @NotNull UseCaseControllerRegistry controllersRegistry) {
        this.context = Objects
                .requireNonNull(context, "Context supplier must not be null.");
        this.controllersRegistry = Objects
                .requireNonNull(controllersRegistry, "Controllers registry must not be null.");
        this.view = getContext().getViewRegistry().resolve(BankBoardView.class);
    }

    /// Initiates the "Manage Bank" use case.
    /// This method performs the setup required to start the use case, including
    /// user validation, privilege propagation, and displaying the associated view.
    /// If an error occurs during the initialization process, it logs the error,
    /// shows an appropriate error message to the user, and closes the view.
    /// The execution flow is as follows:
    /// 1. Attempts to retrieve the currently active user by invoking `getActiveUserOrShowError`.
    ///    - If no active user is found, the method terminates early.
    /// 2. Propagates privileges of the active user to the view layer by calling `propagatePrivileges`.
    /// 3. Loads and displays the secondary view by calling `view.showView`.
    /// 4. Retrieves and displays a list of banks using `listBank`.
    /// Error Handling:
    /// - If any runtime exception occurs during the execution, it logs the error,
    ///   displays an error message to the user, and closes the view to ensure proper cleanup.
    public void startUseCase() {
        try {
            var user = getActiveUserOrShowError(getContext());
            if (user == null) return;

            propagatePrivileges(user);

            view.setOnInsertIntent(this::createBank);
            view.setOnRefreshIntent(this::listBank);
            view.setOnUpdateIntent(this::editBank);
            view.showView();
            listBank();
        } catch (RuntimeException e) {
            LOG.error("Unable to start ManageBank use case.", e);
            view.showError("No pudimos iniciar la administración de bancos debido a un error inesperado.");
            view.closeView();
        }
    }

    /**
     * Retrieves the currently active user associated with the given application context.
     * If no active user is found or the user is no longer active, an error message is
     * displayed, and appropriate actions are taken. This method can return null if the
     * user session is inactive or throw an exception if the user is no longer active.
     *
     * @param ctx the application context used to retrieve the user session and user service.
     *            Must not be null.
     * @return the active user if present and still active; otherwise, returns null
     * or throws an exception if the user is no longer active.
     */
    private @Nullable User getActiveUserOrShowError(@NotNull AppContext ctx) {
        var user = ctx.getUserSession().getCurrentUser();
        if (user == null) {
            view.showError("No puedes iniciar la Administración de Bancos sin haber iniciado sesión primero.");
            LOG.error("There wasn't an active user session when starting Manage Bank use case.");
            return null;
        }

        if (!PrivilegeChecker.isUserStillActive(ctx.getUserService(), user, view)) {
            view.showError("El usuario ya no está activo.");
            throw new BankUseCasesException("User " + user.getId() + " is no longer active.");
        }

        return user;
    }

    /**
     * Propagates user privileges to the view layer.
     *
     * @param user the User whose privileges are to be propagated. Must not be null.
     */
    private void propagatePrivileges(User user) {
        view.propagatePrivileges(user);
    }

    /// Initiates the "Create Bank" use case.
    /// This method triggers an asynchronous flow to create a new bank entity in the system.
    /// It uses the [CreateBankController] to manage the creation process, which involves
    /// validating user privileges, capturing user input, and persisting the new bank data
    /// through the application's services.
    /// The method processes the outcome of the creation flow:
    /// - On successful creation, updates the view with the success details.
    /// - On cancellation or error, delegates appropriate handling to the view.
    /// The invocation is non-blocking and operates asynchronously.
    public void createBank() {
        controllersRegistry
                .resolve(CreateBankController.class)
                .run()
                .thenAcceptAsync(SuccessProcessor.insert(view));
    }

    /**
     * Edits the details of an existing bank entity.
     * This method initializes an edit flow by invoking the EditBankController
     * and processes the provided bank entity asynchronously.
     *
     * @param bank the Bank entity to be edited. Must not be null.
     */
    public void editBank(Bank bank) {
        controllersRegistry
                .resolve(EditBankController.class)
                .run(bank)
                .thenAcceptAsync(SuccessProcessor.replace(view));
    }

    /// Loads and displays the list of banks from the system's bank service.
    /// This method retrieves all bank entities by invoking the `listAllBanks` method
    /// from the `BankService`, which returns a CompletableFuture. The result is
    /// processed to either display the retrieved list of banks or clear the view if the list
    /// is empty. If an error occurs during the retrieval, an error message is logged, and
    /// the view displays an appropriate error notification.
    /// The behavior is asynchronous and non-blocking:
    /// - On success, if a list of banks is returned, it is passed to the view for display.
    /// - If no banks are found, the view's list is cleared.
    /// - On failure, an error message is logged and presented to the user.
    public void listBank() {
        getContext().getBankService()
                .listAllBanks()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Unable to load banks list.", throwable);
                        view.showError("No pudimos cargar el listado de bancos. Por favor, intente nuevamente.");
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
     * Retrieves the current application context required to perform operations in the "Manage Bank" use case.
     * If the context is not available (null), this method throws a {@code BankUseCasesException}.
     *
     * @return a non-null {@code AppContext} representing the current application context.
     * @throws BankUseCasesException if the context is not set or is null.
     */
    private @NotNull AppContext getContext() throws BankUseCasesException {
        var r = context.get();
        if (r == null) {
            throw new BankUseCasesException(
                    "Provided context is null. Manage banks cannot be started while in bootstrap.");
        } else {
            return r;
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ManageBankController) obj;
        return Objects.equals(this.context, that.context) &&
                Objects.equals(this.controllersRegistry, that.controllersRegistry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, controllersRegistry);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "ManageBankController[" +
                "context=" + context + ", " +
                "controllersRegistry=" + controllersRegistry + ']';
    }

}
