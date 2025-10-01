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

import com.yupay.gangcomisiones.AppContext;
import com.yupay.gangcomisiones.model.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Provides a simple in-memory cache for the application's {@link GlobalConfig}.
 * <br/>
 * The cache keeps the last successfully fetched configuration and refreshes it
 * on demand after a time-based expiration period.
 *
 * <p>Behavior overview:</p>
 * <ul>
 *   <li>Fetches the configuration through the {@link com.yupay.gangcomisiones.services.GlobalConfigService}.</li>
 *   <li>Stores the last value and the timestamp of the fetch.</li>
 *   <li>On errors during fetch, it returns the last cached value if present; otherwise, a dummy configuration.</li>
 *   <li>Refreshes at most every 5 minutes to avoid excessive calls.</li>
 * </ul>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class GlobalConfigCache {
    private final Logger LOG = LoggerFactory.getLogger(GlobalConfigCache.class);
    private final AppContext context;
    private GlobalConfig globalConfig;
    private Instant fetchStamp;

    /**
     * Creates a new cache tied to the given application context.
     *
     * @param context the application context used to access services.
     */
    public GlobalConfigCache(AppContext context) {
        this.context = context;
    }

    /**
     * Fetches the global configuration from the underlying service and updates the cache.
     * <br/>
     * In case of failure, the method falls back to:
     * <ul>
     *   <li>Returning the last cached value if available.</li>
     *   <li>Returning and caching a dummy configuration otherwise.</li>
     * </ul>
     *
     * <p>This method is synchronized to prevent concurrent refreshes.</p>
     *
     * @return the freshly fetched configuration, or a fallback as described above.
     */
    public synchronized GlobalConfig fetchGlobalConfig() {
        try {
            var config = context.getGlobalConfigService()
                    .fetchGlobalConfigSync()
                    .orElseGet(() -> GlobalConfig.builder().build());

            globalConfig = config;
            fetchStamp = Instant.now();
            return config;
        } catch (Exception e) {
            // Plan B: on error, return last value even if due.
            if (globalConfig != null) {
                LOG.error("Error fetching GlobalConfig, using cached value.", e);
                return globalConfig;
            }
            // If no cached value, set and return dummy value.
            var dummy = GlobalConfig.builder().build();
            globalConfig = dummy;
            return dummy;
        }
    }

    /**
     * Returns the cached configuration if present and not expired; otherwise, triggers a refresh.
     * <br/>
     * Expiration policy:
     * <ul>
     *   <li>If no previous fetch was performed, it fetches immediately.</li>
     *   <li>If the last fetch was 5 minutes ago or more, it refreshes the cache.</li>
     * </ul>
     *
     * <p>This method is synchronized to avoid multiple threads refreshing simultaneously.</p>
     *
     * @return the current valid configuration, possibly refreshed.
     */
    public synchronized GlobalConfig getOrFetchGlobalConfig() {
        // avoid multiple threads refreshing
        var expired = fetchStamp == null
                || Duration.between(fetchStamp, Instant.now()).toMinutes() >= 5;
        if (globalConfig == null || expired) {
            return fetchGlobalConfig();
        }
        return globalConfig;
    }
}
