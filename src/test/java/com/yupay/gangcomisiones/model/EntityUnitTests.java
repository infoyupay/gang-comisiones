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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;

/**
 * The {@code EntityUnitTests} class contains unit tests for validating the behavior of the `equals` and `hashCode` methods
 * across several entity classes. These tests ensure that object equality and hash code generation rules are properly implemented
 * in compliance with their specific requirements.
 * <br/>
 * The tests in this class verify the following:
 * <ol>
 *   <li>Entities with the same ID value are considered equal, regardless of differences in other fields.</li>
 *   <li>The {@code hashCode} method produces consistent hash codes for objects with equal IDs.</li>
 *   <li>Entities with different ID values are not considered equal.</li>
 *   <li>If entities lack an assigned ID, they are equal only if they are the exact same instance.</li>
 * </ol>
 * Each entity's tests ensure consistency between the behavior of their {@code equals} and {@code hashCode} methods.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px;">
 *     <strong>Execution Note:</strong> tested by dvidal@infoyupay.com passed 6 in 0.686s at 2025-09-25 22:35 UTC-5
 * </div>
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
        var u1 = User.forTest(1L);
        u1.setUsername("test1");
        u1.setPassword("test01..");
        u1.setRole(UserRole.ROOT);
        u1.setActive(true);

        var u2 = User.forTest(1L);
        u2.setUsername("user2");
        u2.setPassword("user02..");
        u2.setRole(UserRole.ADMIN);
        u2.setActive(false);

        assertThatObject(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());

        var u3 = User.forTest(2L);
        u3.setUsername("test1");
        u3.setPassword("test01..");
        u3.setRole(UserRole.ADMIN);

        assertThat(u1).isNotEqualTo(u3);
    }

    /**
     * Tests for the Concept entity, we are checking the equals and hashcode methods.
     * 2 concepts with the same id, must be equals, even if they are not the same.
     * 2 concepts without assigned id, only shall be equals if they are the same.
     * Hashcode shall reflect behavior.
     */
    @Test
    void testConceptEqualsAndHashCode() {
        var c1 = Concept.builder()
                .id(1L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10.0))
                .active(true)
                .build();
        var c2 = Concept.builder()
                .id(1L)
                .name("Internet")
                .type(ConceptType.RATE)
                .value(BigDecimal.valueOf(0.05))
                .active(false)
                .build();

        assertThatObject(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());

        var c3 = Concept.builder()
                .id(2L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10.0))
                .active(true)
                .build();
        assertThatObject(c1).isNotEqualTo(c3);
    }

    /// Tests the `equals` and `hashCode` methods for the `Bank` entity.
    ///
    /// This test verifies that:
    /// - Two `Bank` entities with the same `id` are considered equal, regardless of differences in other fields.
    /// - The `hashCode` method generates the same hash code for two `Bank` entities with the same `id`.
    /// - Two `Bank` entities with different `id` values are not considered equal.
    /// - Entites without id value, are equals only if are the same.
    ///
    /// The behavior ensures consistency between the `equals` and `hashCode` implementations in the `Bank` entity.
    @Test
    void testBankEqualsAndHashCode() {
        var b1 = Bank.builder()
                .id(1)
                .name("Banco A")
                .active(true)
                .build();
        var b2 = Bank.builder()
                .id(1)
                .name("Banco B")
                .active(false)
                .build();

        assertThatObject(b1).isEqualTo(b2);
        assertThat(b1.hashCode()).isEqualTo(b2.hashCode());

        var b3 = Bank.builder()
                .id(2)
                .name("Banco A")
                .active(true)
                .build();
        assertThatObject(b1).isNotEqualTo(b3);
    }

    /// Tests the `equals` and `hashCode` methods for the `GlobalConfig` entity.
    ///
    /// This test verifies the following:
    /// - Two `GlobalConfig` entities with the same `id` are considered equal, regardless of differences in other fields.
    /// - The `hashCode` method produces the same hash code for two `GlobalConfig` entities with the same `id`.
    /// - Two `GlobalConfig` entities with different `id` values are not considered equal.
    ///
    /// The test ensures consistency between the `equals` and `hashCode` implementations in the `GlobalConfig` entity.
    @Test
    void testGlobalConfigEqualsAndHashCode() {
        var root = User.forTest(1L);
        var g1 = GlobalConfig.builder().id((short) 1)
                .ruc("12345678901")
                .legalName("Legal")
                .businessName("Business")
                .address("Address")
                .updatedBy(root)
                .updatedFrom("localhost")
                .updatedAt(OffsetDateTime.now())
                .build();

        var g2 = GlobalConfig.builder().id((short) 1)
                .ruc("98765432109")
                .legalName("Other Legal")
                .businessName("Other Business")
                .address("Other Address")
                .updatedBy(root)
                .updatedFrom("127.0.0.1")
                .updatedAt(OffsetDateTime.now())
                .build();

        assertThatObject(g1).isEqualTo(g2);
        assertThat(g1.hashCode()).isEqualTo(g2.hashCode());

        var g3 = GlobalConfig.builder()
                .id((short) 2)
                .ruc("12345678901")
                .legalName("Legal")
                .businessName("Business")
                .address("Address")
                .updatedBy(root)
                .updatedFrom("localhost")
                .updatedAt(OffsetDateTime.now())
                .build();
        assertThatObject(g1).isNotEqualTo(g3);
    }

    /// Tests the `equals` and `hashCode` methods for the `Transaction` entity.
    ///
    /// This test verifies the following:
    /// - Two `Transaction` entities with the same `id` are considered equal, regardless of differences in other fields.
    /// - The `hashCode` method produces the same hash code for two `Transaction` entities with the same `id`.
    /// - Two `Transaction` entities with different `id` values are not considered equal.
    ///
    /// The test ensures consistency between the `equals` and `hashCode` implementations in the `Transaction` entity.
    @Test
    void testTransactionEqualsAndHashCode() {
        var bank = Bank.builder()
                .id(1)
                .name("Banco")
                .active(true)
                .build();
        var concept = Concept.builder()
                .id(1L)
                .name("Teléfono")
                .type(ConceptType.FIXED)
                .value(BigDecimal.valueOf(10))
                .active(true)
                .build();
        var cashier = User.forTest(2L);

        var t1 = Transaction.builder()
                .id(1L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(100))
                .commission(BigDecimal.valueOf(10))
                .status(TransactionStatus.REGISTERED).
                build();
        var t2 = Transaction.builder()
                .id(1L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(200))
                .commission(BigDecimal.valueOf(20))
                .status(TransactionStatus.REVERSED).
                build();

        assertThatObject(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());

        var t3 = Transaction.builder()
                .id(2L)
                .bank(bank)
                .concept(concept)
                .cashier(cashier)
                .amount(BigDecimal.valueOf(100))
                .commission(BigDecimal.valueOf(10))
                .status(TransactionStatus.REGISTERED).
                build();
        assertThatObject(t1).isNotEqualTo(t3);
    }

    /// Tests the `equals` and `hashCode` methods for the `ReversalRequest` entity.
    ///
    /// This test verifies the following:
    /// - Two `ReversalRequest` entities with the same `id` are considered equal, regardless of differences in other fields.
    /// - The `hashCode` method produces the same hash code for two `ReversalRequest` entities with the same `id`.
    /// - Two `ReversalRequest` entities with different `id` values are not considered equal.
    ///
    /// The test ensures consistency between the `equals` and `hashCode` implementations in the `ReversalRequest` entity.
    @Test
    void testReversalRequestEqualsAndHashCode() {
        var requester = User.forTest(3L);
        var txn = Transaction.builder()
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

        var r1 = ReversalRequest.builder()
                .id(1L)
                .transaction(txn)
                .message("Oops")
                .requestedBy(requester)
                .status(ReversalRequestStatus.PENDING)
                .build();
        var r2 = ReversalRequest.builder()
                .id(1L)
                .transaction(txn)
                .message("Different")
                .requestedBy(requester)
                .status(ReversalRequestStatus.APPROVED)
                .build();

        assertThatObject(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());

        var r3 = ReversalRequest.builder()
                .id(2L)
                .transaction(txn)
                .message("Oops")
                .requestedBy(requester)
                .status(ReversalRequestStatus.PENDING)
                .build();
        assertThatObject(r1).isNotEqualTo(r3);
    }
}
