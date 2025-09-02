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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The integration test for Transaction. It tests:
 * <ul>
 *     <li>A valid transaction is persisted.</li>
 *     <li>Fail when mandatory fields are null.</li>
 *     <li>Fails when amount is not positive.</li>
 *     <li>Fails when commission is negative.</li>
 *     <li>Equals and hashcode works based on id.</li>
 * </ul>
 * <span><strong>Test-Run:</strong> dvidal ran 10 tests in 1.83. All passed.</span>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class TransactionIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * Before each test truncates all involved tables.
     */
    @BeforeEach
    void cleanTables() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }
    /**
     * Verifies that a valid transaction can be successfully persisted into the database.
     * <br/>
     * This method creates a valid transaction entity using the {@code buildValidTransaction}
     * helper method. It then persists the transaction to the database, commits the transaction,
     * and refreshes the entity to ensure all automatically generated or updated fields
     * (such as ID and timestamp) are properly set.
     * <br/>
     * The following assertions are performed to validate the persistence process:
     * <ol>
     * <li>
     *    The ID of the transaction should not be null, indicating that it was successfully
     *    saved and a primary key was generated.
     *    </li>
     * <li>
     *    The timestamp field {@code moment} should not be null, verifying that any required
     *    auto-generation logic for this field was applied.
     *    </li>
     * </ol>
     * This is an integration test that interacts with the database via JPA's {@code EntityManager}.
     * The method ensures that the persistence layer correctly handles valid transaction data.
     */
    @Test
    void shouldPersistValidTransaction() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();
            var tx = TestPersistedEntities.buildValidTansaction(em);
            em.persist(tx);
            et.commit();
            em.refresh(tx);

            assertNotNull(tx.getId(), "ID should be generated");
            assertNotNull(tx.getMoment(), "Moment should be auto-set");
        }
    }

    /**
     * Tests that attempting to persist a transaction with a null value in the specified field
     * results in a failure. Based on the provided field name, the corresponding setter method
     * of the transaction entity is invoked with a null value, and a commit failure is expected.
     *
     * @param field the name of the transaction field to be set to null. Acceptable values are:
     *              "bank", "concept", "cashier", "amount", "commission", and "status".
     */
    @ParameterizedTest
    @ValueSource(strings = {"bank", "concept", "cashier", "amount", "commission", "status"})
    void shouldFailWhenNull(@NotNull String field) {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();
            var tx = TestPersistedEntities.buildValidTansaction(em);
            switch (field) {
                case "bank" -> tx.setBank(null);
                case "concept" -> tx.setConcept(null);
                case "cashier" -> tx.setCashier(null);
                case "amount" -> tx.setAmount(null);
                case "commission" -> tx.setCommission(null);
                case "status" -> tx.setStatus(null);
            }
            em.persist(tx);
            expectCommitFailure(et);
        }
    }

    /**
     * Tests that persisting a transaction with a non-positive amount (e.g., zero or negative)
     * results in a commit failure due to database constraints or validation rules.
     * <br>
     * This method uses the {@code buildValidTransaction} helper to create a valid transaction,
     * modifies the transaction's amount to a non-positive value (e.g., zero),
     * persists the transaction, and asserts that the database rejects the commit.
     * <br>
     * A failure is expected during the commit operation, and the {@code expectCommitFailure}
     * helper method is used to verify that the exception thrown is related to persistence
     * issues (e.g., {@link IllegalStateException} or {@link PersistenceException}).
     * <br>
     * The test ensures that the persistence layer correctly enforces constraints
     * or validation rules for the {@code amount} field.
     */
    @Test
    void shouldFailWhenAmountIsNonPositive() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();
            var tx = TestPersistedEntities.buildValidTansaction(em);
            tx.setAmount(BigDecimal.ZERO);
            em.persist(tx);
            expectCommitFailure(et);
        }
    }

    /**
     * Tests that attempting to persist a transaction with a negative commission
     * results in a commit failure due to database constraints or validation rules.
     * <br/>
     * This method creates a valid transaction using the {@code buildValidTransaction}
     * helper method and modifies the transaction's commission field to a negative
     * value. The transaction is then persisted, and the commit operation is performed.
     * <br/>
     * A failure is expected during the commit process, indicating that the persistence
     * layer enforces validation or constraints on the commission field. The
     * {@code expectCommitFailure} method is used to ensure that the resulting exception
     * is related to persistence issues (such as {@link IllegalStateException} or
     * {@link PersistenceException}).
     */
    @Test
    void shouldFailWhenCommissionIsNegative() {
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            var et = em.getTransaction();
            et.begin();
            var tx = TestPersistedEntities.buildValidTansaction(em);
            tx.setCommission(new BigDecimal("-1.00"));
            em.persist(tx);
            expectCommitFailure(et);
        }
    }

    /**
     * Validates the correctness of the {@code equals()} and {@code hashCode()} implementations
     * for the {@code Transaction} entity, ensuring that they operate based solely on the {@code id} field.
     * <br/>
     * This test performs the following verifications:
     * <ul>
     * <li>
     *    Two {@code Transaction} objects with the same {@code id} are considered equal and have the same hash code.
     * </li>
     * <li>
     *    Two {@code Transaction} objects with different {@code ids} are not considered equal.
     * </li>
     * <li>
     *    Two {@code Transaction} objects without an {@code id} are not considered equal, ensuring that
     *   uninitialized entities behave as distinct.
     *   </li>
     * <li>
     *    The reflexive property of equality is validated for a single {@code Transaction} object.
     * </li>
     * </ul>
     * This test is important to confirm the entity's compliance with the required contract for
     * {@code equals()} and {@code hashCode()}, which is critical for the behavior of collections
     * such as {@code Set} and map keys.
     */
    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsAndHashCodeShouldWorkBasedOnId() {
        var tx1 = Transaction.builder().id(1L).build();

        var tx2 = Transaction.builder().id(1L).build();

        var tx3 = Transaction.builder().id(2L).build();

        var txNull1 = new Transaction();
        var txNull2 = new Transaction();

        assertEquals(tx1, tx2);
        assertEquals(tx1.hashCode(), tx2.hashCode());

        assertNotEquals(tx1, tx3);

        // Two entities without ID should not be equal
        assertNotEquals(txNull1, txNull2);

        // Reflexive property
        assertEquals(tx1, tx1);
    }
}
