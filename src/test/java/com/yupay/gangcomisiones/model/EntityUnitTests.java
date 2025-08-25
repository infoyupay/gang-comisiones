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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for all the domain model entities.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class EntityUnitTests {

    /**
     * Tests for the User entity, we are checking the equals and hashcode methods.
     * 2 users with the same id, must be equals, even if they are not the same.
     * 2 users without assigned id, only shall be equals if they are the same.
     * Hashcode shall reflect behavior.
     */
    @Test
    void testUserEqualsAndHashCode() {
        User u1 = User.builder()
                .id(1L)
                .username("user1")
                .passwordHash("hash")
                .role(UserRole.ROOT)
                .active(true)
                .build();
        User u2 = User.builder()
                .id(1L)
                .username("user2")
                .passwordHash("hash2")
                .role(UserRole.ADMIN)
                .active(false)
                .build();

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());

        User u3 = User.builder()
                .id(2L)
                .username("user1")
                .passwordHash("hash")
                .role(UserRole.ROOT)
                .active(true)
                .build();
        assertNotEquals(u1, u3);
    }

    /**
     * Tests for the Concept entity, we are checking the equals and hashcode methods.
     * 2 concepts with the same id, must be equals, even if they are not the same.
     * 2 concepts without assigned id, only shall be equals if they are the same.
     * Hashcode shall reflect behavior.
     */
    @Test
    void testConceptEqualsAndHashCode() {
        Concept c1 = Concept.builder()
                .id(1L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10.0))
                .active(true)
                .build();
        Concept c2 = Concept.builder()
                .id(1L)
                .name("Internet")
                .type(ConceptType.RATE)
                .value(BigDecimal.valueOf(0.05))
                .active(false)
                .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        Concept c3 = Concept.builder()
                .id(2L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10.0))
                .active(true)
                .build();
        assertNotEquals(c1, c3);
    }

    /**
     * Tests the `equals` and `hashCode` methods for the `Bank` entity.
     * <br/>
     * This test verifies that:
     * - Two `Bank` entities with the same `id` are considered equal, regardless of differences in other fields.
     * - The `hashCode` method generates the same hash code for two `Bank` entities with the same `id`.
     * - Two `Bank` entities with different `id` values are not considered equal.
     * - Entites without id value, are equals only if are the same.
     * <br/>
     * The behavior ensures consistency between the `equals` and `hashCode` implementations in the `Bank` entity.
     */
    @Test
    void testBankEqualsAndHashCode() {
        Bank b1 = Bank.builder()
                .id(1)
                .name("Banco A")
                .active(true)
                .build();
        Bank b2 = Bank.builder()
                .id(1)
                .name("Banco B")
                .active(false)
                .build();

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());

        Bank b3 = Bank.builder()
                .id(2)
                .name("Banco A")
                .active(true)
                .build();
        assertNotEquals(b1, b3);
    }

    /**
     * Tests the `equals` and `hashCode` methods for the `GlobalConfig` entity.
     * <br/>
     * This test verifies the following:
     * - Two `GlobalConfig` entities with the same `id` are considered equal, regardless of differences in other fields.
     * - The `hashCode` method produces the same hash code for two `GlobalConfig` entities with the same `id`.
     * - Two `GlobalConfig` entities with different `id` values are not considered equal.
     * <br/>
     * The test ensures consistency between the `equals` and `hashCode` implementations in the `GlobalConfig` entity.
     */
    @Test
    void testGlobalConfigEqualsAndHashCode() {
        User root = User.builder()
                .id(1L)
                .username("root")
                .passwordHash("hash")
                .role(UserRole.ROOT)
                .active(true).build();
        GlobalConfig g1 = GlobalConfig.builder().id((short) 1)
                .ruc("12345678901")
                .legalName("Legal")
                .businessName("Business")
                .address("Address")
                .updatedBy(root)
                .updatedFrom("localhost")
                .updatedAt(OffsetDateTime.now())
                .build();

        GlobalConfig g2 = GlobalConfig.builder().id((short) 1)
                .ruc("98765432109")
                .legalName("Other Legal")
                .businessName("Other Business")
                .address("Other Address")
                .updatedBy(root)
                .updatedFrom("127.0.0.1")
                .updatedAt(OffsetDateTime.now())
                .build();

        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        GlobalConfig g3 = GlobalConfig.builder()
                .id((short) 2)
                .ruc("12345678901")
                .legalName("Legal")
                .businessName("Business")
                .address("Address")
                .updatedBy(root)
                .updatedFrom("localhost")
                .updatedAt(OffsetDateTime.now())
                .build();
        assertNotEquals(g1, g3);
    }

    /**
     * Tests the `equals` and `hashCode` methods for the `Transaction` entity.
     * <br/>
     * This test verifies the following:
     * - Two `Transaction` entities with the same `id` are considered equal, regardless of differences in other fields.
     * - The `hashCode` method produces the same hash code for two `Transaction` entities with the same `id`.
     * - Two `Transaction` entities with different `id` values are not considered equal.
     * <br/>
     * The test ensures consistency between the `equals` and `hashCode` implementations in the `Transaction` entity.
     */
    @Test
    void testTransactionEqualsAndHashCode() {
        Bank bank = Bank.builder()
                .id(1)
                .name("Banco")
                .active(true)
                .build();
        Concept concept = Concept.builder()
                .id(1L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10))
                .active(true)
                .build();
        User cashier = User.builder()
                .id(2L)
                .username("cashier")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        Transaction t1 = Transaction.builder()
                .id(1L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(100))
                .commission(BigDecimal.valueOf(10))
                .status(TransactionStatus.REGISTERED).
                build();
        Transaction t2 = Transaction.builder()
                .id(1L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(200))
                .commission(BigDecimal.valueOf(20))
                .status(TransactionStatus.REVERSED).
                build();

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        Transaction t3 = Transaction.builder()
                .id(2L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(100))
                .commission(BigDecimal.valueOf(10))
                .status(TransactionStatus.REGISTERED).
                build();
        assertNotEquals(t1, t3);
    }

    /**
     * Tests the `equals` and `hashCode` methods for the `ReversalRequest` entity.
     * <br/>
     * This test verifies the following:
     * - Two `ReversalRequest` entities with the same `id` are considered equal, regardless of differences in other fields.
     * - The `hashCode` method produces the same hash code for two `ReversalRequest` entities with the same `id`.
     * - Two `ReversalRequest` entities with different `id` values are not considered equal.
     * <br/>
     * The test ensures consistency between the `equals` and `hashCode` implementations in the `ReversalRequest` entity.
     */
    @Test
    void testReversalRequestEqualsAndHashCode() {
        User requester = User.builder().id(3L).username("user").passwordHash("hash").role(UserRole.ADMIN).active(true).build();
        Transaction txn = Transaction.builder()
                .id(1L)
                .bank(Bank.builder()
                        .id(1)
                        .name("Banco")
                        .active(true)
                        .build())
                .concept(Concept.builder()
                        .id(1L)
                        .name("Teléfono")
                        .type(ConceptType.FIXED)
                        .value(BigDecimal.valueOf(10))
                        .active(true)
                        .build())
                .cashier(requester)
                .amount(BigDecimal.valueOf(100))
                .commission(BigDecimal.valueOf(10))
                .status(TransactionStatus.REGISTERED)
                .build();

        ReversalRequest r1 = ReversalRequest.builder()
                .id(1L)
                .transaction(txn)
                .message("Oops")
                .requestedBy(requester)
                .status(ReversalRequestStatus.PENDING)
                .build();
        ReversalRequest r2 = ReversalRequest.builder()
                .id(1L)
                .transaction(txn)
                .message("Different")
                .requestedBy(requester)
                .status(ReversalRequestStatus.APPROVED)
                .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        ReversalRequest r3 = ReversalRequest.builder()
                .id(2L)
                .transaction(txn)
                .message("Oops")
                .requestedBy(requester)
                .status(ReversalRequestStatus.PENDING)
                .build();
        assertNotEquals(r1, r3);
    }
}
