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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a hierarchical path structure, supporting lazy computation and caching of a
 * {@link Path} instance derived from parent paths, root suppliers, and a name component.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class PathHolder {
    private final PathHolder parent;
    private final Supplier<String> root;
    private final String name;
    private Path value;
    /**
     * Constructs a new {@code PathHolder} instance, representing a hierarchical path structure.
     *
     * @param parent the parent {@code PathHolder}, used for hierarchical resolution,
     *               or {@code null} if this is a root path
     * @param root   a {@link Supplier} providing the root path as a string,
     *               or {@code null} if the root is fixed
     * @param name   the name of the current path element
     */
    @Contract(pure = true)
    public PathHolder(PathHolder parent, Supplier<String> root, String name) {
        this.parent = parent;
        this.root = root;
        this.name = name;
    }

    /**
     * Creates a new {@code PathHolder} instance representing a root path with the provided root supplier
     * and name.
     *
     * @param root a {@link Supplier} of the root path's string representation, allowing dynamic resolution
     * @param name the name of the current path element
     * @return a new {@code PathHolder} instance representing the root path with the specified name
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull PathHolder ofRoot(Supplier<String> root, String name) {
        return new PathHolder(null, root, name);
    }

    /**
     * Creates a new {@code PathHolder} instance as a child of the specified parent, with the given name.
     *
     * @param parent the parent {@code PathHolder} instance, representing the hierarchical structure
     * @param name   the name of the current path element
     * @return a new {@code PathHolder} instance representing the child path with the specified parent and name
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull PathHolder ofParent(PathHolder parent, String name) {
        return new PathHolder(parent, null, name);
    }

    /**
     * Creates a new {@code PathHolder} instance with the specified name.
     *
     * @param name the name of the path element
     * @return a new {@code PathHolder} instance representing the given name
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull PathHolder of(String name) {
        return new PathHolder(null, null, name);
    }

    /**
     * Retrieves the computed path value, computing it if it has not already been resolved.
     *
     * @return the resolved {@link Path} instance, computing it if necessary.
     */
    public Path asPath() {
        if (value == null) {
            computePath();
        }
        return value;
    }

    /**
     * Forces recomputation of the path, replacing any cached value.
     */
    public void computePath() {
        if (parent == null) {
            value = root == null
                    ? Path.of(name)
                    : Path.of(root.get(), name);
        } else {
            value = parent.asPath().resolve(name);
        }
    }

    /**
     * Resets the cached path value to null.
     * This method is intended for testing purposes only and clears the previously
     * computed or cached path value, forcing a recomputation when {@link #asPath()}
     * or related methods are called next.
     * <br/>
     * This is useful in scenarios where the state of the path value needs to be reset
     * to ensure accurate testing of lazy computation or caching logic.
     */
    @TestOnly
    @VisibleForTesting
    public void reset() {
        value = null;
    }

    /**
     * Returns a string representation of the current state of the path value.
     * If the path value is unresolved, returns a placeholder string.
     *
     * @return a string representation of the resolved path value,
     * or "&lt;unresolved&gt;" if the value is not yet computed.
     */
    @Override
    public String toString() {
        return Objects.toString(value, "<unresolved>");
    }

    /**
     * Returns the string representation of the resolved path value.
     * This method ensures the underlying path is fully resolved before converting it to a string.
     *
     * @return the string representation of the resolved path value.
     */
    public String toResolvedString() {
        return asPath().toString();
    }
}

