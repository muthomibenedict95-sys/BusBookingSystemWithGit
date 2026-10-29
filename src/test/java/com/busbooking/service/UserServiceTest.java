package com.busbooking.service;

import com.busbooking.dao.UserDAO;
import com.busbooking.model.User;
import com.busbooking.util.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static final String PASSWORD =
            "correct horse battery staple";

    @Test
    void registersNormalizedAccountAndStoresPasswordHash() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);
        char[] password = PASSWORD.toCharArray();

        User user = service.register(
                "  Jane Doe  ",
                "  JANE@example.com ",
                " +254 712 345678 ",
                password
        );

        assertEquals(42, user.getId());
        assertEquals("Jane Doe", user.getFullName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("+254 712 345678", user.getPhoneNumber());

        assertNotNull(dao.savedPasswordHash);
        assertNotEquals(PASSWORD, dao.savedPasswordHash);
        assertTrue(PasswordHasher.verify(
                PASSWORD.toCharArray(), dao.savedPasswordHash));

        assertArrayEquals(new char[password.length], password);
    }

    @Test
    void rejectsPasswordShorterThanFifteenCharacters() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);
        char[] password = "short-password".toCharArray();

        assertTrue(password.length < 15);

        assertThrows(IllegalArgumentException.class,
                () -> service.register(
                        "Jane Doe", "jane@example.com",
                        "+254712345678", password));

        assertArrayEquals(new char[password.length], password);
        assertNull(dao.savedUser);
    }

    @Test
    void rejectsInvalidEmail() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);
        char[] password = PASSWORD.toCharArray();

        assertThrows(IllegalArgumentException.class,
                () -> service.register(
                        "Jane Doe", "not-an-email",
                        "+254712345678", password));

        assertArrayEquals(new char[password.length], password);
        assertNull(dao.savedUser);
    }

    @Test
    void loginReturnsPublicUserForCorrectPassword() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);

        service.register(
                "Jane Doe", "jane@example.com",
                "+254712345678", PASSWORD.toCharArray());

        char[] loginPassword = PASSWORD.toCharArray();
        User loggedIn = service.login("JANE@example.com", loginPassword);

        assertEquals("jane@example.com", loggedIn.getEmail());
        assertEquals(42, loggedIn.getId());
        assertArrayEquals(new char[loginPassword.length], loginPassword);
    }

    @Test
    void loginRejectsIncorrectPassword() {
        StubUserDAO dao = new StubUserDAO();
        UserService service = new UserService(dao);

        service.register(
                "Jane Doe", "jane@example.com",
                "+254712345678", PASSWORD.toCharArray());

        char[] wrongPassword = "incorrect password value".toCharArray();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.login("jane@example.com", wrongPassword));

        assertEquals("Invalid email or password.", error.getMessage());
        assertArrayEquals(new char[wrongPassword.length], wrongPassword);
    }

    @Test
    void registrationReportsDuplicateEmail() {
        StubUserDAO dao = new StubUserDAO();
        dao.duplicateEmail = true;
        UserService service = new UserService(dao);
        char[] password = PASSWORD.toCharArray();

        assertThrows(IllegalArgumentException.class,
                () -> service.register(
                        "Jane Doe", "jane@example.com",
                        "+254712345678", password));

        assertArrayEquals(new char[password.length], password);
    }

    private static class StubUserDAO extends UserDAO {
        private User savedUser;
        private String savedPasswordHash;
        private boolean duplicateEmail;

        @Override
        public User insert(User user, String passwordHash) {
            if (duplicateEmail) {
                throw new IllegalArgumentException(
                        "An account with that email already exists.");
            }

            savedPasswordHash = passwordHash;
            savedUser = new User(
                    42,
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    LocalDateTime.of(2026, 10, 29, 14, 0)
            );
            return savedUser;
        }

        @Override
        public Account findAccountByEmail(String email) {
            if (savedUser == null || !savedUser.getEmail().equals(email)) {
                return null;
            }

            return new Account(savedUser, savedPasswordHash);
        }
    }
}
