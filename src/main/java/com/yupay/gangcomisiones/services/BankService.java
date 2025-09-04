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

import com.yupay.gangcomisiones.model.Bank;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Services for {@link Bank} entities.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface BankService {
    /**
     * Lists all banks persisted in the database.
     *
     * @return a future that completes with the list of all banks.
     */
    CompletableFuture<List<Bank>> listAllBanks();

    /**
     * Lists all banks with active flag set to true.
     *
     * @return a future that completes with the list of all active banks.
     */
    CompletableFuture<List<Bank>> listAllActiveBanks();

    /**
     * Creates a new bank with the provided name. The bank will be created with the active flag set to true.
     * The database will assign the identifier using its sequence.
     *
     * @param name the bank name to assign.
     * @return a future that completes with the persisted {@link Bank} entity (with id populated).
     */
    CompletableFuture<Bank> createBank(@NotNull String name);

    /**
     * Updates the basic information (name and active flag) of an existing bank.
     * It uses {@code EntityManager.getReference} to obtain a managed instance and sets the new values.
     * Changes are flushed on transaction commit.
     *
     * @param id     the identifier of the bank to update.
     * @param name   the new name to set (must be non-null).
     * @param active the new active flag to set (must be non-null).
     * @return a future that completes when the update has been committed.
     */
    CompletableFuture<Void> updateBank(int id, @NotNull String name, Boolean active);
}
