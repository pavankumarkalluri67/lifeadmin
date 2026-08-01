package com.LifeAdmin.ai.lifeadmin.auth.domain;

import java.util.regex.Pattern;

/**
 * Pure domain validator for email addresses.
 *
 * <p>An email is valid iff it is at most {@value #MAX_LENGTH} characters long
 * (Req 1.1) and matches the RFC 5322 addr-spec shape of {@code local-part@domain}
 * (Req 1.5).
 *
 * <p>This is a framework-free utility: no Spring, no external state. The pattern
 * is intentionally a pragmatic subset of the full RFC 5322 grammar that accepts
 * the common addr-spec forms (dot-atom local parts and dot-separated domain
 * labels) while rejecting clearly malformed values.
 */
public final class EmailFormat {

    /** Maximum allowed email length (inclusive) per Req 1.1. */
    public static final int MAX_LENGTH = 320;

    // Local part: dot-atom of allowed atext characters, no leading/trailing/double dot.
    private static final String ATEXT = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+";
    private static final String LOCAL_PART = ATEXT + "(?:\\." + ATEXT + ")*";

    // Domain: dot-separated labels of letters/digits/hyphens (no leading/trailing hyphen),
    // with a final alphabetic top-level label of at least two characters.
    private static final String LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
    private static final String DOMAIN = "(?:" + LABEL + "\\.)+[A-Za-z]{2,}";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^" + LOCAL_PART + "@" + DOMAIN + "$");

    private EmailFormat() {
        // utility class - not instantiable
    }

    /**
     * Determines whether the supplied value is a well-formed email address
     * within the allowed length.
     *
     * @param email the email address to validate; may be {@code null}
     * @return {@code true} iff the value is a valid addr-spec of length &le; 320
     */
    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.isEmpty() || email.length() > MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
