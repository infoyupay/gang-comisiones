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
import com.yupay.gangcomisiones.export.ExportableTransaction;
import com.yupay.gangcomisiones.export.OutputType;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.services.dto.CreateTransactionRequest;
import com.yupay.gangcomisiones.usecase.commons.FormMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * The {@code CreateTransactionController} class is responsible for handling the "Create Transaction" use case.
 * This includes managing user interactions, performing business logic, and integrating with the service layer
 * to perform the transaction creation and subsequent operations like exporting tickets.
 * <br/>
 * The controller ensures that proper user context is set, validates input, and handles errors gracefully.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Initialize and manage the view associated with this use case.</li>
 *   <li>Handle user interactions during the transaction creation process (e.g., form submission, cancellation).</li>
 *   <li>Inject user-related metadata (e.g., cashier ID) into the transaction request.</li>
 *   <li>Communicate with the service layer to execute the transaction logic.</li>
 *   <li>Provide feedback to the user on success or failure of the transaction operation.</li>
 *   <li>Generate a ticket preview and facilitate ticket export operations.</li>
 * </ul>
 *
 * <h2>Error Handling:</h2>
 * The controller ensures that any errors encountered during the transaction creation or export process are handled
 * appropriately:
 * <ul>
 *   <li>Logs the error details for further investigation and debugging.</li>
 *   <li>Displays user-friendly error messages in the view.</li>
 *   <li>Returns error states in the {@link ExportableTransaction} to indicate process failures.</li>
 * </ul>
 *
 * <h2>Initialization:</h2>
 * To construct an instance of {@code CreateTransactionController}, the following is required:
 * <ul>
 *   <li>{@link AppContext} to provide access to the shared application context, services, configurations, and view registry.</li>
 *   <li>An initialized {@link CreateTransactionView} that handles user interactions.</li>
 * </ul>
 *
 * <h2>Primary Methods:</h2>
 * <ul>
 *   <li>{@link #startUseCase()} - Orchestrates the complete "Create Transaction" workflow, including form handling,
 *       transaction submission, and ticket export.</li>
 *   <li>{@link #injectCashierId(CreateTransactionRequest, User)} - Injects user metadata (e.g., cashier ID)
 *       into the transaction request.</li>
 *   <li>{@link #sendToService(CreateTransactionRequest)} - Sends the transaction request to the service layer and
 *       manages subsequent steps like ticket export and user feedback.</li>
 * </ul>
 *
 * <h2>Concurrency:</h2>
 * <ul>
 *   <li>The controller utilizes {@link CompletableFuture} to handle asynchronous operations, ensuring non-blocking
 *       execution while interacting with service layers and ticket exporters.</li>
 *   <li>Any exceptions occurring within asynchronous operations are appropriately handled using {@code exceptionally} and
 *       {@code thenCompose} to provide error handling and fallback mechanisms.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
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
     * Initiates the "Create Transaction" use case by interacting with the user to gather necessary input,
     * associating the transaction with the currently logged-in user, and sending the request to the service layer.
     * <br/>
     * This method executes the following steps:
     * <ol>
     *   <li>Checks if there is a logged-in user:
     *       <ul>
     *         <li>If no user is logged in, logs an error message, displays an error to the user,
     *         and returns an error {@link ExportableTransaction}.</li>
     *       </ul>
     *   </li>
     *   <li>Displays a form for user input:
     *       <ul>
     *         <li>If the user submits the form, injects the cashier ID into the transaction request
     *         and sends the request to the service layer.</li>
     *         <li>If the user cancels the form, returns a canceled state {@link ExportableTransaction}.</li>
     *       </ul>
     *   </li>
     *   <li>Handles unexpected runtime exceptions:
     *       <ul>
     *         <li>Logs the error, displays an error message to the user,
     *         and returns an error {@link ExportableTransaction}.</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @return A {@link CompletableFuture} resolving to an {@link ExportableTransaction}:
     * <ul>
     *   <li>{@code ExportableTransaction.ok(transaction, outputType, payload)} - if the transaction is
     *       successfully created and processed.</li>
     *   <li>{@code ExportableTransaction.cancelCompleted()} - if the user cancels the transaction
     *       creation process.</li>
     *   <li>{@code ExportableTransaction.errorCompleted()} - if an error occurs during the process.</li>
     * </ul>
     */
    public CompletableFuture<ExportableTransaction> startUseCase() {
        try {
            var user = context.getUserSession().getCurrentUser();
            if (user == null) {
                LOG.error("A valid user is not logged in.");
                view.showError("Para crear una transacción debes iniciar sesión primero.");
                return ExportableTransaction.errorCompleted();
            }

            return view.showUserForm(FormMode.CREATE)
                    .map(input -> injectCashierId(input, user))
                    .map(this::sendToService)
                    .orElseGet(ExportableTransaction::cancelCompleted);

        } catch (RuntimeException e) {
            LOG.error("Error sending Transaction to service layer.", e);
            view.showError("No pudimos enviar tu solicitud de crear una transacción al servicio de persistencia.");
            return ExportableTransaction.errorCompleted();
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
     * Sends a {@link CreateTransactionRequest} to the transaction service to create a new transaction.
     * Upon successful creation, additional steps such as generating a transaction preview and exporting
     * the transaction are performed based on user interaction.
     * <br/>
     * The process involves the following steps:
     * <ol>
     *   <li>Invoke the transaction service to create a transaction using the provided request.</li>
     *   <li>
     *       If successful:
     *       <ul>
     *           <li>Log the transaction creation and display a success message to the user.</li>
     *           <li>Generate a preview of the transaction ticket in HTML format and display it on the view.</li>
     *           <li>Prompt the user to select the desired export format for the transaction ticket.</li>
     *           <li>Export the ticket in the chosen format and return the transaction as an {@link ExportableTransaction}.</li>
     *       </ul>
     *   </li>
     *   <li>
     *       If an error occurs during ticket export, log the error and return the transaction
     *       without the exportable payload.
     *   </li>
     * </ol>
     * In case of failure at any step of the process (e.g., transaction creation or ticket generation):
     * <ul>
     *   <li>The error is logged.</li>
     *   <li>An error message is displayed to the user.</li>
     *   <li>An error state {@link ExportableTransaction} is returned.</li>
     * </ul>
     * <br/>
     *
     * @param request The {@link CreateTransactionRequest} containing details for the transaction creation.
     *                Must not be {@code null}.
     * @return A {@link CompletableFuture} resolving to an {@link ExportableTransaction}:
     * <ul>
     *   <li>{@code ExportableTransaction.ok(transaction, outputType, payload)} - if the transaction is
     *       successfully created and the ticket is exported in the selected format.</li>
     *   <li>{@code ExportableTransaction.okWithoutPayloadCompleted(transaction)} - if the ticket export
     *       fails or the user does not select an export format.</li>
     *   <li>{@code ExportableTransaction.error()} - if an error occurs during the creation or processing
     *       of the transaction.</li>
     * </ul>
     */
    private @NotNull CompletableFuture<ExportableTransaction> sendToService(CreateTransactionRequest request) {
        return context.getTransactionService()
                .createTransaction(request)
                .thenCompose(transaction -> {
                    LOG.info("Transaction created with id {}.", transaction.getId());
                    view.showSuccess("Se creó la transacción Nro. %d".formatted(transaction.getId()));

                    try {
                        //1. Create HTML preview
                        var cfg = context.getGlobalConfigCache().getOrFetchGlobalConfig();
                        var exporter = new com.yupay.gangcomisiones.export.impl.TicketExporterImpl(context.getTaskExecutor());

                        return exporter.export(transaction, OutputType.PREVIEW_HTML, cfg)
                                .thenCompose(htmlBytes -> {
                                    view.showExportPreview(htmlBytes);

                                    //2. Ask end user the final target
                                    var choice = view.askTicketOutputType();
                                    if (choice != null && choice.isPresent()) {
                                        return exporter.export(transaction, choice.get(), cfg)
                                                .thenApply(bytes ->
                                                        ExportableTransaction.ok(transaction, choice.get(), bytes));
                                    } else {
                                        return ExportableTransaction.okWithoutPayloadCompleted(transaction);
                                    }
                                });

                    } catch (Exception ex) {
                        LOG.error("Error during ticket export for transaction {}.", transaction.getId(), ex);
                        return ExportableTransaction.okWithoutPayloadCompleted(transaction);
                    }
                })
                .exceptionally(e -> {
                    LOG.error("Error creating Transaction for request {}.", request, e);
                    view.showError("Error al momento de registrar la transacción en la base de datos.");
                    return ExportableTransaction.error();
                });
    }


}
