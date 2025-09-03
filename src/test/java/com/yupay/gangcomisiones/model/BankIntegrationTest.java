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
import com.yupay.gangcomisiones.AppContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test class for {@link Bank} JPA entity.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankIntegrationTest extends AbstractPostgreIntegrationTest {
    /**
     * The entity manager object.
     */
    private EntityManager em;

    /**
     * Sets up the test environment, and cleans tables.
     */
    @BeforeEach
    void setUp() {
        em = AppContext.getInstance().getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        // Cleans the table.
        em.createNativeQuery("TRUNCATE TABLE public.bank CASCADE").executeUpdate();
        em.getTransaction().commit();
    }

    /**
     * Rollsback and releases entity manager object.
     */
    @AfterEach
    void tearDown() {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
        em.close();
    }

    /**
     * Tests that a valid bank is persisted.
     */
    @Test
    void persistValidBank_shouldPass() {
        em.getTransaction().begin();
        Bank bank = Bank.builder()
                .name("BCP")
                .active(true)
                .build();
        em.persist(bank);
        em.flush();
        em.getTransaction().commit();

        assertNotNull(bank.getId());
    }

    /**
     * Tests that a bank with null name cannot be persisted.
     */
    @Test
    void persistBankWithNullName_shouldFail() {
        em.getTransaction().begin();
        Bank bank = Bank.builder()
                .name(null)
                .active(true)
                .build();

        em.persist(bank);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a bank with empty name fails.
     */
    @Test
    void persistBankWithEmptyName_shouldFail() {
        em.getTransaction().begin();
        Bank bank = Bank.builder()
                .name("")
                .active(true)
                .build();

        em.persist(bank);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }

    /**
     * Tests that a bank with null active status fails.
     */
    @Test
    void persistBankWithNullActive_shouldFail() {
        em.getTransaction().begin();
        Bank bank = Bank.builder()
                .name("BCP")
                .active(null)
                .build();

        em.persist(bank);
        assertThrows(PersistenceException.class, em::flush);
        em.getTransaction().rollback();
    }
}

