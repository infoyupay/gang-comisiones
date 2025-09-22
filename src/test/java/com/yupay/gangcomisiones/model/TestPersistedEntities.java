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

package com.yupay.gangcomisiones.model;

import com.yupay.gangcomisiones.AppContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Facility to centralize test entities creation and persist.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class TestPersistedEntities {
    /**
     * User container.
     */
    public static final Map<UserRole, User> USER = new ConcurrentHashMap<>();
    /**
     * Concept container.
     */
    public static final AtomicReference<Concept> CONCEPT = new AtomicReference<>(null);
    /**
     * Bank container.
     */
    public static final AtomicReference<Bank> BANK = new AtomicReference<>(null);
    /**
     * Transaction container.
     */
    public static final AtomicReference<Transaction> TRANSACTION = new AtomicReference<>(null);
    private static Logger LOG;

    /**
     * Inner logger.
     *
     * @return the logger.
     */
    private static Logger getLog() {
        if (LOG == null) {
            LOG = LoggerFactory.getLogger(TestPersistedEntities.class);
        }
        return LOG;
    }

    /**
     * Cleans all created values from memory and truncates all database tables.
     *
     * @param emf the entity manager factory.
     */
    public static void clean(EntityManagerFactory emf) {
        USER.clear();
        CONCEPT.set(null);
        BANK.set(null);
        TRANSACTION.set(null);
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE reversal_request CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE transaction CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE concept CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE bank CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE global_config CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE audit_Log CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE \"user\" CASCADE").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(TestPersistedEntities.class).error("Cannot truncate tables.", e);
        }
    }

    /**
     * Retrieves or creates and persists a user associated with the specified role.
     * If a user for the given role is not already stored, this method will create
     * and persist a new user based on the specified role.
     * <br/><br/>
     * The following rules dictate the behavior based on the role:
     * <ul>
     *   <li><strong>ROOT:</strong> Creates and persists a root user.</li>
     *   <li><strong>ADMIN:</strong> Creates and persists an admin user.</li>
     *   <li><strong>CASHIER:</strong> Creates and persists a cashier user.</li>
     * </ul>
     *
     * @param em   the {@link EntityManager} used for persistence operations. Must not be {@code null}.
     * @param role the {@link UserRole} determining the type of user to create. Must not be {@code null}.
     * @return the persisted {@link User} associated with the specified role.
     * @throws NullPointerException if either {@code em} or {@code role} is {@code null}.
     */
    public static User persistedUserWithRole(@NotNull EntityManager em, @NotNull UserRole role) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(role);
        return USER.computeIfAbsent(role, k -> switch (k) {
            case ROOT -> samplePersistedRoot(em);
            case ADMIN -> samplePersistedAdmin(em);
            case CASHIER -> samplePersistedCashier(em);
        });
    }

    /**
     * Creates and returns a pre-configured {@link User} instance representing a ROOT user.
     * <br/><br/>
     * The returned user object is initialized with the following attributes:
     * <ul>
     *   <li><strong>Username:</strong> "rootUser"</li>
     *   <li><strong>Password:</strong> "password"</li>
     *   <li><strong>Role:</strong> {@link UserRole#ROOT}</li>
     *   <li><strong>Active:</strong> {@code true}</li>
     * </ul>
     *
     * @return a {@link User} instance with predefined ROOT user attributes.
     */
    public static User sampleRoot() {
        return User.builder()
                .active(true)
                .password("password")
                .role(UserRole.ROOT)
                .username("rootUser")
                .build();
    }

    /**
     * Creates and persists a sample ROOT user within the provided {@link EntityManager} context.
     * <br/><br/>
     * This method performs the following steps:
     * <ol>
     *   <li>Creates a new sample ROOT user using {@code sampleRoot()} method.</li>
     *   <li>Persists the created user using the provided {@link EntityManager}.</li>
     *   <li>Returns the persisted {@link User} instance.</li>
     * </ol>
     *
     * @param em the {@link EntityManager} used for persistence operations. Must not be {@code null}.
     * @return the persisted {@link User} instance representing a ROOT user.
     * @throws NullPointerException if {@code em} is {@code null}.
     */
    private static User samplePersistedRoot(@NotNull EntityManager em) {
        var r = sampleRoot();
        em.persist(r);
        return r;
    }

    /**
     * Creates and persists a valid test ROOT user.
     *
     * @param em the entity manager.
     * @return the created user.
     */
    public static User persistRootUser(@NotNull EntityManager em) {
        return persistedUserWithRole(em, UserRole.ROOT);
    }

    /**
     * Creates and returns a pre-configured {@link User} instance representing a CASHIER user.
     * <br/><br/>
     * The returned user object is initialized with the following attributes:
     * <ul>
     *   <li><strong>Username:</strong> "cashier"</li>
     *   <li><strong>Password:</strong> "abcd1234"</li>
     *   <li><strong>Role:</strong> {@link UserRole#CASHIER}</li>
     *   <li><strong>Active:</strong> {@code true}</li>
     * </ul>
     * <br/>
     *
     * @return a {@link User} instance with predefined CASHIER user attributes.
     */
    public static User sampleCashier() {
        return User.builder()
                .active(true)
                .password("abcd1234")
                .role(UserRole.CASHIER)
                .username("cashier")
                .build();
    }

    /**
     * Creates and persists a sample {@link User} instance representing a CASHIER user within the provided {@link EntityManager} context.
     * <br/><br/>
     * This method performs the following steps:
     * <ol>
     *   <li>Creates a new sample CASHIER user using the {@code sampleCashier()} method.</li>
     *   <li>Persists the created user using the provided {@link EntityManager} instance.</li>
     *   <li>Returns the persisted {@link User} instance.</li>
     * </ol>
     *
     * @param em the {@link EntityManager} used for persistence operations. Must not be {@code null}.
     * @return the persisted {@link User} instance representing a CASHIER user.
     * @throws NullPointerException if {@code em} is {@code null}.
     */
    private static User samplePersistedCashier(@NotNull EntityManager em) {
        var r = sampleCashier();
        em.persist(r);
        return r;
    }

    /**
     * Creates and persists a valid test CASHIER user.
     *
     * @param em the entity manager.
     * @return the created user.
     */
    public static User persistCashierUser(@NotNull EntityManager em) {
        return persistedUserWithRole(em, UserRole.CASHIER);
    }

    /**
     * Creates and returns a pre-configured {@link User} instance representing an ADMIN user.
     * <br/><br/>
     * The returned user object is initialized with the following attributes:
     * <ul>
     *   <li><strong>Username:</strong> "admin"</li>
     *   <li><strong>Password:</strong> "12345678"</li>
     *   <li><strong>Role:</strong> {@link UserRole#ADMIN}</li>
     *   <li><strong>Active:</strong> {@code true}</li>
     * </ul>
     *
     * @return a {@link User} instance with predefined ADMIN user attributes.
     */
    public static User sampleAdmin() {
        return User.builder()
                .active(true)
                .password("12345678")
                .role(UserRole.ADMIN)
                .username("admin")
                .build();
    }

    /**
     * Creates and persists a sample {@link User} instance representing an ADMIN user within the provided
     * {@link EntityManager} context.
     * <br/><br/>
     * This method performs the following steps:
     * <ol>
     *   <li>Creates a new sample ADMIN user using the {@code sampleAdmin()} method.</li>
     *   <li>Persists the created user using the provided {@link EntityManager} instance.</li>
     *   <li>Returns the persisted {@link User} instance.</li>
     * </ol>
     *
     * @param em the {@link EntityManager} used for persistence operations. Must not be {@code null}.
     *
     * @return the persisted {@link User} instance representing an ADMIN user.
     *
     * @throws NullPointerException if {@code em} is {@code null}.
     */
    private static User samplePersistedAdmin(@NotNull EntityManager em) {
        var r = sampleAdmin();
        em.persist(r);
        return r;
    }

    /**
     * Creates and persists a valid test ADMIN user.
     *
     * @param em the entity manager.
     * @return the created user.
     */
    public static User persistAdminUser(@NotNull EntityManager em) {
        return persistedUserWithRole(em, UserRole.ADMIN);
    }

    /**
     * Creates and persists a valid test concept.
     *
     * @param em the entity manager.
     * @return the created concept.
     */
    public static Concept persistConcept(@NotNull EntityManager em) {
        if (CONCEPT.get() == null) {
            var r = Concept.builder()
                    .active(true)
                    .name("Test Concept")
                    .type(ConceptType.FIXED)
                    .value(new BigDecimal("10.0000"))
                    .build();
            em.persist(r);
            CONCEPT.set(r);
        }
        return CONCEPT.get();
    }

    /**
     * Creates and persists a valid test bank.
     *
     * @param em the entity manager.
     * @return the created bank.
     */
    public static Bank persistBank(@NotNull EntityManager em) {
        if (BANK.get() == null) {
            var r = Bank.builder()
                    .active(true)
                    .name("Test Bank")
                    .build();
            em.persist(r);
            BANK.set(r);
        }
        return BANK.get();
    }

    /**
     * Creates a valid test transaction persisting dependents entities.
     *
     * @param em the entity manager.
     * @return the created transaction.
     */
    public static Transaction buildValidTansaction(EntityManager em) {
        return Transaction.builder()
                .bank(persistBank(em))
                .concept(persistConcept(em))
                .conceptName(CONCEPT.get().getName())
                .cashier(persistCashierUser(em))
                .amount(new BigDecimal("100.0000"))
                .commission(new BigDecimal("10.0000"))
                .status(TransactionStatus.REGISTERED)
                .build();
    }

    /**
     * Creates the global config with Lorem Ipsum values.
     * @param em entity manager.
     * @return the persisted entity.
     */
    public static GlobalConfig persistGlobalConfig(@NotNull EntityManager em) {
        var r = GlobalConfig
                .builder()
                .address("Jr. Neque Porro 355, Of. 1092, Quia - Consectetur")
                .announcement("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .businessName("LIPSUM S.A.C.")
                .id((short)1)
                .legalName("LOREM IPSUM S.A.C.")
                .ruc("20123456780")
                .updatedBy(persistRootUser(em))
                .updatedFrom("lipsum-computer")
                .build();
        em.persist(r);
        return r;
    }

    /**
     * Creates and persists a valid test transaction.
     *
     * @param em the entity manager.
     * @return the created transaction.
     */
    public static Transaction persistTransaction(EntityManager em) {
        if (TRANSACTION.get() == null) {
            var r = buildValidTansaction(em);
            em.persist(r);
            em.flush();
            em.refresh(r);
            TRANSACTION.set(r);
        }
        return TRANSACTION.get();
    }

    /**
     * Executes a given operation within a transactional context.
     * An EntityManager is provided to the operation, and the transaction
     * is committed if the operation completes successfully or rolled back
     * in case of a runtime exception.
     *
     * @param <T>       the type of the result produced by the operation performed within the transaction
     * @param ctx       the application context containing the EntityManagerFactory
     * @param performer a function that takes an EntityManager, performs an operation, and produces a result
     * @return the result of the operation performed within the transaction
     * @throws RuntimeException if an exception occurs during the transaction, after rolling back the transaction
     */
    public static <T> T performInTransaction(@NotNull AppContext ctx,
                                             @NotNull Function<EntityManager, T> performer) {
        EntityTransaction tx = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            var r = performer.apply(em);
            tx.commit();
            return r;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

}
