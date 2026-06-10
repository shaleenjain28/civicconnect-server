package com.civicconnect.server.entity.enums;

/**
 * Role represents the type of user in the CivicConnect system.
 *
 * Each role controls what actions a user can perform:
 *   CITIZEN    → Can report issues, vote, comment, confirm resolutions
 *   MUNICIPAL  → Can update issue status, submit resolution photos
 *   SUPERVISOR → Can verify resolutions, reassign departments, view escalations
 *   NGO        → Read-only partner access
 *
 * Stored in the "users" table under the "role" column as lowercase strings.
 * Translated by RoleConverter automatically via JPA.
 */
public enum Role {

    CITIZEN("citizen"),
    MUNICIPAL("municipal"),
    SUPERVISOR("supervisor"),
    NGO("ngo");

    // The exact string stored in the DB "role" column
    private final String value;

    Role(String value) {
        this.value = value;
    }

    /**
     * Returns the DB string for this role.
     * e.g. Role.SUPERVISOR.getValue() → "supervisor"
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a raw DB string into the matching Role constant.
     * Called by RoleConverter when reading from the database.
     * e.g. Role.fromValue("citizen") → Role.CITIZEN
     */
    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid Role value: " + value);
    }
}
