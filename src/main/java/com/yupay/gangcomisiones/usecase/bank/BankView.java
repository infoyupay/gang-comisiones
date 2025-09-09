
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

package com.yupay.gangcomisiones.usecase.bank;

import com.yupay.gangcomisiones.model.Bank;
import com.yupay.gangcomisiones.usecase.commons.UserPrompter;
/**
 * Interface for viewing and interacting with the {@link Bank} entity
 * in the different use cases. It extends the {@link UserPrompter} interface
 * to provide standarized ways to show messages, and to interact with the user.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public interface BankView extends UserPrompter<Bank> {

}
