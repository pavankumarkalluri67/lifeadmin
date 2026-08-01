package com.LifeAdmin.ai.lifeadmin.auth.domain;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Set;

/**
 * Pure domain validator for IANA timezone identifiers.
 *
 * <p>A value is valid iff it is a non-blank, known IANA timezone id resolvable
 * by {@link java.time.ZoneId} (Req 5.3). Null, blank, and unknown ids are
 * rejected. Fixed-offset ids (e.g. {@code "+05:30"}) that are not part of the
 * available region-based zone set are rejected to keep this strictly IANA.
 *
 * <p>This is a framework-free utility: no Spring, no external state.
 */
public final class TimezoneValidator {

    private static final Set<String> AVAILABLE_ZONE_IDS = ZoneId.getAvailableZoneIds();

    private TimezoneValidator() {
        // utility class - not instantiable
    }

    /**
     * Determines whether the supplied value is a valid IANA timezone id.
     *
     * @param timezone the timezone id to validate; may be {@code null}
     * @return {@code true} iff the value is a known IANA timezone id
     */
    public static boolean isValid(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return false;
        }
        if (!AVAILABLE_ZONE_IDS.contains(timezone)) {
            return false;
        }
        try {
            ZoneId.of(timezone);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }
}
