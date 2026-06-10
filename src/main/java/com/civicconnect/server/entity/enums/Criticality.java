package com.civicconnect.server.entity.enums;

/**
 * Criticality represents how urgent a reported issue is.
 *
 * This drives two things in the system:
 *   1. Deadline calculation — how many days before the issue is considered overdue
 *   2. Urgency scoring — higher criticality = higher urgency score = appears higher in listings
 *
 * Business deadlines (from the original Express logic):
 *   CRITICAL → 1 day deadline
 *   HIGH     → 3 days deadline
 *   MEDIUM   → 7 days deadline
 *   LOW      → 14 days deadline
 *
 * Stored in the "issues" table under the "criticality" column as lowercase strings.
 * Translated by CriticalityConverter automatically via JPA.
 */
public enum Criticality {

    CRITICAL("critical"),
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    // The exact string stored in the DB "criticality" column
    private final String value;

    Criticality(String value) {
        this.value = value;
    }

    /**
     * Returns the DB string for this criticality level.
     * e.g. Criticality.HIGH.getValue() → "high"
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a raw DB string into the matching Criticality constant.
     * Called by CriticalityConverter when reading from the database.
     * e.g. Criticality.fromValue("medium") → Criticality.MEDIUM
     */
    public static Criticality fromValue(String value) {
        for (Criticality c : Criticality.values()) {
            if (c.value.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Invalid Criticality value: " + value);
    }
}
