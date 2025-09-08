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

package com.yupay.gangcomisiones.services;

import com.yupay.gangcomisiones.AbstractPostgreIntegrationTest;
import com.yupay.gangcomisiones.exceptions.AppSecurityException;
import com.yupay.gangcomisiones.exceptions.PersistenceServicesException;
import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.model.TestPersistedEntities;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BankService} operations.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class BankServiceIntegrationTest extends AbstractPostgreIntegrationTest {

    private BankService bankService;

    /**
     * Sets up the test environment before each test method execution.
     * <br/>
     * This method resets the database state by truncating all relevant tables,
     * ensuring no persisted entities interfere with subsequent tests. It achieves
     * this through the {@link TestPersistedEntities#clean(EntityManagerFactory)} method.
     * <br/>
     * Additionally, it initializes the {@code BankService} instance used in the tests
     * by accessing it through the application context.
     */
    @BeforeEach
    void setUp() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
        bankService = ctx.getBankService();
    }


    /**
     * Verifies that a bank is persisted with an auto-generated id and active=true by default.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testCreateBank_AssignsIdAndActiveTrue() throws Exception {
        // when
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("Interbank").get();

        // then
        assertNotNull(bank.getId(), "Bank id must be assigned by DB sequence");
        assertTrue(bank.getId() > 0, "Bank id must be positive");
        assertEquals(Boolean.TRUE, bank.getActive(), "Bank must be active after creation");

        // and it should appear in listAll and listAllActive
        List<Bank> all = bankService.listAllBanks().get();
        assertTrue(all.stream().anyMatch(b -> b.getId().equals(bank.getId())));
        List<Bank> active = bankService.listAllActiveBanks().get();
        assertTrue(active.stream().anyMatch(b -> b.getId().equals(bank.getId())));
    }

    /**
     * Verifies that trying to create a bank when the logged-in user is
     * not an admin fails with a {@link PersistenceServicesException}.
     */
    @Test
    void testCreateBank_UnprivilegedUserFails() {
        UserSessionHelpers.createAndLogCashierUser();

        var ex = assertThrows(ExecutionException.class, bankService.createBank("One Bank")::get);
        assertInstanceOf(AppSecurityException.class, ex.getCause());
    }

    /**
     * Verifies that trying to update a bank when the logged-in user is
     * not an admin fails with a {@link PersistenceServicesException}.
     */
    @Test
    void testUpdateeBank_UnprivilegedUserFails() {
        UserSessionHelpers.createAndLogCashierUser();

        var ex = assertThrows(ExecutionException.class,
                bankService.updateBank(1, "One Bank", false)::get);
        assertInstanceOf(AppSecurityException.class, ex.getCause());
    }

    /**
     * Verifies that creating a bank with a null name fails with a {@link PersistenceException}.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testCreateBank_NullNameFails() {
        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> bankService.createBank(null).get());
        assertInstanceOf(PersistenceException.class, ex.getCause());
    }

    /**
     * Verifies that updating a bank with a null name or null active flag fails due to not-null constraints.
     *
     * @throws Exception if an error occurs during the test.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testUpdateBank_NullFieldsFail() throws Exception {
        UserSessionHelpers.createAndLogAdminUser();
        Bank bank = bankService.createBank("BCP").get();

        // null name
        ExecutionException ex1 = assertThrows(ExecutionException.class,
                () -> bankService.updateBank(bank.getId(), null, true).get());
        assertInstanceOf(PersistenceException.class, ex1.getCause());

        // null active
        ExecutionException ex2 = assertThrows(ExecutionException.class,
                () -> bankService.updateBank(bank.getId(), "BCP", null).get());
        assertInstanceOf(PersistenceException.class, ex2.getCause());
    }
}
