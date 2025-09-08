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

package com.yupay.gangcomisiones.usecase.createuser;

import com.yupay.gangcomisiones.model.UserRole;

/**
 * Represents a Data Transfer Object (DTO) for creating a user in the system.
 * <br/>
 * This record encapsulates information necessary for user creation, including
 * a username, password, and associated role.
 * <br/>
 * The {@code role} is expected to be one of the predefined user roles defined
 * by the {@code UserRole} enum.
 * <br/>
 * This class is intended to be immutable and only used for transferring user creation
 * data between different layers of the application.
 *
 * @param username the username to be associated with the new user. It is expected to be unique.
 * @param password the password for the new user. It should be stored in a secured, hashed format.
 * @param role     the role assigned to the new user. Indicates their privileges within the system.
 * @author InfoYupay SACS
 * @version 1.0
 */
public record CreateUserDTO(String username,
                            String password,
                            UserRole role) {
}
