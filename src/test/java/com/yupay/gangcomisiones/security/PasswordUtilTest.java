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

package com.yupay.gangcomisiones.security;

import com.yupay.gangcomisiones.exceptions.AppSecurityException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static com.yupay.gangcomisiones.assertions.CauseAssertions.assertExpectedCause;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
//import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for password utility class.
 * <br/>
 * <div style="border: 1px solid black; padding: 2px">
 *     <strong>Execution Note:</strong> dvidal@infoyupay.com passed 6 tests in 1.138s at 2025-09-28 21:27 UTC-5.
 * </div>
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
class PasswordUtilTest {
    /**
     * Tests the {@link PasswordUtil#generateSalt()} method to ensure the generated salt
     * meets the expected length of 16 bytes.
     * <br/>
     * The test checks:
     * <ul>
     *  <li> The generated salt is not null.</li>
     *  <li> The decoded salt (in byte array form) has a length of 16 bytes,
     *       ensuring compliance with the defined {@code SALT_LENGTH}.</li>
     * </ul>
     */
    @Test
    void testGenerateSaltLength() {
        assertThat(PasswordUtil.generateSalt())
                .isNotNull()
                .asBase64Decoded()
                .as("Salt must be 16 bytes long.")
                .hasSize(16);
    }

    /**
     * Tests the consistency of the password hashing process.
     * Ensures that hashing the same password with the same salt using {@link PasswordUtil#hashPassword(String, String)}
     * produces identical results, confirming that the hashing operation is deterministic.
     * <br/>
     * The test performs the following steps:
     * <ul>
     *  <li> Generates a cryptographic salt using {@link PasswordUtil#generateSalt()}.</li>
     *  <li> Hashes a predefined password twice using the same salt and compares the results.</li>
     * </ul>
     * Asserts:
     * <ul>
     *  <li>The two computed hash values must be equal, verifying deterministic behavior of the hashing process.</li>
     * </ul>
     */
    @Test
    void testHashConsistency() {
        var salt = PasswordUtil.generateSalt();
        var password = "SuperSecret123!";
        var hash1 = PasswordUtil.hashPassword(password, salt);
        var hash2 = PasswordUtil.hashPassword(password, salt);

        assertThat(hash1)
                .as("Hashing the same password+sañt must be deterministic.")
                .isEqualTo(hash2);
    }

    /**
     * Tests the behavior of {@link PasswordUtil#hashPassword(String, String)} when the same password is hashed
     * with different salts. Ensures that password hashes generated with distinct salts are unique,
     * affirming the importance of salt randomization in producing non-reproducible hashes.
     * <br/>
     * Test steps:
     * <ul>
     *  <li> Define a password string.</li>
     *  <li> Generate two distinct cryptographic salts using {@link PasswordUtil#generateSalt()}.</li>
     *  <li> Hash the password with each salt using {@link PasswordUtil#hashPassword(String, String)}.</li>
     *  <li> Assert that the resulting hashes are not equal.</li>
     * </ul>
     * Assertion:
     * <ul>
     *  <li> Confirms that using different salts produces different hash values for the same password.</li>
     * </ul>
     */
    @Test
    void testHashWithDifferentSalt() {
        var password = "SuperSecret123!";
        var salt1 = PasswordUtil.generateSalt();
        var salt2 = PasswordUtil.generateSalt();

        var hash1 = PasswordUtil.hashPassword(password, salt1);
        var hash2 = PasswordUtil.hashPassword(password, salt2);

        assertThat(hash1)
                .as("Hashes with different salts must differ.")
                .isNotEqualTo(hash2);
    }

    /**
     * Tests the behavior of {@link PasswordUtil#verifyPassword(String, String, String)} when the correct password is provided.
     * Ensures that the verification process succeeds when the input matches the stored hash.
     */
    @Test
    void testVerifyPasswordSuccess() {
        var password = "SuperSecret123!";
        var salt = PasswordUtil.generateSalt();
        var hash = PasswordUtil.hashPassword(password, salt);

        assertThat(PasswordUtil.verifyPassword(password, salt, hash))
                .as("Password must verify successfully.")
                .isTrue();
    }

    /**
     * Tests the behavior of {@link PasswordUtil#verifyPassword(String, String, String)} when the incorrect
     * password is provided.
     * Ensures that the verification process fails when the input does not match the stored hash.
     */
    @Test
    void testVerifyPasswordFailure() {
        var salt = PasswordUtil.generateSalt();
        var correctHash = PasswordUtil.hashPassword("SuperSecret123!", salt);

        assertThat(PasswordUtil.verifyPassword("WrongPassword!", salt, correctHash))
                .as("Verification must fail with incorrect password.")
                .isFalse();
    }

    /**
     * Tests the behavior of {@link PasswordUtil#verifyPassword(String, String, String)} when the incorrect
     * password is provided.
     * Ensures that the verification process fails when the input does not match the stored hash.
     *
     * @throws Exception if cannot access to PasswordUtil.class via reflection.
     */
    @Test
    void constructorIsPrivateAndThrowsOnReflectiveUse() throws Exception {
        var ctor = PasswordUtil.class.getDeclaredConstructor();

        assertThat(ctor.getModifiers())
                .as("The constructor should be private")
                .matches(Modifier::isPrivate);

        ctor.setAccessible(true);
        var ex =
                catchThrowable(ctor::newInstance);
        assertExpectedCause(AppSecurityException.class)
                .assertCauseWithMessage(ex, "cannot be instantiated");
    }
}

