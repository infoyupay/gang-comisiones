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

package com.yupay.gangcomisiones.usecase.transaction.create;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.SuitableFor;
import com.yupay.gangcomisiones.model.Transaction;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import com.yupay.gangcomisiones.usecase.commons.Result;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * The {@code CreateTransactionController} class serves as the main controller for managing the creation
 * of transactions within the application. It is responsible for orchestrating user interactions,
 * validating inputs, enforcing privilege checks, and delegating the transaction persistence to the
 * appropriate service layer.
 * <p>
 * <br/>
 *
 * <strong>Responsibilities:</strong>
 * <ul>
 *   <li>
 *       Coordinate the user interface logic with the view ({@link CreateTransactionView})
 *       during the transaction creation process.
 *   </li>
 *   <li>
 *       Validate that the user is logged in before initiating any transaction-related actions.
 *   </li>
 *   <li>
 *       Enforce privilege checks to ensure that only authorized users (e.g., those with the {@code CASHIER} role)
 *       can create transactions.
 *   </li>
 *   <li>
 *       Handle transaction form interactions with users to capture input data.
 *   </li>
 *   <li>
 *       Delegate transaction creation to the appropriate transaction service within the application context.
 *   </li>
 *   <li>
 *       Respond to user cancellations, transaction errors, or system failures
 *       appropriately by providing feedback via the view.
 *   </li>
 * </ul>
 * <p>
 * <br/>
 *
 * @author InfoYupay SACS
 * @version 1.0
 * @implNote <ul>
 * <li>
 * The class depends on the {@link AppContext} to provide application-wide services and utilities,
 * including the {@link CreateTransactionView} and transaction service.
 * </li>
 * <li>
 * Uses {@link Logger} to log significant system events, errors, and informational messages.
 * </li>
 * <li>
 * Handles exceptions gracefully at various points in the transaction creation process to ensure a
 * robust user experience.
 * </li>
 * </ul>
 */
public class CreateTransactionController {
    @SuitableFor("stableValue")
    private final Logger LOG = LoggerFactory.getLogger(CreateTransactionController.class);
    private final CreateTransactionView view;
    private final AppContext context;

    /**
     * Constructs a new instance of {@code CreateTransactionController}.
     * This controller is responsible for managing the user interactions and business logic
     * necessary to create a transaction. It ensures proper initialization of the associated view.
     *
     * @param context The application context that provides access to application-wide services,
     *                configurations, and the view registry for resolving views.
     */
    public CreateTransactionController(@NotNull AppContext context) {
        this.context = context;
        this.view = context.getViewRegistry().resolve(CreateTransactionView.class);
    }

    /**
     * Initiates the use case for creating a transaction. This method validates the user session,
     * checks for appropriate user privileges, captures the necessary input for transaction creation,
     * and invokes the transaction service to persist the transaction. It handles various outcomes such
     * as user cancellation or errors during processing.
     * <br/>
     * The method operates through the following steps:
     * <ol>
     *   <li>
     *       Validates whether a user is logged in. If no user is active, logs an error
     *       and shows an error message to the view.
     *   </li>
     *   <li>
     *       Checks if the logged-in user has sufficient privileges (e.g., must be a cashier).
     *       Returns an error result if privileges are insufficient.
     *   </li>
     *   <li>Captures input from the user through a form view to provide data necessary for transaction creation.</li>
     *   <li>Invokes the transaction service with the captured input to create a transaction:
     *     <ul>
     *       <li>
     *           If successful, logs the transaction creation, displays a success message, and returns
     *           the transaction details in the result.
     *       </li>
     *       <li>If an error occurs, logs the failure, displays an error message, and returns an error result.</li>
     *       <li>If the user cancels the operation, returns a cancellation result.</li>
     *     </ul>
     *   </li>
     *   <li>
     *       Handles unexpected runtime exceptions, logging the errors
     *       and showing an appropriate error message to the view.
     *   </li>
     * </ol>
     *
     * @return A {@code CompletableFuture} that resolves to a {@code Result<Transaction>} object:
     * <ul>
     *   <li>{@code Result.ok(transaction)} - if the transaction is successfully created.</li>
     *   <li>{@code Result.cancel()} - if the user cancels the form input.</li>
     *   <li>{@code Result.error()} - if an error occurs at any point during the process.</li>
     * </ul>
     */
    public CompletableFuture<Result<Transaction>> startUseCase() {
        try {
            var user = context.getUserSession().getCurrentUser();
            if (user == null) {
                LOG.error("A valid user is not logged in.");
                view.showError("Para crear una transacción debes iniciar sesión primero.");
                return Result.errorCompleted();
            }

            return view.showUserForm(FormMode.CREATE)
                    .map(input -> injectCashierId(input, user))
                    .map(this::sendToService)
                    .orElseGet(Result::cancelCompleted);

        } catch (RuntimeException e) {
            LOG.error("Error sending Transaction to service layer.", e);
            view.showError("No pudimos enviar tu solicitud de crear una transacción al servicio de persistencia.");
            return Result.errorCompleted();
        }
    }

