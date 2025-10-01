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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.model.GlobalConfig;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Defines asynchronous operations to retrieve and update the application's {@link GlobalConfig}.
 * <br/>
 * Implementations should interact with the persistence layer in a non-blocking fashion and
 * complete returned {@link CompletableFuture} instances when the operation finishes.
 * <ul>
 *   <li>Read: fetch the single global configuration record, if present.</li>
 *   <li>Write: persist changes to the global configuration.</li>
 * </ul>
 *
 * <p>Unless explicitly stated by an implementation, this interface does not define
 * thread-safety guarantees; callers should coordinate concurrent access as needed.</p>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface GlobalConfigService {
    /**
     * Synchronously fetches the current global configuration from the persistence layer.
     * <br/>
     * The method retrieves the global configuration record if it exists. The result is returned synchronously,
     * meaning the caller is blocked until the operation completes.
     * <br/>
     * <p>
     * If no global configuration is present, the result will be an empty {@link Optional}.
     * <br/>
     *
     * <ul>
     *   <li><strong>Success:</strong> Returns an {@link Optional} containing the {@link GlobalConfig} when it exists.</li>
     *   <li><strong>Empty result:</strong> Returns an empty {@link Optional} when no configuration is found.</li>
     *   <li><strong>Error:</strong> Throws an {@link Exception} if an error occurs.</li>
     * </ul>
     *
     * @return an {@link Optional} containing the {@link GlobalConfig}, or empty if no configuration exists.
     * @throws Exception if an error occurs while fetching the global configuration.
     */
    Optional<GlobalConfig> fetchGlobalConfigSync() throws Exception;

    /**
     * Asynchronously persists the provided global configuration.
     * <br/>
     * The returned future completes when the update operation finishes. If an error occurs
     * during validation or persistence, the future completes exceptionally.
     *
     * @param config the {@link GlobalConfig} instance to be created or updated; must not be {@code null}.
     * @return a {@link CompletableFuture} that completes when the update has been committed.
     */
    CompletableFuture<Void> updateGlobalConfig(GlobalConfig config);
}
