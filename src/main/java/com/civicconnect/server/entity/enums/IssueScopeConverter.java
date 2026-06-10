package com.civicconnect.server.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * IssueScopeConverter translates between the IssueScope enum (Java) and a String (PostgreSQL).
 *
 * @Converter(autoApply = true) — Hibernate automatically applies this for every
 * entity field of type IssueScope without any additional annotation needed.
 *
 * SAVE flow:  IssueScope.LOCAL → convertToDatabaseColumn() → "local" stored in DB
 * READ flow:  "local" from DB → convertToEntityAttribute() → IssueScope.LOCAL in Java
 */
@Converter(autoApply = true)
public class IssueScopeConverter implements AttributeConverter<IssueScope, String> {

    /**
     * Java → DB: converts IssueScope enum to its lowercase DB string.
     * Called before every INSERT or UPDATE that involves a scope field.
     */
    @Override
    public String convertToDatabaseColumn(IssueScope scope) {
        if (scope == null) {
            return null;
        }
        return scope.getValue(); // e.g. IssueScope.CITY → "city"
    }

    /**
     * DB → Java: converts the raw "scope" column string to an IssueScope enum.
     * dbData is whatever string PostgreSQL returned from the "scope" column.
     */
    @Override
    public IssueScope convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return IssueScope.fromValue(dbData); // e.g. "local" → IssueScope.LOCAL
    }
}