    /**
     * Injects the cashier ID into the provided {@link CreateTransactionRequest} object by
     * associating it with the identifier of the specified {@link User}.
     * This ensures that the transaction request contains the required reference
     * to the cashier executing the transaction.
     * <br/>
     * The method returns a new {@link CreateTransactionRequest} instance with the modified data.
     *
     * @param input The original {@link CreateTransactionRequest} object that needs to be updated.
     *              Must not be {@code null}.
     * @param user  The {@link User} object representing the cashier performing the transaction.
     *              Must not be {@code null}.
     * @return A new {@link CreateTransactionRequest} instance containing the injected cashier ID.
     */
    private CreateTransactionRequest injectCashierId(@NotNull CreateTransactionRequest input, @NotNull User user) {
        return input.toBuilder()
                .cashierId(user.getId())
                .build();
    }

    /**
     * Sends the provided {@link CreateTransactionRequest} to the transaction service for processing.
     * Upon successful execution, logs the transaction creation and displays a success message via the view.
     * In case of an error, logs the failure and displays an error message.
     * <br/>
     * <br/>
     * The method processes the request as follows:
     * <ol>
     *   <li>The transaction service is invoked to create a transaction using the provided request.</li>
     *   <li>
     *       If the transaction is successfully created:
     *       <ul>
     *           <li>Logs the transaction ID.</li>
     *           <li>Displays a success message through the associated view.</li>
     *           <li>Wraps the transaction in a {@link Result} object and returns it.</li>
     *       </ul>
     *   </li>
     *   <li>
     *       If an error occurs during transaction creation:
     *       <ul>
     *           <li>Logs the error along with the request details.</li>
     *           <li>Displays an error message through the associated view.</li>
     *           <li>Returns a {@link Result#error()} object.</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param request The {@link CreateTransactionRequest} containing the details of the transaction to be created.
     *                Must not be {@code null}.
     * @return A {@link CompletableFuture} that resolves to a {@link Result<Transaction>} object:
     * <ul>
     *   <li>{@code Result.ok(transaction)} - if the transaction is successfully created.</li>
     *   <li>{@code Result.error()} - if an error occurs during transaction creation.</li>
     * </ul>
     */
    private @NotNull CompletableFuture<Result<Transaction>> sendToService(CreateTransactionRequest request) {
        return context.getTransactionService()
                .createTransaction(request)
                .thenApply(transaction -> {
                    LOG.info("Transaction created with id {}.", transaction.getId());
                    view.showSuccess("Se creó la transacción Nro. %d".formatted(transaction.getId()));
                    return new Result<>(UseCaseResultType.OK, transaction);
                })
                .exceptionally(e -> {
                    LOG.error("Error creating Transaction for request {}.", request, e);
                    view.showError("Error al momento de registrar la transacción en la base de datos.");
                    return Result.error();
                });
    }


}
