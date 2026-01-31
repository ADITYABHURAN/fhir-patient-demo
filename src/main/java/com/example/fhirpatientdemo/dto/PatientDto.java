package com.example.fhirpatientdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for Patient data.
 * 
 * Why use a DTO instead of the FHIR Patient resource directly?
 * 1. Simplicity: FHIR resources are complex; DTOs expose only what the API needs
 * 2. Decoupling: Changes to FHIR structure don't break our API contract
 * 3. Validation: We can add our own validation rules
 * 4. Security: We control exactly what data is exposed
 * 
 * This DTO maps to the most commonly used Patient elements:
 * - HumanName (given + family)
 * - Identifier (for MRN, SSN, etc.)
 * - Gender (administrative gender)
 * - BirthDate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {

    /**
     * The FHIR resource ID (assigned by the server after creation).
     * Format: alphanumeric string, e.g., "12345" or "abc-123"
     */
    private String id;

    /**
     * Patient's given (first) name.
     * Maps to: Patient.name[0].given[0]
     */
    @NotBlank(message = "Given name is required")
    private String givenName;

    /**
     * Patient's family (last) name.
     * Maps to: Patient.name[0].family
     */
    @NotBlank(message = "Family name is required")
    private String familyName;

    /**
     * Administrative gender.
     * Maps to: Patient.gender
     * Valid values: male, female, other, unknown
     */
    @Pattern(regexp = "male|female|other|unknown", 
             message = "Gender must be: male, female, other, or unknown")
    private String gender;

    /**
     * Date of birth in ISO format (YYYY-MM-DD).
     * Maps to: Patient.birthDate
     * Example: "1990-05-15"
     */
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", 
             message = "Birth date must be in YYYY-MM-DD format")
    private String birthDate;

    /**
     * Patient identifier value (e.g., MRN, SSN).
     * Maps to: Patient.identifier[0].value
     * 
     * In real healthcare systems, identifiers are crucial for:
     * - Patient matching across systems
     * - Avoiding duplicate records
     * - Cross-referencing with other resources
     */
    private String identifier;

    /**
     * The system/namespace for the identifier (e.g., hospital MRN system).
     * Maps to: Patient.identifier[0].system
     * Example: "http://hospital.org/mrn"
     */
    private String identifierSystem;
}
