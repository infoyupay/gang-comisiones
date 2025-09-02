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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for password utility class.
 * <br/>
 * Tested by dvidal, 6 tests 1.281s passed at 2025-08-01 23:06:00 UTC-5.
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
        String salt = PasswordUtil.generateSalt();
        assertNotNull(salt);
        byte[] saltBytes = java.util.Base64.getDecoder().decode(salt);
        assertEquals(16, saltBytes.length, "Salt must be 16 bytes long");
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
        String salt = PasswordUtil.generateSalt();
        String password = "SuperSecret123!";
        String hash1 = PasswordUtil.hashPassword(password, salt);
        String hash2 = PasswordUtil.hashPassword(password, salt);

        assertEquals(hash1, hash2, "Hashing the same password+salt must be deterministic");
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
        String password = "SuperSecret123!";
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        String hash1 = PasswordUtil.hashPassword(password, salt1);
        String hash2 = PasswordUtil.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2, "Hashes with different salts must differ");
    }

    /**
     * Tests the behavior of {@link PasswordUtil#verifyPassword(String, String, String)} when the correct password is provided.
     * Ensures that the verification process succeeds when the input matches the stored hash.
     */
    @Test
    void testVerifyPasswordSuccess() {
        String password = "SuperSecret123!";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        assertTrue(PasswordUtil.verifyPassword(password, salt, hash), "Password must verify successfully");
    }

    /**
     * Tests the behavior of {@link PasswordUtil#verifyPassword(String, String, String)} when the incorrect
     * password is provided.
     * Ensures that the verification process fails when the input does not match the stored hash.
     */
    @Test
    void testVerifyPasswordFailure() {
        String salt = PasswordUtil.generateSalt();
        String correctHash = PasswordUtil.hashPassword("SuperSecret123!", salt);

        assertFalse(PasswordUtil.verifyPassword("WrongPassword!", salt, correctHash),
                "Verification must fail with incorrect password");
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
        Constructor<PasswordUtil> ctor = PasswordUtil.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(ctor.getModifiers()), "Constructor must be private");

        ctor.setAccessible(true);
        InvocationTargetException ex =
                assertThrows(InvocationTargetException.class, ctor::newInstance);

        assertNotNull(ex.getCause(), "Expected wrapped cause");
        assertInstanceOf(AppSecurityException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().toLowerCase().contains("cannot be instantiated"));
    }
}

