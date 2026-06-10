package com.civicconnect.server.entity.enums;

/**
 * IssueStatusConverter is a JPA AttributeConverter.
 *
 * THE PROBLEM IT SOLVES:
 * Your DB column "status" stores "pending", "resolved" etc. (lowercase Strings).
 * Your Java entity field is of type IssueStatus (an enum).
 * JPA doesn't know how to convert between them automatically.
 * This class teaches JPA exactly how to do that translation.
 *
 * HOW JPA USES THIS:
 * The @Converter(autoApply = true) annotation tells Hibernate:
 *   "For every entity field of type IssueStatus, use THIS class to convert it."
 * You never call this converter manually — Hibernate calls it for you.
 *
 * FLOW on SAVE (Java → DB):
 *   issue.setStatus(IssueStatus.PENDING)
 *   → Hibernate calls convertToDatabaseColumn(IssueStatus.PENDING)
 *   → returns "pending"
 *   → SQL: INSERT INTO issues (status) VALUES ('pending')
 *
 * FLOW on READ (DB → Java):
 *   SQL returns row with status = 'pending'
 *   → Hibernate calls convertToEntityAttribute("pending")
 *   → returns IssueStatus.PENDING
 *   → issue.getStatus() == IssueStatus.PENDING ✅
 *
 * AttributeConverter<X, Y>:
 *   X = Java type  → IssueStatus (the enum)
 *   Y = DB type    → String (what the column stores)
 */
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IssueStatusConverter implements AttributeConverter<IssueStatus, String> {

    /**
     * Called by Hibernate every time it saves or updates an IssueStatus field.
     * Converts the Java enum → lowercase DB string.
     *
     * Null check is important: if the field is null (optional status),
     * we return null instead of crashing with a NullPointerException.
     */
    @Override
    public String convertToDatabaseColumn(IssueStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue(); // e.g. IssueStatus.PENDING → "pending"
    }

    /**
     * Called by Hibernate every time it reads an IssueStatus column from the DB.
     * Converts the raw DB string → Java enum constant.
     *
     * dbData is the raw value from the PostgreSQL column, e.g. "pending".
     * IssueStatus.fromValue() loops through all constants to find a match.
     * Null check handles rows where the status column is NULL.
     */
    @Override
    public IssueStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return IssueStatus.fromValue(dbData); // e.g. "pending" → IssueStatus.PENDING
    }
}
