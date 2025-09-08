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

package com.yupay.gangcomisiones.usecase.setglobalconfig;

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.GlobalConfig;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.GlobalConfigService;
import com.yupay.gangcomisiones.services.UserService;
import com.yupay.gangcomisiones.usecase.commons.PrivilegeChecker;
import com.yupay.gangcomisiones.usecase.commons.UseCaseResultType;
import com.yupay.gangcomisiones.usecase.commons.WriteSingleResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the "Set Global Config" use case.
 * <br/>
 * Orchestrates privilege validation, current configuration retrieval, view interaction,
 * and persistence via {@link GlobalConfigService}.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class SetGlobalConfigController {
    private final SetGlobalConfigView view;
    private final UserService userService;
    private final GlobalConfigService globalConfigService;
    private final boolean bootstrapMode;
    private final Logger LOG = LoggerFactory.getLogger(SetGlobalConfigController.class);

    /**
     * Creates the controller.
     *
     * @param view                the view implementation
     * @param userService         the user service dependency
     * @param globalConfigService the global config service dependency
     * @param bootstrapMode       whether the controller runs in bootstrap mode
     */
    public SetGlobalConfigController(@NotNull SetGlobalConfigView view,
                                     @NotNull UserService userService,
                                     @NotNull GlobalConfigService globalConfigService,
                                     boolean bootstrapMode) {
        this.view = view;
        this.userService = userService;
        this.globalConfigService = globalConfigService;
        this.bootstrapMode = bootstrapMode;
    }

    /**
     * Checks if the user has root privileges.
     *
     * @param user the user to check.
     * @return true if the user has root privileges, false otherwise.
     */
    private boolean checkRootPrivileges(@NotNull User user) {
        return PrivilegeChecker.checkPrivileges(
                AppContext.getInstance().getEntityManagerFactory(),
                userService,
                user,
                UserRole.ROOT,
                view);
    }

    /**
     * Executes the use case.
     *
     * @return a future containing a {@link WriteSingleResult}
     * with the outcome and possibly the updated {@link GlobalConfig}
     */
    public CompletableFuture<WriteSingleResult<GlobalConfig>> run() {
        try {
            var currentUser = AppContext.getInstance().getUserSession().getCurrentUser();
            if (!checkRootPrivileges(currentUser)) {
                return WriteSingleResult.errorCompleted();
            }

            // Step 3: get current GlobalConfig via cache (fresh fetch)
            var current = AppContext.getInstance().getGlobalConfigCache().fetchGlobalConfig();
            var copyForEditing = current.toBuilder().build();

            // Step 4-7: show form and handle optional response
            var editedOpt = view.showSetGlobalConfigForm(copyForEditing, bootstrapMode);
            if (editedOpt.isEmpty()) {
                return WriteSingleResult.completed(UseCaseResultType.CANCEL);
            }
            var edited = editedOpt.get();

            if (bootstrapMode) {
                // 9.a bootstrap: blocking update
                globalConfigService.updateGlobalConfig(edited).join();
                view.showSuccess("Configuración global actualizada correctamente.");
                return WriteSingleResult.completed(UseCaseResultType.OK, edited);
            }

            // 9.b normal: non-blocking
            return globalConfigService.updateGlobalConfig(edited)
                    .thenApply(_ -> {
                        view.showSuccess("Configuración global actualizada correctamente.");
                        return new WriteSingleResult<>(UseCaseResultType.OK, edited);
                    })
                    .exceptionally(t -> {
                        view.showError("Error actualizando configuración global.\n" + t.getMessage());
                        LOG.error("Cannot update GlobalConfig.", t);
                        return WriteSingleResult.error();
                    });
        } catch (RuntimeException e) {
            LOG.error("Set Global Config failed before sending request to persistence service.", e);
            view.showError("No pudimos iniciar el proceso de actualización de configuración.\n" + e.getMessage());
            return WriteSingleResult.errorCompleted();
        }
    }
}
