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

package com.yupay.gangcomisiones.usecase;

import com.yupay.gangcomisiones.model.AuditLog;
import com.yupay.gangcomisiones.model.User;
import jakarta.persistence.EntityManagerFactory;

/**
 * The AuditLogChecker class provides utilities for verifying the presence of
 * audit log entries associated with specific entities and users.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class AuditLogChecker {
    /**
     * Checks if an audit log entry exists for the given entity ID and user.
     *
     * @param entityId the ID of the entity to check for an audit log entry
     * @param user     the user associated with the audit log entry
     * @param emf      the entity manager factory used to create the entity manager
     * @return true if an audit log entry exists, false otherwise
     */
    public static boolean checkAuditLogExists(Long entityId, User user, EntityManagerFactory emf) {
        try (var em = emf.createEntityManager()) {
            return !em.createQuery(
                            "SELECT a FROM AuditLog a WHERE a.entityId = :id AND a.user = :user",
                            AuditLog.class)
                    .setParameter("id", entityId)
                    .setParameter("user", user)
                    .getResultList()
                    .isEmpty();
        }
    }

    /**
     * Checks if any audit log entry exists in database.
     *
     * @param emf the entity manager factory used to create the entity manager
     * @return true if any audit log entry exists, false otherwise
     */
    public static boolean checkAnyAuditLogExists(EntityManagerFactory emf) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT COUNT(a) FROM AuditLog a",
                            Long.class)
                    .getSingleResult() > 0;
        }
    }
}
