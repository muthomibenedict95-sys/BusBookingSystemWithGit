package com.busbooking.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Hashes passwords for storage and verifies supplied passwords at login.
 * Passwords are never stored in plaintext.
 */
public final class PasswordHasher {
    private static final String FORMAT = "pbkdf2-sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private static final int MIN_ACCEPTED_ITERATIONS = 100_000;
    private static final int MAX_ACCEPTED_ITERATIONS = 2_000_000;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER =
            Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private PasswordHasher() {
    }

    /**
     * Returns a self-contained encoded hash with algorithm, work factor,
     * random salt, and derived password hash.
     */
    public static String hash(char[] password) {
        validatePassword(password);

        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        byte[] derivedKey = deriveKey(password, salt, ITERATIONS);

        try {
            return FORMAT + "$" + ITERATIONS + "$"
                    + ENCODER.encodeToString(salt) + "$"
                    + ENCODER.encodeToString(derivedKey);
        } finally {
            Arrays.fill(derivedKey, (byte) 0);
        }
    }

    /**
     * Returns true only if the supplied password matches the encoded hash.
     * Malformed or unsupported stored hashes return false.
     */
    public static boolean verify(char[] password, String encodedHash) {
        if (password == null || password.length == 0
                || password.length > MAX_PASSWORD_LENGTH
                || encodedHash == null) {
            return false;
        }

        String[] parts = encodedHash.split("\\$", -1);

        if (parts.length != 4 || !FORMAT.equals(parts[0])) {
            return false;
        }

        final int iterations;
        final byte[] salt;
        final byte[] expectedKey;

        try {
            iterations = Integer.parseInt(parts[1]);
            salt = DECODER.decode(parts[2]);
            expectedKey = DECODER.decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (iterations < MIN_ACCEPTED_ITERATIONS
                || iterations > MAX_ACCEPTED_ITERATIONS
                || salt.length < SALT_LENGTH
                || expectedKey.length != KEY_LENGTH_BITS / 8) {
            return false;
        }

        byte[] actualKey = deriveKey(password, salt, iterations);

        try {
            return MessageDigest.isEqual(expectedKey, actualKey);
        } finally {
            Arrays.fill(actualKey, (byte) 0);
            Arrays.fill(expectedKey, (byte) 0);
        }
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "Password cannot be empty.");
        }

        if (password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password is too long.");
        }
    }

    private static byte[] deriveKey(
            char[] password, byte[] salt, int iterations) {

        PBEKeySpec specification = new PBEKeySpec(
                password, salt, iterations, KEY_LENGTH_BITS);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Password hashing is unavailable.", e);
        } finally {
            specification.clearPassword();
        }
    }
}
