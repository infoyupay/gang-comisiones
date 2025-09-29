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

import com.yupay.gangcomisiones.model.User;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Manages the currently logged-in user and notifies listeners when the user changes.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class UserSession {
    private static final Logger LOG = LoggerFactory.getLogger(UserSession.class);
    private final List<BiConsumer<User, User>> listeners = new CopyOnWriteArrayList<>();
    /**
     * -- GETTER --
     * Returns the currently logged-in user, or null if no user is logged in.
     */
    @Getter
    private volatile User currentUser;

    /**
     * Sets the currently logged-in user and notifies all registered listeners.
     *
     * @param user the new user to set; can be null to indicate logout
     */
    public void setCurrentUser(User user) {
        var oldUser = this.currentUser;
        this.currentUser = user;
        notifyListeners(oldUser, user);
    }

    /**
     * Adds a listener that will be notified when the current user changes.
     * The listener receives both the old and the new user.
     *
     * @param listener a BiConsumer taking (oldUser, newUser)
     */
    public void addListener(@NotNull BiConsumer<User, User> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(@NotNull BiConsumer<User, User> listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners of a change in the current user.
     *
     * @param oldUser the previous user
     * @param newUser the new user
     */
    private void notifyListeners(User oldUser, User newUser) {
        for (var listener : listeners) {
            try {
                listener.accept(oldUser, newUser);
            } catch (Exception e) {
                LOG.error("Error notifying listener", e);
            }
        }
    }

    /**
     * Clears the current user (logs out) and notifies listeners.
     */
    public void logout() {
        setCurrentUser(null);
    }
}
