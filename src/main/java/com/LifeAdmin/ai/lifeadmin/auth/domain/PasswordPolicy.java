package com.LifeAdmin.ai.lifeadmin.auth.domain;

/**
 * Pure domain validator for the password policy.
 *
 * <p>A password is valid iff it is between 8 and 128 characters (inclusive)
 * and contains at least one uppercase letter, one lowercase letter, one digit,
 * and one special (non alphanumeric) character (Req 1.3).
 *
 * <p>This is a framework-free utility: no Spring, no external state.
 */
public final class PasswordPolicy {

    /** Minimum allowed password length (inclusive). */
    public static final int MIN_LENGTH = 8;

    /** Maximum allowed password length (inclusive). */
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
        // utility class - not instantiable
    }

    /**
     * Determines whether the supplied password satisfies the policy.
     *
     * @param password the raw password to validate; may be {@code null}
     * @return {@code true} iff the password meets length and character-class rules
     */
    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        int length = password.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < length; i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            }
            if (Character.isLowerCase(c)) {
                hasLower = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (!Character.isLetterOrDigit(c)) {
                // Any non-alphanumeric character (whitespace or symbol)
                // counts as a special character.
                hasSpecial = true;
            }
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
