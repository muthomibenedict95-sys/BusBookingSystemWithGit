package com.busbooking.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesAndVerifiesPassword() {
        char[] password = "correct horse battery staple".toCharArray();

        String encodedHash = PasswordHasher.hash(password);

        assertTrue(encodedHash.startsWith("pbkdf2-sha256$600000$"));
        assertTrue(PasswordHasher.verify(password, encodedHash));
        assertFalse(PasswordHasher.verify(
                "a different password".toCharArray(), encodedHash));
    }

    @Test
    void generatesDifferentHashesForSamePassword() {
        char[] password = "same password".toCharArray();

        String firstHash = PasswordHasher.hash(password);
        String secondHash = PasswordHasher.hash(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(PasswordHasher.verify(password, firstHash));
        assertTrue(PasswordHasher.verify(password, secondHash));
    }

    @Test
    void rejectsMalformedStoredHash() {
        assertFalse(PasswordHasher.verify(
                "some password".toCharArray(), "not-a-valid-hash"));
    }

    @Test
    void rejectsEmptyPasswordWhenHashing() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hash(new char[0]));
    }
}
