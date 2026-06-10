package com.civicconnect.server.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * RoleConverter translates between the Role enum (Java) and a String (PostgreSQL).
 *
 * @Converter(autoApply = true) — Hibernate automatically uses this converter
 * for every entity field of type Role, across ALL entity classes.
 * You never need to annotate individual fields.
 *
 * SAVE flow:  Role.MUNICIPAL → convertToDatabaseColumn() → "municipal" stored in DB
 * READ flow:  "municipal" from DB → convertToEntityAttribute() → Role.MUNICIPAL in Java
 */
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    /**
     * Java → DB: converts Role enum to its lowercase DB string before INSERT/UPDATE.
     * Returns null if the field is null (role is optional during initial Supabase sync).
     */
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.getValue(); // e.g. Role.SUPERVISOR → "supervisor"
    }

    /**
     * DB → Java: converts the raw DB string to a Role enum after SELECT.
     * dbData is whatever string PostgreSQL returned from the "role" column.
     * Returns null if the column value is NULL.
     */
    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Role.fromValue(dbData); // e.g. "citizen" → Role.CITIZEN
    }
}
