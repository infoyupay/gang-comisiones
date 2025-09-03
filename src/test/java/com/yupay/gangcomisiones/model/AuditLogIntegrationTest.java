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


import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link AuditLog}, successfully tested by dvidal
 * passing 4 tests in 567 ms.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class AuditLogIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Truncates tables.
     */
    @BeforeEach
    void cleanTable() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE public.audit_log CASCADE").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE public.user CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Tests that the audit log is persisted with the user information.
     */
    @Test
    void shouldPersistAuditLogWithUser() {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Arrange: create a User (assuming minimal constructor/setters available)
        User user = User.builder()
                .username("tester")
                .password("secret25")
                .role(UserRole.ROOT)
                .active(true)
                .build();
        em.persist(user);

        AuditLog log = AuditLog.builder()
                .user(user)
                .action("LOGIN")
                .entity("User")
                .entityId(user.getId())
                .details("User logged in")
                .computerName("workstation-01")
                .build();

        // Act
        em.persist(log);
        em.flush();  // ensures insert in DB
        em.refresh(log); // reloads eventStamp, id, etc.
        tx.commit();

        // Assert
        assertNotNull(log.getId());
        assertNotNull(log.getEventStamp());
        assertEquals("LOGIN", log.getAction());
        assertEquals(user.getId(), log.getUser().getId());

        em.close();
    }

    /**
     * Tests that AuditLog insert fails when user is null.
     */
    @Test
    void shouldFailWhenUserIsNull() {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        AuditLog log = AuditLog.builder()
                .action("DELETE")
                .computerName("pc01")
                .build();

        assertThrows(Exception.class, () -> {
            em.persist(log);
            em.flush();
        });

        tx.rollback();
        em.close();
    }

    /**
     * Tests that AuditLog insert fails when action is empty.
     */
    @Test
    void shouldFailWhenActionIsEmpty() {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        User user = User.builder()
                .username("tester2")
                .password("secret25")
                .role(UserRole.ROOT)
                .active(true)
                .build();

        AuditLog log = AuditLog.builder()
                .user(user)
                .action("") // violates CHECK constraint
                .computerName("pc02")
                .build();

        assertThrows(Exception.class, () -> {
            em.persist(log);
            em.flush();
        });

        tx.rollback();
        em.close();
    }

    /**
     * Test that index (event_stamp descending) is working fine on queries.
     */
    @Test
    void shouldQueryLogsOrderedByEventStampDescForUser() {
        // Arrange: create user (single transaction)
        User user;
        try (EntityManager em = ctx.getEntityManagerFactory().createEntityManager();) {
            em.getTransaction().begin();

            user = User.builder()
                    .username("auditUser")
                    .password("secret25")
                    .role(UserRole.ROOT)
                    .active(true)
                    .build();
            em.persist(user);

            em.getTransaction().commit();
        }

        // Persist 3 logs in separate transactions.
        persistAuditLog(user, "ACTION1");
        persistAuditLog(user, "ACTION2");
        persistAuditLog(user, "ACTION3");

        List<AuditLog> result;
        // Query descending order.
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            result = em.createQuery(
                            "SELECT l FROM AuditLog l WHERE l.user = :user ORDER BY l.eventStamp DESC",
                            AuditLog.class)
                    .setParameter("user", user)
                    .getResultList();
        }

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.get(0).getEventStamp().isAfter(result.get(1).getEventStamp()));
        assertTrue(result.get(1).getEventStamp().isAfter(result.get(2).getEventStamp()));
        assertEquals("ACTION3", result.get(0).getAction());
        assertEquals("ACTION2", result.get(1).getAction());
        assertEquals("ACTION1", result.get(2).getAction());
    }

    /**
     * Persists an audit log entry in a single transaction and refreshes the entity to
     * retrieve event stamp. Also, sleeps for 5 ms to avoid timestamp collisions.
     *
     * @param user   The user associated with the audit log.
     * @param action The action performed.
     */
    private void persistAuditLog(User user, String action) {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();

            AuditLog log = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .computerName("pc1")
                    .build();

            em.persist(log);
            em.getTransaction().commit();
            em.refresh(log);
        }

        // Sleep to avoid timestamp collisions.
        try {
            Thread.sleep(5);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

}
