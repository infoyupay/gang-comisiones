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

package com.yupay.gangcomisiones.usecase.createuser;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.UseCaseResultType;
import com.yupay.gangcomisiones.usecase.WriteSingleResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Controller responsible for managing user creation operations and interaction
 * with the view and service layers. This controller handles the logic necessary
 * to perform user creation in different modes, validates user privileges, and
 * communicates status updates to the view.
 * <br/>
 * This class works in conjunction with `CreateUserView` to display necessary
 * information to the user and `UserService` to execute user creation tasks.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class CreateUserController {
    private final CreateUserView view;
    private final UserService userService;
    private final boolean bootstrapMode;
    private final Logger LOG = LoggerFactory.getLogger(CreateUserController.class);

    /**
     * Constructs a new instance of the CreateUserController.
     *
     * @param view          the view that will handle displaying the user creation interface and interactions.
     * @param userService   the service responsible for handling user-related operations.
     * @param bootstrapMode a boolean indicating if the controller is operating in bootstrap mode,
     *                      which alters the user creation flow or behavior.
     */
    public CreateUserController(CreateUserView view, UserService userService, boolean bootstrapMode) {
        this.view = view;
        this.userService = userService;
        this.bootstrapMode = bootstrapMode;
    }

    /**
     * Checks if the given user has ROOT privileges.
     *
     * @param user the user whose privileges are to be verified
     * @return true if the user has ROOT privileges, false otherwise
     */
    private boolean checkRootPrivileges(User user) {
        return PrivilegeChecker.checkPrivileges(
                AppContext.getInstance().getEntityManagerFactory(),
                userService,
                user,
                UserRole.ROOT,
                view);
    }

    /**
     * Initiates the process for creating a user, operating differently based on whether
     * the system is in bootstrap mode or normal mode. If in bootstrap mode, the method
     * performs a synchronous user creation process required at startup; otherwise,
     * it executes a non-blocking, asynchronous user creation flow.
     * <br/>
     * The method checks whether the current user has sufficient privileges to create
     * a new user unless running in bootstrap mode. It presents a user creation form and
     * processes the user creation request based on the provided data. Any errors encountered
     * during execution are logged and displayed to the user.
     *
     * @return a CompletableFuture containing the result of the user creation process.
     * If successful, the result includes the created user and an OK status.
     * If cancelled, a CANCEL status is returned. If an error occurs,
     * an error result is returned.
     */
    public CompletableFuture<WriteSingleResult<User>> run() {
        try {
            if (!(bootstrapMode ||
                    checkRootPrivileges(AppContext.getInstance().getUserSession().getCurrentUser()))) {
                return WriteSingleResult.errorCompleted();
            }
            var dtoOptional = view.showCreateUserForm(bootstrapMode);
            if (dtoOptional.isEmpty()) {
                return WriteSingleResult.completed(UseCaseResultType.CANCEL);
            }
            var dto = dtoOptional.get();
            if (bootstrapMode) {
                // Blocking, required at startup
                var user = userService.createUserSync(dto.username(), dto.role(), dto.password());
                succeeded(user);
                return WriteSingleResult.completed(UseCaseResultType.OK, user);
            } else {
                //Non-blocking normal use case
                return userService.createUser(dto.username(), dto.role(), dto.password())
                        .thenApply(user -> {
                            succeeded(user);
                            return new WriteSingleResult<>(UseCaseResultType.OK, user);
                        }).exceptionally(throwable -> {
                            view.showError("Error creando usuario.\n" + throwable.getMessage());
                            LOG.error("Cannot create user.", throwable);
                            return WriteSingleResult.error();
                        });
            }
        } catch (RuntimeException e) {
            LOG.error("User creation failed before sending request to persistence service.", e);
            view.showError("No pudimos iniciar la creación del usuario.\n" + e.getMessage());
            return WriteSingleResult.errorCompleted();
        }
    }

    /**
     * Displays a success message when a user has been successfully created.
     *
     * @param user the user object that was successfully created, containing details such as username and ID
     */
    private void succeeded(@NotNull User user) {
        view.showSuccess("Usuario %s creado exitosamente con id %d.".formatted(user.getUsername(), user.getId()));
    }
}
