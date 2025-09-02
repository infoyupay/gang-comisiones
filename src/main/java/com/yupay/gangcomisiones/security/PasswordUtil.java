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
import org.jetbrains.annotations.Contract;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password.
 *
 * @author InfoYupay SACS
 * @version 1.0
 */
public class PasswordUtil {
    /**
     * Defines the default length for the cryptographic salt used in password generation
     * and hashing processes. The salt helps to ensure that identical passwords result
     * in different hashes, enhancing security against precomputed hash attacks such as
     * rainbow table attacks.
     */
    private static final int SALT_LENGTH = 16;
    /**
     * Defines the default number of iterations for the password hashing algorithm.
     * Increasing the number of iterations makes the hashing process slower,
     * which can help to mitigate brute-force attacks.
     */
    private static final int ITERATIONS = 65536;
    /**
     * Defines the default length for the cryptographic key used in password hashing.
     */
    private static final int KEY_LENGTH = 256;

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws AppSecurityException always.
     * @deprecated Utility class, not for instantiation.
     */
    @Deprecated
    @Contract(" -> fail")
    private PasswordUtil() throws AppSecurityException {
        throw new AppSecurityException("PasswordUtil is a utility class and cannot be instantiated. I've got you rascal pirate!");
    }

    /**
     * Generates a cryptographic salt of {@link #SALT_LENGTH} length using a secure random number generator.
     * The generated salt is encoded in Base64 format to make it suitable for storage or transmission.
     *
     * @return A string representation of the generated cryptographic salt in Base64 format.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes the given password using the PBKDF2WithHmacSHA256 algorithm with the
     * specified salt, {@link #ITERATIONS} number of iterations, and {@link #KEY_LENGTH}.
     *
     * @param password the plain text password to be hashed
     * @param salt     the cryptographic salt, encoded in Base64 format, used for the hashing process
     * @return the hashed password as a Base64-encoded string
     * @throws AppSecurityException if an error occurs during the hashing process
     */
    public static String hashPassword(String password, String salt) {
        return Base64.getEncoder().encodeToString(hashPasswordRaw(password, salt));
    }

    /**
     * Hashes the given password using the PBKDF2WithHmacSHA256 algorithm with the
     * specified salt, {@link #ITERATIONS} number of iterations, and {@link #KEY_LENGTH}.
     *
     * @param password the plain text password to be hashed.
     * @param salt     the cryptographic salt, encoded in Base64 format, used for the hashing process.
     * @return the hashed password as a byte array for further usage.
     * @throws AppSecurityException if an error occurs during the hashing process.
     */
    private static byte[] hashPasswordRaw(String password, String salt) {
        PBEKeySpec spec = null;
        try {
            spec = new PBEKeySpec(
                    password.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERATIONS,
                    KEY_LENGTH
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new AppSecurityException("Error hashing password", e);
        } finally {
            if (spec != null) spec.clearPassword();
        }
    }

    /**
     * Verifies if the provided plain text password matches the expected hash using the given salt.
     *
     * @param password     the plain text password to verify
     * @param salt         the cryptographic salt in Base64 format
     * @param expectedHash the expected hashed password in Base64 format
     * @return true if the password matches, false otherwise
     * @throws AppSecurityException if an error occurs during verification
     */
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        var computedHash = hashPasswordRaw(password, salt);
        var expectedBytes = Base64.getDecoder().decode(expectedHash);
        return MessageDigest.isEqual(computedHash, expectedBytes);
    }
}

