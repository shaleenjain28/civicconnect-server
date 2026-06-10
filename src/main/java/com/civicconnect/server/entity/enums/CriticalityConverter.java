package com.civicconnect.server.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CriticalityConverter translates between the Criticality enum (Java) and a String (PostgreSQL).
 *
 * @Converter(autoApply = true) — Hibernate automatically applies this for every
 * entity field of type Criticality without any additional annotation needed.
 *
 * SAVE flow:  Criticality.HIGH → convertToDatabaseColumn() → "high" stored in DB
 * READ flow:  "high" from DB → convertToEntityAttribute() → Criticality.HIGH in Java
 */
@Converter(autoApply = true)
public class CriticalityConverter implements AttributeConverter<Criticality, String> {

    /**
     * Java → DB: converts Criticality enum to its lowercase DB string.
     * Called before every INSERT or UPDATE that involves a criticality field.
     */
    @Override
    public String convertToDatabaseColumn(Criticality criticality) {
        if (criticality == null) {
            return null;
        }
        return criticality.getValue(); // e.g. Criticality.CRITICAL → "critical"
    }

    /**
     * DB → Java: converts the raw "criticality" column string to a Criticality enum.
     * dbData is whatever string PostgreSQL returned, e.g. "medium".
     */
    @Override
    public Criticality convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Criticality.fromValue(dbData); // e.g. "medium" → Criticality.MEDIUM
    }
}
