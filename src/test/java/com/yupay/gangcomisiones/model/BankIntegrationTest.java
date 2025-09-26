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
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
                "null",
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
                "null",
                em -> em.persist(bank));
    }

    /**
     * Tests that two banks with same name fails.
     */
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

    void mergeBankWithDuplicateName_shouldFail(){
        //Arrange
        var bank1 = Bank.builder()
                .name("BCP")
                .active(true)
                .build();
        var bank2 = Bank.builder()
                .name("IBK")
                .active(true)
                .build();
        runInTransaction(em->{
            em.persist(bank1);
            em.persist(bank2);
        });
    }
}

