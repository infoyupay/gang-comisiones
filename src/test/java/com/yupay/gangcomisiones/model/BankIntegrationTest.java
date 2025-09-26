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
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test class for {@link Bank} JPA entity.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankIntegrationTest extends AbstractPostgreIntegrationTest {

    /**
     * Sets up the test environment, and cleans tables.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Tests that a valid bank is persisted.
     */
    @Test
    void persistValidBank_shouldPass() {
        var bank = performInTransaction(ctx, em -> {
            var r = Bank.builder()
                    .name("BCP")
                    .active(true)
                    .build();
            em.persist(r);
            return r;
        });
        assertNotNull(bank.getId());
    }

    /**
     * Tests that a bank with null name cannot be persisted.
     */
    @Test
    void persistBankWithNullName_shouldFail() {

        EntityTransaction et = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            var bank = Bank.builder()
                    .name(null)
                    .active(true)
                    .build();

            em.persist(bank);
            expectCommitFailure(et);
        } catch (Exception e) {
            if (et != null && et.isActive()) et.rollback();
            fail(e);
        }
    }

    /**
     * Tests that a bank with empty name fails.
     */
    @Test
    void persistBankWithEmptyName_shouldFail() {
        EntityTransaction et = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            var bank = Bank.builder()
                    .name("")
                    .active(true)
                    .build();

            em.persist(bank);
            expectCommitFailure(et);
        } catch (Exception e) {
            if (et != null && et.isActive()) et.rollback();
            fail(e);
        }
    }

    /**
     * Tests that a bank with null active status fails.
     */
    @Test
    void persistBankWithNullActive_shouldFail() {
        EntityTransaction et = null;
        try (var em = ctx.getEntityManagerFactory().createEntityManager()) {
            et = em.getTransaction();
            et.begin();
            var bank = Bank.builder()
                    .name("BCP")
                    .active(null)
                    .build();

            em.persist(bank);
            expectCommitFailure(et);
        } catch (Exception e) {
            if (et != null && et.isActive()) et.rollback();
            fail(e);
        }
    }
}

