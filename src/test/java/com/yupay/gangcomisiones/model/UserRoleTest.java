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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@code UserRole} enumeration. These tests validate the behavior of
 * the {@code isAtLeast} method in determining the privilege hierarchy among different
 * user roles within the system.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class UserRoleTest {
    /// Tests the behavior of the `isAtLeast` method when invoked with the `ROOT` user role.
    /// This test ensures that the `ROOT` role is recognized as having a privilege level
    /// equal to or higher than all other user roles in the system.
    /// The test checks the following scenarios:
    /// - `ROOT` is at least `ADMIN`.
    /// - `ROOT` is at least `CASHIER`.
    /// - `ROOT` is at least `ROOT`.
    /// Expected results:
    /// - All assertions must pass, verifying that the `ROOT` role has the highest privilege level.
    @Test
    void roleROOTisAtLeastAnybody(){
        assertTrue(UserRole.ROOT.isAtLeast(UserRole.ADMIN));
        assertTrue(UserRole.ROOT.isAtLeast(UserRole.CASHIER));
        assertTrue(UserRole.ROOT.isAtLeast(UserRole.ROOT));
    }

    /// Tests the behavior of the `isAtLeast` method for the `ADMIN` user role when
    /// compared against other roles. It ensures that `ADMIN` has the expected privilege
    /// level relative to other roles in the system.
    /// This test verifies the following scenarios:
    /// - `ADMIN` is at least itself (`ADMIN`).
    /// - `ADMIN` is at least `CASHIER`.
    /// - `ADMIN` is not at least `ROOT`.
    /// Expected results:
    /// - The assertions confirm that `ADMIN` can access privileges of
    ///   `CASHIER` and itself but does not have sufficient privileges
    ///   to act as or supersede `ROOT`.
    @Test
    void roleADMINisNotAtLeastRoot(){
        assertTrue(UserRole.ADMIN.isAtLeast(UserRole.ADMIN));
        assertTrue(UserRole.ADMIN.isAtLeast(UserRole.CASHIER));
        assertFalse(UserRole.ADMIN.isAtLeast(UserRole.ROOT));
    }

    /// Tests the behavior of the `isAtLeast` method for the `UserRole.CASHIER` role.
    /// This test ensures the correct privilege hierarchy when comparing the `CASHIER` role
    /// against other roles in the system.
    /// Verifies the following scenarios:
    /// - `CASHIER` is at least itself (`CASHIER`).
    /// - `CASHIER` is not at least `ADMIN`.
    /// - `CASHIER` is not at least `ROOT`.
    /// Expected results:
    /// - `CASHIER.isAtLeast(CASHIER)` returns `true`.
    /// - `CASHIER.isAtLeast(ADMIN)` returns `false`.
    /// - `CASHIER.isAtLeast(ROOT)` returns `false`.
    @Test
    void roleCASHIERisAtLeastCASHIER(){
        assertTrue(UserRole.CASHIER.isAtLeast(UserRole.CASHIER));
        assertFalse(UserRole.CASHIER.isAtLeast(UserRole.ADMIN));
        assertFalse(UserRole.CASHIER.isAtLeast(UserRole.ROOT));
    }

    /// Tests the behavior of the `isAtLeast` method when the provided role is `null`.
    /// This test ensures that no user role, including `ROOT`, `ADMIN`, or `CASHIER`,
    /// satisfies the condition of having at least the privilege level of `null`.
    /// Verifies the following scenarios:
    /// - `ROOT.isAtLeast(null)` returns `false`.
    /// - `ADMIN.isAtLeast(null)` returns `false`.
    /// - `CASHIER.isAtLeast(null)` returns `false`.
    /// Expected results:
    /// - All assertions confirm that when the `role` parameter is `null`, the `isAtLeast` method
    ///   correctly returns `false` for all user roles.
    @Test
    void nobodyIsAtLeastNull(){
        assertFalse(UserRole.ROOT.isAtLeast(null));
        assertFalse(UserRole.ADMIN.isAtLeast(null));
        assertFalse(UserRole.CASHIER.isAtLeast(null));
    }
}
