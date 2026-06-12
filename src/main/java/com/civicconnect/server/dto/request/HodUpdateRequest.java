package com.civicconnect.server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HodUpdateRequest — Input DTO for PATCH /api/departments/{id}/hod
 *
 * ── What is a Request DTO? ────────────────────────────────────────────────────
 * This is the shape of data coming INTO your API from the client as JSON.
 * Spring automatically deserializes the JSON body into this class.
 *
 * ── Why not accept the Department entity directly? ────────────────────────────
 * The supervisor can ONLY update HOD fields — not name, slug, icon, or color.
 * Accepting a Department entity would allow a malicious client to send:
 *   { "name": "hacked", "slug": "hacked", "hodName": "..." }
 * and overwrite fields they shouldn't touch. This is called a "mass assignment"
 * or "over-posting" attack.
 * The request DTO is a WHITELIST — only fields declared here can be modified.
 *
 * ── Validation annotations ────────────────────────────────────────────────────
 * Jakarta Bean Validation annotations run when the controller has @Valid on the param.
 * If any rule fails → Spring returns 400 Bad Request BEFORE your service is called.
 * No manual if-checks needed.
 *
 * ── All fields are optional (no @NotBlank) ────────────────────────────────────
 * The supervisor might update only the email without touching the name or phone.
 * If a field is null → Service leaves the existing value unchanged.
 * If a field is provided → Service updates it.
 */
@Data
@NoArgsConstructor
public class HodUpdateRequest {

    /**
     * Head of Department's full name.
     * @Size(max = 100) prevents absurdly long strings hitting the database.
     */
    @Size(max = 100, message = "HOD name must not exceed 100 characters")
    private String hodName;

    /**
     * HOD's official email address.
     * @Email validates the format — must have @, a domain, etc.
     *   Valid:   "rajesh@municipality.gov.in"
     *   Invalid: "notanemail" → 400 Bad Request
     */
    @Email(message = "HOD email must be a valid email address")
    @Size(max = 255, message = "HOD email must not exceed 255 characters")
    private String hodEmail;

    /**
     * HOD phone number — stored as String, NOT Integer or Long.
     *
     * WHY String for a phone number?
     *   1. Numbers can start with 0 — integers drop leading zeros silently.
     *   2. Phone numbers include formatting: "+91-98765-43210", "(022) 1234-5678"
     *   3. No arithmetic is done on phone numbers — treating them as strings is correct.
     *
     * E.164 international format: max 15 digits. With formatting chars: use 20.
     */
    @Size(max = 20, message = "HOD phone must not exceed 20 characters")
    private String hodPhone;

    /**
     * HOD's official job designation.
     * e.g. "Commissioner", "Chief Engineer", "Executive Officer"
     */
    @Size(max = 100, message = "HOD title must not exceed 100 characters")
    private String hodTitle;
}
