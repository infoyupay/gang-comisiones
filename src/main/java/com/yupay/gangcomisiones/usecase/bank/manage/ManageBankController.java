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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class responsible for managing operations related to the "Manage Bank" use case.
 * This class coordinates user interactions, manages business logic, and communicates with
 * the view layer to render outputs and handle inputs.
 *
 * @param context the {@link AppContext} instance that provides application-level services
 *                and user session management. Must not be null.
 * @param view    the {@link BankBoardView} instance responsible for displaying the user interface
 *                for managing banks. Must not be null.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record ManageBankController(BankBoardView view, AppContext context) {
    private static final Logger LOG = LoggerFactory.getLogger(ManageBankController.class);

    /**
     * Constructs a new instance of the ManageBankController.
     *
     * @param view    the {@link BankBoardView} instance responsible for displaying the user interface
     *                for managing banks. Must not be null.
     * @param context the {@link AppContext} instance that provides application-level services
     *                and user session management. Must not be null.
     */
    @Contract(pure = true)
    public ManageBankController {
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
            var user = getActiveUserOrShowError();
            if (user == null) return;

            propagatePrivileges(user);
            view.showView();
            listBank();
        } catch (RuntimeException e) {
            LOG.error("Unable to start ManageBank use case.", e);
            view.showError("No pudimos iniciar la administración de bancos debido a un error inesperado.");
            view.closeView();
        }
    }

    /// Retrieves the currently active user from the user session associated with the application context.
    /// If no user session is active or the user's account is no longer active, appropriate error handling is triggered:
    /// - Displays an error message to the user when there is no active session.
    /// - Throws a `BankUseCasesException` if the user is inactive.
    ///
    /// @return the currently active `User` if one exists and is active; otherwise, returns `null`.
    ///         If a `BankUseCasesException` is thrown, no value is returned.
    private @Nullable User getActiveUserOrShowError() {
        var user = context.getUserSession().getCurrentUser();
        if (user == null) {
            view.showError("No puedes iniciar la Administración de Bancos sin haber iniciado sesión primero.");
            LOG.error("There wasn't an active user session when starting Manage Bank use case.");
            return null;
        }

        if (!PrivilegeChecker.isUserStillActive(context.getUserService(), user, view)) {
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
        new CreateBankController(context)
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
        new EditBankController(context)
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
        context.getBankService()
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
}
