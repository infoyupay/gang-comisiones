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

package com.yupay.gangcomisiones;

import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility helper for retrieving test resources as {@link Path}.
 * <br/>
 * This is mainly used in integration tests to load the dummy JPA properties file
 * and other resources located in the test classpath.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class DummyHelpers {
    /**
     * Resolves a resource available in the test classpath into a {@link Path}.
     *
     * @param resourceName the resource name, e.g. {@code "dummy-jpa.properties"}.
     * @return a {@link Path} pointing to the resource.
     * @throws NullPointerException if the resource is not found in the classpath.
     * @throws RuntimeException     if the resource URI cannot be converted into a path.
     */
    public static @NotNull Path getDummyPathFromResource(String resourceName) {
        try {
            var resource = DummyHelpers.class.getResource(resourceName);
            Objects.requireNonNull(resource, "TEST Resource '" + resourceName + "' not found.");
            return Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error getting resource " + resourceName, e);
        }
    }

    /**
     * Returns the path to the dummy JPA properties file for integration tests.
     *
     * @return a {@link Path} to {@code dummy-jpa.properties}.
     */
    public static @NotNull Path getDummyJpaProperties() {
        return getDummyPathFromResource("dummy-jpa.properties");
    }
}
