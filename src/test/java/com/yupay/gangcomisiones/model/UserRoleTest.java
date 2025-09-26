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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UserRole} enumeration, specifically validating
 * the hierarchy and privilege comparisons among different user roles.
 * <br/>
 * This class ensures that the {@code isAtLeast} method in {@link UserRole} behaves as expected
 * when comparing privilege levels of various roles or against {@code null}.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution Note:</strong> tested-by dvidal@infoyupay.com passed 1 in 0.885s at 2025-09-25 23:40 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UserRoleTest {

    /**
     * Validates the hierarchy of user roles to ensure that the privilege levels follow the expected ordering.
     * <br/>
     * The method performs soft assertions to verify comparisons between different user roles based on their privilege levels.
     * It utilizes the {@code isAtLeast} method of {@link UserRole} to compare the current role with others.
     * <br/>
     * The validation includes:
     * <ul>
     *     <li>Checking that roles with higher privilege levels evaluate as greater than or equal to roles with lower privilege levels.</li>
     *     <li>Ensuring that a role is always greater than or equal to itself.</li>
     *     <li>Verifying that comparisons with {@code null} always return {@code false}.</li>
     * </ul>
     * <br/>
     * Assertions for each role:
     * <ol>
     *     <li><b>ROOT</b>:
     *     <ul>
     *         <li>ROOT &gt;= ROOT: true</li>
     *         <li>ROOT &gt;= ADMIN: true</li>
     *         <li>ROOT &gt;= CASHIER: true</li>
     *         <li>ROOT &gt;= null: false</li>
     *     </ul>
     *     </li>
     *     <li><b>ADMIN</b>:
     *     <ul>
     *         <li>ADMIN &gt;= ROOT: false</li>
     *         <li>ADMIN &gt;= ADMIN: true</li>
     *         <li>ADMIN &gt;= CASHIER: true</li>
     *         <li>ADMIN &gt;= null: false</li>
     *     </ul>
     *     </li>
     *     <li><b>CASHIER</b>:
     *     <ul>
     *         <li>CASHIER &gt;= ROOT: false</li>
     *         <li>CASHIER &gt;= ADMIN: false</li>
     *         <li>CASHIER &gt;= CASHIER: true</li>
     *         <li>CASHIER &gt;= null: false</li>
     *     </ul>
     *     </li>
     * </ol>
     * <br/>
     * This test ensures that the role hierarchy behaves correctly, maintaining logical integrity between role comparisons.
     */
    @SuppressWarnings("ConstantValue")
    @Test
    void validateRoleHierarchy() {
        SoftAssertions.assertSoftly(softly -> {
            for (var role : UserRole.values()) {
                switch (role) {
                    case ROOT -> {
                        softly.assertThat(role.isAtLeast(UserRole.ROOT))
                                .as("ROOT >= ROOT").isTrue();
                        softly.assertThat(role.isAtLeast(UserRole.ADMIN))
                                .as("ROOT >= ADMIN").isTrue();
                        softly.assertThat(role.isAtLeast(UserRole.CASHIER))
                                .as("ROOT >= CASHIER").isTrue();
                        softly.assertThat(role.isAtLeast(null))
                                .as("ROOT >= null").isFalse();
                    }
                    case ADMIN -> {
                        softly.assertThat(role.isAtLeast(UserRole.ROOT))
                                .as("ADMIN >= ROOT").isFalse();
                        softly.assertThat(role.isAtLeast(UserRole.ADMIN))
                                .as("ADMIN >= ADMIN").isTrue();
                        softly.assertThat(role.isAtLeast(UserRole.CASHIER))
                                .as("ADMIN >= CASHIER").isTrue();
                        softly.assertThat(role.isAtLeast(null))
                                .as("ADMIN >= null").isFalse();
                    }
                    case CASHIER -> {
                        softly.assertThat(role.isAtLeast(UserRole.ROOT))
                                .as("CASHIER >= ROOT").isFalse();
                        softly.assertThat(role.isAtLeast(UserRole.ADMIN))
                                .as("CASHIER >= ADMIN").isFalse();
                        softly.assertThat(role.isAtLeast(UserRole.CASHIER))
                                .as("CASHIER >= CASHIER").isTrue();
                        softly.assertThat(role.isAtLeast(null))
                                .as("CASHIER >= null").isFalse();
                    }
                }
            }
        });
    }
}
