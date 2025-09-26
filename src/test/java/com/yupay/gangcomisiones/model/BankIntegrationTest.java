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

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Integration tests for Bank entity to ensure correct persistence behavior and database constraints.
 * <br/>
 * <br/>
 * These tests validate the following scenarios concerning the Bank entity:
 * <ul>
 *     <li>Persistence of valid bank instances.</li>
 *     <li>Failure scenarios for invalid bank instances due to application of business constraints
 *         and database constraints such as null values, uniqueness, and emptiness.</li>
 * </ul>
 * <br/>
 * <br/>
 * Each test interacts with the database to validate data consistency and conformance to defined constraints,
 * ensuring the integrity of the Bank entity's persistence logic.
 * <br/>
 * <br/>
 * Test cases include:
 * <ol>
 *     <li><b>persistValidBank_shouldPass</b>: Verifies that a bank entity with valid data is persisted successfully.</li>
 *     <li><b>persistBankWithNullName_shouldFail</b>: Ensures that a bank cannot be persisted if its name is null.</li>
 *     <li><b>persistBankWithEmptyName_shouldFail</b>: Ensures that a bank cannot be persisted if its name is empty.</li>
 *     <li><b>persistBankWithNullActive_shouldFail</b>: Verifies that a bank with a null active status cannot be saved.</li>
 *     <li><b>persistBankWithDuplicateName_shouldFail</b>: Validates that multiple banks with the same name cannot coexist in the database due to unique constraints.</li>
 *     <li><b>mergeBankWithDuplicateName_shouldFail</b>: Tests that updating an existing bank to have the same name as another bank fails
 *         due to the unique constraint on the "name" attribute.</li>
 * </ol>
 * <br/>
 * These tests leverage transactional setups and teardown mechanisms to ensure database consistency,
 * as well as the application of defined checks on the Bank entity.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px;">
 *     <strong>Execution Note:</strong> Tested by dvidal@infoyupay.com passed 6 in 937 ms at 2025-09-25 21:57 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
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
        var bank = performInTransaction(em -> {
            var r = Bank.builder()
                    .name("BCP")
                    .active(true)
                    .build();
            em.persist(r);
            return r;
        });
        assertThat(bank.getId())
                .describedAs("Bank ID must be positive and non-null, found {}", bank.getId())
                .isNotNull()
                .isGreaterThan(0);
    }

    /**
     * Tests that a bank with null name cannot be persisted.
     */
    @Test
    void persistBankWithNullName_shouldFail() {
        //Arrange
        var bank = Bank.builder()
                .name(null)
                .active(true)
                .build();
        //Act and assert
        performAndExpectFlushFailure(SQLException.class,
                "null, t)",
                em -> em.persist(bank));

    }

    /**
     * Tests that a bank with empty name fails.
     */
    @Test
    void persistBankWithEmptyName_shouldFail() {
        //Arrange
        var bank = Bank.builder()
                .name("")
                .active(true)
                .build();
        //Act and assert
        performAndExpectFlushFailure(SQLException.class,
                "chk_bank_name_nonempty",
                em -> em.persist(bank));
    }

    /**
     * Tests that a bank with null active status fails.
     */
    @Test
    void persistBankWithNullActive_shouldFail() {
        //Arrange
        var bank = Bank.builder()
                .name("BCP")
                .active(null)
                .build();
        //Act and assert
        performAndExpectFlushFailure(SQLException.class,
                "BCP, null)",
                em -> em.persist(bank));
    }

    /**
     * Tests that two banks with same name fails.
     */
    @Test
    void persistBankWithDuplicateName_shouldFail() {
        //Arrange
        //Given 2 banks with same name.
        var bank1 = Bank.builder()
                .name("BCP")
                .active(true)
                .build();
        var bank2 = Bank.builder()
                .name("BCP")
                .active(true)
                .build();
        //Act and assert
        performAndExpectFlushFailure(SQLException.class,
                "uq_bank_name",
                em -> {
                    em.persist(bank1);
                    em.persist(bank2);
                });
    }

    /**
     * Tests that updates with duplicated name will fail.
     * Ie: {@code "bank1":{"name" : "BCP"}} and {@code "bank2" : {"name" : "IBK"}}, then changing
     * bank2.name to BCP should fail due unique constraint on name.
     */
    @Test
    void mergeBankWithDuplicateName_shouldFail() {
        //Arrange
        var bank1 = Bank.builder()
                .name("BCP")
                .active(true)
                .build();
        var bank2 = Bank.builder()
                .name("IBK")
                .active(true)
                .build();
        runInTransaction(em -> {
            em.persist(bank1);
            em.persist(bank2);
        });
        //Act and assert
        performAndExpectFlushFailure(SQLException.class,
                "uq_bank_name",
                em -> {
                    var fresh = em.getReference(Bank.class, bank2.getId());
                    fresh.setName(bank1.getName());
                });
    }
}

