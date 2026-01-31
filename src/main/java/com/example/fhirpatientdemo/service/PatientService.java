package com.example.fhirpatientdemo.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.example.fhirpatientdemo.dto.PatientDto;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class containing all FHIR Patient operations.
 * 
 * This service demonstrates key FHIR concepts:
 * - CRUD operations on Patient resources
 * - FHIR Search with various parameters
 * - Bundle handling for search results
 * - Mapping between DTOs and FHIR resources
 * 
 * The IGenericClient provides a fluent API that mirrors FHIR's RESTful operations.
 */
@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final IGenericClient fhirClient;

    /**
     * Constructor injection of the FHIR client.
     * Spring automatically injects the bean created in FhirClientConfig.
     */
    public PatientService(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    // ==================== CREATE OPERATION ====================

    /**
     * Creates a new Patient resource on the FHIR server.
     * 
     * FHIR Operation: POST [base]/Patient
     * 
     * The server will:
     * 1. Validate the resource
     * 2. Assign a unique ID
     * 3. Return the created resource with metadata (id, versionId, lastUpdated)
     * 
     * @param patientDto The patient data to create
     * @return The created patient with server-assigned ID
     */
    public PatientDto createPatient(PatientDto patientDto) {
        log.info("Creating new patient: {} {}", patientDto.getGivenName(), patientDto.getFamilyName());

        // Convert our DTO to a FHIR Patient resource
        Patient patient = mapDtoToPatient(patientDto);

        // Execute the create operation
        // This sends: POST https://hapi.fhir.org/baseR4/Patient
        MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();

        // Get the server-assigned ID from the response
        String assignedId = outcome.getId().getIdPart();
        log.info("Patient created successfully with ID: {}", assignedId);

        // Return the DTO with the new ID
        patientDto.setId(assignedId);
        return patientDto;
    }

    // ==================== READ OPERATION ====================

    /**
     * Retrieves a Patient by their FHIR resource ID.
     * 
     * FHIR Operation: GET [base]/Patient/[id]
     * 
     * This is a "read" operation - the most basic FHIR retrieval.
     * It returns exactly one resource or throws an exception if not found.
     * 
     * @param id The FHIR resource ID
     * @return The patient data, or null if not found
     */
    public PatientDto getPatientById(String id) {
        log.info("Fetching patient with ID: {}", id);

        try {
            // Execute the read operation
            // This sends: GET https://hapi.fhir.org/baseR4/Patient/12345
            Patient patient = fhirClient.read()
                    .resource(Patient.class)
                    .withId(id)
                    .execute();

            log.info("Found patient: {} {}", 
                    patient.getNameFirstRep().getGivenAsSingleString(),
                    patient.getNameFirstRep().getFamily());

            return mapPatientToDto(patient);

        } catch (Exception e) {
            log.warn("Patient not found with ID: {}", id);
            return null;
        }
    }

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Searches for patients by name (given or family).
     * 
     * FHIR Operation: GET [base]/Patient?name=[searchTerm]
     * 
     * The "name" search parameter is a string search that:
     * - Searches across all name parts (given, family, prefix, suffix)
     * - Uses "starts with" matching by default
     * - Is case-insensitive
     * 
     * @param name The name to search for
     * @return List of matching patients
     */
    public List<PatientDto> searchByName(String name) {
        log.info("Searching for patients with name: {}", name);

        // Execute the search operation
        // This sends: GET https://hapi.fhir.org/baseR4/Patient?name=Smith
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.NAME.matches().value(name))
                .returnBundle(Bundle.class)
                .execute();

        return extractPatientsFromBundle(bundle);
    }

    /**
     * Searches for patients by identifier (e.g., MRN, SSN).
     * 
     * FHIR Operation: GET [base]/Patient?identifier=[system]|[value]
     * 
     * Identifier search is crucial in healthcare for:
     * - Patient matching across systems
     * - Ensuring you're accessing the correct patient record
     * - Integration with external systems (labs, pharmacies, etc.)
     * 
     * @param system The identifier system (e.g., "http://hospital.org/mrn")
     * @param value The identifier value (e.g., "MRN12345")
     * @return List of matching patients
     */
    public List<PatientDto> searchByIdentifier(String system, String value) {
        log.info("Searching for patients with identifier: {}|{}", system, value);

        // Build the search with system|value format
        // This sends: GET https://hapi.fhir.org/baseR4/Patient?identifier=http://hospital.org/mrn|12345
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier(system, value))
                .returnBundle(Bundle.class)
                .execute();

        return extractPatientsFromBundle(bundle);
    }

    /**
     * Searches for patients by family name only.
     * 
     * FHIR Operation: GET [base]/Patient?family=[familyName]
     * 
     * This demonstrates using a more specific search parameter.
     * FHIR defines separate parameters for different name parts:
     * - family: searches only family name
     * - given: searches only given names
     * - name: searches all name parts
     * 
     * @param familyName The family name to search for
     * @return List of matching patients
     */
    public List<PatientDto> searchByFamilyName(String familyName) {
        log.info("Searching for patients with family name: {}", familyName);

        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value(familyName))
                .returnBundle(Bundle.class)
                .execute();

        return extractPatientsFromBundle(bundle);
    }

    /**
     * Retrieves all patients with pagination support.
     * 
     * FHIR Operation: GET [base]/Patient?_count=[count]
     * 
     * The _count parameter limits results per page. FHIR servers return
     * results in Bundles with pagination links (next, previous) for
     * navigating large result sets.
     * 
     * Note: In production, you'd implement proper pagination using
     * bundle.getLink("next") to fetch additional pages.
     * 
     * @param count Maximum number of results to return
     * @return List of patients
     */
    public List<PatientDto> getAllPatients(int count) {
        log.info("Fetching up to {} patients", count);

        // This sends: GET https://hapi.fhir.org/baseR4/Patient?_count=50
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .count(count)
                .returnBundle(Bundle.class)
                .execute();

        List<PatientDto> patients = extractPatientsFromBundle(bundle);
        log.info("Retrieved {} patients", patients.size());

        return patients;
    }

    // ==================== UPDATE OPERATION ====================

    /**
     * Updates an existing Patient resource.
     * 
     * FHIR Operation: PUT [base]/Patient/[id]
     * 
     * FHIR update is a complete replacement - you must send the entire
     * resource, not just the changed fields. For partial updates,
     * you would use PATCH instead.
     * 
     * @param id The patient ID to update
     * @param patientDto The new patient data
     * @return The updated patient
     */
    public PatientDto updatePatient(String id, PatientDto patientDto) {
        log.info("Updating patient with ID: {}", id);

        // Set the ID on the DTO
        patientDto.setId(id);
        
        // Convert to FHIR Patient
        Patient patient = mapDtoToPatient(patientDto);
        patient.setId(id);

        // Execute the update
        // This sends: PUT https://hapi.fhir.org/baseR4/Patient/12345
        fhirClient.update()
                .resource(patient)
                .execute();

        log.info("Patient updated successfully");
        return patientDto;
    }

    // ==================== DELETE OPERATION ====================

    /**
     * Deletes a Patient resource.
     * 
     * FHIR Operation: DELETE [base]/Patient/[id]
     * 
     * Note: Many FHIR servers don't actually delete the resource but
     * mark it as deleted (soft delete). The resource may still be
     * retrievable via version history.
     * 
     * @param id The patient ID to delete
     */
    public void deletePatient(String id) {
        log.info("Deleting patient with ID: {}", id);

        // This sends: DELETE https://hapi.fhir.org/baseR4/Patient/12345
        fhirClient.delete()
                .resourceById(new IdType("Patient", id))
                .execute();

        log.info("Patient deleted successfully");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Extracts Patient resources from a FHIR Bundle.
     * 
     * FHIR search operations return results in a Bundle, which:
     * - Contains a list of entries (bundle.entry)
     * - Each entry has a resource and metadata
     * - Includes pagination links for navigating results
     * - Has a total count of matching resources
     * 
     * This is a common pattern you'll use whenever processing search results.
     */
    private List<PatientDto> extractPatientsFromBundle(Bundle bundle) {
        List<PatientDto> patients = new ArrayList<>();

        // Check if bundle has entries
        if (bundle.hasEntry()) {
            log.debug("Bundle contains {} entries", bundle.getEntry().size());

            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                // Each entry's resource could be any FHIR type, so we check
                if (entry.getResource() instanceof Patient) {
                    Patient patient = (Patient) entry.getResource();
                    patients.add(mapPatientToDto(patient));
                }
            }
        }

        return patients;
    }

    /**
     * Maps our simple DTO to a FHIR Patient resource.
     * 
     * FHIR resources have a specific structure defined in the spec.
     * The Patient resource includes many optional elements; here we
     * populate only the most common ones.
     * 
     * Key FHIR Patient elements:
     * - name: List of HumanName (can have multiple names)
     * - identifier: List of Identifier (MRN, SSN, etc.)
     * - gender: Code from AdministrativeGender value set
     * - birthDate: Date in YYYY-MM-DD format
     */
    private Patient mapDtoToPatient(PatientDto dto) {
        Patient patient = new Patient();

        // Set the name
        // FHIR supports multiple names (legal, nickname, etc.)
        // We're using the first/default name
        HumanName name = patient.addName();
        name.setFamily(dto.getFamilyName());
        name.addGiven(dto.getGivenName());

        // Set gender using FHIR's enumeration
        if (dto.getGender() != null) {
            patient.setGender(Enumerations.AdministrativeGender.fromCode(dto.getGender()));
        }

        // Set birth date
        if (dto.getBirthDate() != null) {
            patient.setBirthDateElement(new DateType(dto.getBirthDate()));
        }

        // Set identifier if provided
        if (dto.getIdentifier() != null) {
            Identifier identifier = patient.addIdentifier();
            identifier.setValue(dto.getIdentifier());
            if (dto.getIdentifierSystem() != null) {
                identifier.setSystem(dto.getIdentifierSystem());
            }
        }

        return patient;
    }

    /**
     * Maps a FHIR Patient resource back to our DTO.
     * 
     * This handles the conversion from FHIR's complex structure
     * to our simplified API response format.
     * 
     * Note: We use "FirstRep" methods which get the first element
     * of a list, or create an empty one if none exists. This is
     * a HAPI FHIR convenience for handling optional/repeating elements.
     */
    private PatientDto mapPatientToDto(Patient patient) {
        PatientDto dto = new PatientDto();

        // Get the resource ID (without version)
        dto.setId(patient.getIdElement().getIdPart());

        // Get the first name (patients can have multiple names)
        HumanName name = patient.getNameFirstRep();
        dto.setFamilyName(name.getFamily());
        dto.setGivenName(name.getGivenAsSingleString());

        // Get gender
        if (patient.hasGender()) {
            dto.setGender(patient.getGender().toCode());
        }

        // Get birth date
        if (patient.hasBirthDate()) {
            dto.setBirthDate(patient.getBirthDateElement().getValueAsString());
        }

        // Get first identifier
        if (patient.hasIdentifier()) {
            Identifier identifier = patient.getIdentifierFirstRep();
            dto.setIdentifier(identifier.getValue());
            dto.setIdentifierSystem(identifier.getSystem());
        }

        return dto;
    }
}
