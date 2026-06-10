package com.civicconnect.server.entity.enums;

/**
 * IssueScope represents the geographic reach of a reported issue.
 *
 * Scope determines how widely the issue is visible and how it is escalated:
 *   LOCAL   → Affects a street or neighbourhood (default for most issues)
 *   CITY    → Affects the entire municipality
 *   STATE   → Affects the state level
 *   COUNTRY → Affects national infrastructure
 *
 * A broken streetlight is LOCAL. A city-wide water shortage is CITY.
 * Scope influences which officers see the issue and the escalation path.
 *
 * Stored in the "issues" table under the "scope" column as lowercase strings.
 * Translated by IssueScopeConverter automatically via JPA.
 */
public enum IssueScope {

    LOCAL("local"),
    CITY("city"),
    STATE("state"),
    COUNTRY("country");

    // The exact string stored in the DB "scope" column
    private final String value;

    IssueScope(String value) {
        this.value = value;
    }

    /**
     * Returns the DB string for this scope.
     * e.g. IssueScope.LOCAL.getValue() → "local"
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a raw DB string into the matching IssueScope constant.
     * Called by IssueScopeConverter when reading from the database.
     * e.g. IssueScope.fromValue("city") → IssueScope.CITY
     */
    public static IssueScope fromValue(String value) {
        for (IssueScope scope : IssueScope.values()) {
            if (scope.value.equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Invalid IssueScope value: " + value);
    }
}
