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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test class for entity {@link User}.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UserIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Tests persisting and querying a user.
     */
    @Test
    void testPersistAndQueryUser() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();

        em.getTransaction().begin();
        User user = User.builder().username("admin")
                .password("salt01..")
                .role(UserRole.ROOT)
                .active(true)
                .build();
        em.persist(user);
        em.getTransaction().commit();

        User found = em.find(User.class, user.getId());
        assertNotNull(found);
        assertEquals("admin", found.getUsername());
        assertEquals(UserRole.ROOT, found.getRole());

        em.close();
    }
}
