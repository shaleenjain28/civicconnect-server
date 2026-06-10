package com.civicconnect.server.entity.enums;

public enum IssueStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    RESOLVED("resolved"),
    REJECTED("rejected"),
    PENDING_VERIFICATION("pending_verification"),
    PENDING_USER_VERIFICATION("pending_user_verification");

    private final String value;

    IssueStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IssueStatus fromValue(String value) {
        for (IssueStatus status : IssueStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid IssueStatus value: " + value);
    }
}

/**
 * Converts a String (e.g. "resolved") into its corresponding IssueStatus enum.
 *
 * IssueStatus.values() returns all enum constants:
 * [PENDING, IN_PROGRESS, RESOLVED, REJECTED]
 *
 * The loop checks each enum object's `value` field and returns the matching
 * enum.
 * Throws IllegalArgumentException if no match is found.
 */