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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

/**
 * Integration tests for the {@code GlobalConfig} entity, focusing on validation and persistence requirements.
 * This test class ensures that the {@code GlobalConfig} entity adheres to design constraints when persisted
 * to the database.
 * <br/>
 * The following are the test cases included in this integration test:
 * <ul>
 *     <li>Validation of {@code id} field constraints.</li>
 *     <li>Validation of {@code ruc} format and length constraints.</li>
 *     <li>Validation of mandatory fields against {@code null} values.</li>
 * </ul>
 * <br/>
 * Utilizes {@code AbstractPostgreIntegrationTest} as the base test class for shared setup and database handling.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution Note: </strong> tested-by dvidal@infoyupay.com passed 7 in 2.026s at 2025-09-25 23:31 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class GlobalConfigIntegrationTest extends AbstractPostgreIntegrationTest {

    /**
     * Initialize the EntityManager.
     */
    @BeforeEach
    void setup() {
        TestPersistedEntities.clean(ctx.getEntityManagerFactory());
    }

    /**
     * Close the EntityManager.
     */
    @AfterEach
    void cleanup() {

    }

    /**
     * Build a valid GlobalConfig object builder.
     *
     * @return the builder.
     */
    private GlobalConfig.GlobalConfigBuilder buildValidConfig() {
        return GlobalConfig
                .builder()
                .id((short) 1)
                .ruc("12345678901")
                .legalName("Empresa Legal SAC")
                .businessName("Empresa Comercial")
                .address("Av. Principal 123")
                .updatedFrom("mylaptop");
    }

    /**
     * Tests that persisting GlobalConfig with id!=1 fails.
     */
    @Test
    void persistConfig_withIdNotOne_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "chk_id_one",
                em -> {
                    var root = TestPersistedEntities.persistRootUser(em);
                    em.persist(buildValidConfig().id((short) 2).updatedBy(root).build());
                });
    }

    /**
     * Tests that persisting GlobalConfig with invalid RUC length fails.
     */
    @Test
    void persistConfig_withInvalidRucLength_shouldFail() {
        performAndExpectFlushFailure(SQLException.class,
                "chk_ruc_digits",
                em -> {
                    var root = TestPersistedEntities.persistRootUser(em);
                    em.persist(buildValidConfig().ruc("123").updatedBy(root).build());
                });
    }

    /**
     * Tests that persisting GlobalConfig with mandatory fields null, fails.
     *
     * @param field the field name to test.
     */
    @ParameterizedTest
    @ValueSource(strings = {"legalName", "businessName", "address", "updatedBy", "updatedFrom"})
    void persistConfig_withNullMandatoryField_shouldFail(@NotNull String field) {
        var builder = buildValidConfig();
        System.out.println("Nullifying field " + field);
        var ref = new Object() {
            boolean nullifyUpdatedBy;
        };
        switch (field) {
            case "legalName" -> builder.legalName(null);
            case "businessName" -> builder.businessName(null);
            case "address" -> builder.address(null);
            case "updatedBy" -> ref.nullifyUpdatedBy = true;
            case "updatedFrom" -> builder.updatedFrom(null);
        }

        performAndExpectFlushFailure(SQLException.class,
                "null",
                em -> {
                    var root = TestPersistedEntities.persistRootUser(em);
                    em.persist(builder.updatedBy(ref.nullifyUpdatedBy ? null : root).build());
                });
    }
}
