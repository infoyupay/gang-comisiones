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

package com.yupay.gangcomisiones.services.impl;

import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.User;
import com.yupay.gangcomisiones.model.UserRole;
import com.yupay.gangcomisiones.services.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.Predicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Implementation of the UserService interface.
 *
 * @param emf          the entity manager factory to use in the service instance.
 * @param jdbcExecutor the executor service to use for executing JDBC operations.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record UserServiceImpl(EntityManagerFactory emf,
                              ExecutorService jdbcExecutor) implements UserService, RollbackChecker {
    @Contract("_, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<User> createUser(String username, UserRole role, String plainPassword) {
        return CompletableFuture.supplyAsync(() -> {
            var user = User.builder()
                    .username(username)
                    .role(role)
                    .active(true)
                    .password(plainPassword)
                    .build();
            EntityTransaction et = null;
            try (var em = emf.createEntityManager()) {
                et = em.getTransaction();
                et.begin();
                em.persist(user);
                et.commit();
            } catch (RuntimeException e) {
                checkTxAndRollback(et, e);
            }
            return user;
        }, jdbcExecutor);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        return CompletableFuture.runAsync(() -> {
            EntityTransaction et = null;
            try (var em = emf.createEntityManager()) {
                et = em.getTransaction();
                et.begin();
                var user = em.find(User.class, userId);
                if (user == null) throw UserServiceError.USER_NOT_FOUND_BY_ID.createException(userId);
                if (!user.getActive()) throw UserServiceError.USER_NOT_ACTIVE.createException(user.getUsername());
                if (!user.verifyPassword(oldPassword)) {
                    throw UserServiceError.PASSWORD_MISMATCH.createException(user.getUsername());
                }
                user.setPassword(newPassword);
                et.commit();
            } catch (RuntimeException e) {
                checkTxAndRollback(et, e);
            }
        }, jdbcExecutor);
    }

    @Contract("_ -> new")
    @Override
    public @NotNull CompletableFuture<Optional<User>> findById(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                return Optional.ofNullable(em.find(User.class, userId));
            }
        }, jdbcExecutor);
    }

    @Contract("_ -> new")
    @Override
    public @NotNull CompletableFuture<Optional<User>> findByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                return findByUsername(username, em);
            }
        }, jdbcExecutor);
    }

    /**
     * Finds a user by their username using the provided EntityManager.
     *
     * @param username the username of the user to search for; must not be null
     * @param em       the EntityManager to use for executing the query; must not be null
     * @return an Optional containing the User if found; otherwise, an empty Optional
     */
    private @NotNull Optional<User> findByUsername(@NotNull String username, @NotNull EntityManager em) {
        var query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    @Contract(" -> new")
    @Override
    public @NotNull CompletableFuture<List<User>> listAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                var query = em.createQuery("SELECT u FROM User u", User.class);
                return query.getResultList();
            }
        }, jdbcExecutor);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Optional<User>> validateUser(@NotNull String username,
                                                                   @NotNull String password,
                                                                   @Nullable UserRole role) {
        return CompletableFuture.supplyAsync(() -> {
            try (var em = emf.createEntityManager()) {
                return validateUser(username, password, role, em);
            }
        }, jdbcExecutor);
    }

    @Contract("_, _, _, _ -> new")
    @Override
    public @NotNull CompletableFuture<Void> resetPassword(@NotNull String rootUsername,
                                                          @NotNull String rootPassword,
                                                          @NotNull String username,
                                                          @NotNull String newPassword) {
        return CompletableFuture.runAsync(() -> {
            EntityTransaction et = null;
            try (var em = emf.createEntityManager()) {
                et = em.getTransaction();
                et.begin();
                validateUser(rootUsername, rootPassword, UserRole.ROOT, em)
                        .orElseThrow(() -> UserServiceError.ROOT_AUTH_FAILED.createException(rootUsername));
                var user = findByUsername(username, em)
                        .orElseThrow(() -> UserServiceError.USER_NOT_FOUND_BY_USERNAME.createException(username));
                user.setPassword(newPassword);
                et.commit();
            } catch (RuntimeException e) {
                checkTxAndRollback(et, e);
            }
        }, jdbcExecutor);
    }

    /**
     * Validates a user's credentials and role.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role     The role of the user.
     * @param em       The EntityManager to use for the query.
     * @return An Optional containing the User if the credentials and role are valid, or an empty Optional otherwise.
     */
    private Optional<User> validateUser(@NotNull String username,
                                        @NotNull String password,
                                        @Nullable UserRole role,
                                        @NotNull EntityManager em) {
        var cb = em.getCriteriaBuilder();
        var qry = cb.createQuery(User.class);
        var root = qry.from(User.class);
        var predicates = Stream.of(
                        cb.equal(root.get("username"), username),
                        cb.isTrue(root.get("active")),
                        role != null ? cb.equal(root.get("role"), role) : null)
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);
        qry.select(root).where(cb.and(predicates));

        return em.createQuery(qry)
                .getResultStream()
                .findFirst()
                .filter(u -> u.verifyPassword(password));
    }

    /**
     * Enumeration with UserServices specific error messages.
     *
     * @author InfoYupay SACS
     * @version 1.0
     */
    private enum UserServiceError {
        /**
         * When user is searched by ID but not found.
         */
        USER_NOT_FOUND_BY_ID("User not found by ID", "userId"),
        /**
         * When user is searched by username but not found.
         */
        USER_NOT_FOUND_BY_USERNAME("User not found by username", "username"),
        /**
         * When a user is not active (active flag is false).
         */
        USER_NOT_ACTIVE("User is not active", "username"),
        /**
         * When user password doesn't match with an external password.
         */
        PASSWORD_MISMATCH("Password does not match", "username"),
        /**
         * When authenticating a root user fails.
         */
        ROOT_AUTH_FAILED("Root authentication failed", "username");

        /**
         * The default message.
         */
        private final String defaultMessage;

        /**
         * The faulty parameter name associated with the error.
         */
        private final String faultyParam;

        /**
         * Constructs a new UserServiceError with the given default message.
         *
         * @param defaultMessage the default message.
         * @param faultyParam    the faulty parameter name.
         */
        @Contract(pure = true)
        UserServiceError(String defaultMessage, String faultyParam) {
            this.defaultMessage = defaultMessage;
            this.faultyParam = faultyParam;
        }

        /**
         * Creates a new PersistenceServicesException with the given faulty value, and cause.
         *
         * @param faultyValue the faulty value.
         * @param cause       the cause of the exception.
         * @return the created PersistenceServicesException.
         */
        public @NotNull PersistenceServicesException createException(
                @Nullable Object faultyValue,
                @Nullable Throwable cause) {
            var r = new PersistenceServicesException("[%s] %s: %s = %s"
                    .formatted(
                            this.name(),
                            defaultMessage,
                            faultyParam,
                            faultyValue));
            if (cause != null) {
                r.initCause(cause);
            }
            return r;
        }

        /**
         * Creates a new PersistenceServicesException with the given faulty value, and null cause.
         *
         * @param faultyValue the faulty value.
         * @return the created PersistenceServicesException {@code createException(faultyValue, null);}
         */
        public @NotNull PersistenceServicesException createException(
                @Nullable Object faultyValue) {
            return createException(faultyValue, null);
        }
    }
}
