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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@code User} entity.
 * This class extends {@code AbstractPostgreIntegrationTest} to leverage base functionality for database integration tests.
 * The tests validate operations such as persisting and querying user entities within a PostgreSQL database environment.
 * <br/>
 * <br/>
 * The functionality of this test class includes:
 * <ul>
 *   <li>Database cleanup before each test to ensure isolated test cases.</li>
 *   <li>Validations for persisting a {@code User} entity and ensuring data integrity.</li>
 *   <li>Assertions to confirm password verification and role consistency.</li>
 * </ul>
 * <br/>
 * The following methods are part of this class:
 * <ol>
 *   <li><b>cleanDatabase</b>: Cleans the database by truncating tables before each test execution to ensure a consistent state.</li>
 *   <li><b>testPersistAndQueryUser</b>: Validates the process of persisting a {@code User} entity and performing a query to ensure correctness and integrity.</li>
 * </ol>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution Note:</strong> dvidal@infoyupay.com passed 1 tests in 1.060s at 2025-09-28 19:36 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UserIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Before each test execution, cleans the databases truncating tables.
     */
    @BeforeEach
    void cleanDatabase() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests persisting and querying a user.
     */
    @Test
    void testPersistAndQueryUser() {
        var persisted = performInTransaction(em -> {
            //Arrange
            var r = new User[2];
            r[0] = User.builder()
                    .username("admin")
                    .password("salt01..")
                    .role(UserRole.ROOT)
                    .active(true)
                    .build();
            //Act
            em.persist(r[0]);
            em.flush();
            r[1] = em.find(User.class, r[0].getId());
            return r;
        });
        //Assert
        assertThat(persisted[0])
                .isNotNull()
                .extracting(User::getId)
                .isNotNull();
        assertThat(persisted[0])
                .returns(true, u -> u.verifyPassword("salt01.."));
        assertThat(persisted[1]).isNotNull();
        assertThat(persisted[1].getId()).isNotNull();
        assertThat(persisted[0]).isEqualTo(persisted[1]);
        assertThat(persisted[1]).returns(true, u -> u.verifyPassword("salt01.."));
        assertThat(persisted[1].getRole()).isEqualTo(persisted[0].getRole());
    }
}
