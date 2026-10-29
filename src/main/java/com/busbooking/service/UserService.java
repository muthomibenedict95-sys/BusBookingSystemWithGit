package com.busbooking.service;

import com.busbooking.dao.UserDAO;
import com.busbooking.model.User;
import com.busbooking.util.PasswordHasher;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Registration and login rules for customer accounts.
 */
public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 15;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_PHONE_LENGTH = 30;

    private static final Pattern BASIC_EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO);
    }

    /**
     * Registers an account. The supplied password array is cleared before
     * this method returns or throws.
     */
    public User register(String fullName, String email,
                         String phoneNumber, char[] password) {
        try {
            String name = validateName(fullName);
            String normalizedEmail = normalizeEmail(email);
            String phone = validatePhone(phoneNumber);
            validateNewPassword(password);

            String passwordHash = PasswordHasher.hash(password);

            User newUser = new User(
                    0, name, normalizedEmail, phone, null);

            // The unique email constraint in MariaDB is authoritative.
            // It also protects against concurrent registration attempts.
            return userDAO.insert(newUser, passwordHash);
        } finally {
            clearPassword(password);
        }
    }

    /**
     * Returns public user details after successful authentication.
     * Unknown emails and incorrect passwords share one error message.
     */
    public User login(String email, char[] password) {
        try {
            final String normalizedEmail;

            try {
                normalizedEmail = normalizeEmail(email);
            } catch (IllegalArgumentException e) {
                throw invalidCredentials();
            }

            if (password == null
                    || password.length == 0
                    || password.length > MAX_PASSWORD_LENGTH) {
                throw invalidCredentials();
            }

            UserDAO.Account account =
                    userDAO.findAccountByEmail(normalizedEmail);

            if (account == null
                    || !PasswordHasher.verify(
                            password, account.passwordHash())) {
                throw invalidCredentials();
            }

            return account.user();
        } finally {
            clearPassword(password);
        }
    }

    private static String validateName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty.");
        }

        String name = fullName.trim();

        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 150 characters.");
        }

        return name;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() > MAX_EMAIL_LENGTH
                || !BASIC_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }

        return normalized;
    }

    private static String validatePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Phone number cannot be empty.");
        }

        String phone = phoneNumber.trim();

        if (phone.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException(
                    "Phone number cannot exceed 30 characters.");
        }

        return phone;
    }

    private static void validateNewPassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must contain at least 15 characters.");
        }

        if (password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 128 characters.");
        }
    }

    private static IllegalArgumentException invalidCredentials() {
        return new IllegalArgumentException("Invalid email or password.");
    }

    private static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, (char) 0);
        }
    }
}
