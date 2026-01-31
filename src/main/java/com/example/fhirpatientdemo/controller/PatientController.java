package com.example.fhirpatientdemo.controller;

import com.example.fhirpatientdemo.dto.PatientDto;
import com.example.fhirpatientdemo.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Patient operations.
 * 
 * This controller exposes RESTful endpoints that internally use the
 * FHIR client to communicate with the FHIR server. It acts as a
 * simplified API gateway to the underlying FHIR infrastructure.
 * 
 * API Design:
 * - Uses standard HTTP methods (GET, POST, PUT, DELETE)
 * - Returns JSON responses with appropriate HTTP status codes
 * - Follows REST naming conventions (/api/patients)
 * 
 * In a real healthcare application, you would add:
 * - Authentication/Authorization (OAuth2, SMART on FHIR)
 * - Rate limiting
 * - Audit logging for PHI access
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ==================== CREATE ====================

    /**
     * Creates a new patient.
     * 
     * POST /api/patients
     * 
     * Request body example:
     * {
     *   "givenName": "John",
     *   "familyName": "Doe",
     *   "gender": "male",
     *   "birthDate": "1990-05-15",
     *   "identifier": "MRN12345",
     *   "identifierSystem": "http://hospital.org/mrn"
     * }
     * 
     * @param patientDto The patient data
     * @return Created patient with assigned ID
     */
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto patientDto) {
        log.info("POST /api/patients - Creating new patient");
        
        PatientDto created = patientService.createPatient(patientDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // ==================== READ ====================

    /**
     * Retrieves a patient by ID.
     * 
     * GET /api/patients/{id}
     * 
     * @param id The FHIR resource ID
     * @return The patient data or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable String id) {
        log.info("GET /api/patients/{} - Fetching patient", id);
        
        PatientDto patient = patientService.getPatientById(id);
        
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(patient);
    }

    /**
     * Lists all patients with optional limit.
     * 
     * GET /api/patients
     * GET /api/patients?count=50
     * 
     * @param count Maximum number of patients to return (default: 20)
     * @return List of patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients(
            @RequestParam(defaultValue = "20") int count) {
        log.info("GET /api/patients - Listing patients (count={})", count);
        
        List<PatientDto> patients = patientService.getAllPatients(count);
        
        return ResponseEntity.ok(patients);
    }

    // ==================== SEARCH ====================

    /**
     * Searches patients by name.
     * 
     * GET /api/patients/search?name=John
     * 
     * This uses FHIR's name search parameter which matches across
     * all name parts (given, family, prefix, suffix).
     * 
     * @param name The name to search for
     * @return List of matching patients
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchByName(@RequestParam String name) {
        log.info("GET /api/patients/search?name={} - Searching by name", name);
        
        List<PatientDto> patients = patientService.searchByName(name);
        
        return ResponseEntity.ok(patients);
    }

    /**
     * Searches patients by family name only.
     * 
     * GET /api/patients/search/family?name=Doe
     * 
     * @param name The family name to search for
     * @return List of matching patients
     */
    @GetMapping("/search/family")
    public ResponseEntity<List<PatientDto>> searchByFamilyName(@RequestParam String name) {
        log.info("GET /api/patients/search/family?name={} - Searching by family name", name);
        
        List<PatientDto> patients = patientService.searchByFamilyName(name);
        
        return ResponseEntity.ok(patients);
    }

    /**
     * Searches patients by identifier.
     * 
     * GET /api/patients/search/identifier?system=http://hospital.org/mrn&value=MRN12345
     * 
     * Identifier search is critical in healthcare for patient matching.
     * The system parameter ensures we're searching in the correct namespace.
     * 
     * @param system The identifier system (namespace)
     * @param value The identifier value
     * @return List of matching patients
     */
    @GetMapping("/search/identifier")
    public ResponseEntity<List<PatientDto>> searchByIdentifier(
            @RequestParam String system,
            @RequestParam String value) {
        log.info("GET /api/patients/search/identifier - system={}, value={}", system, value);
        
        List<PatientDto> patients = patientService.searchByIdentifier(system, value);
        
        return ResponseEntity.ok(patients);
    }

    // ==================== UPDATE ====================

    /**
     * Updates an existing patient.
     * 
     * PUT /api/patients/{id}
     * 
     * This performs a full replacement of the patient resource.
     * All fields should be provided in the request body.
     * 
     * @param id The patient ID to update
     * @param patientDto The updated patient data
     * @return The updated patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientDto patientDto) {
        log.info("PUT /api/patients/{} - Updating patient", id);
        
        // Verify patient exists first
        if (patientService.getPatientById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        
        PatientDto updated = patientService.updatePatient(id, patientDto);
        
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================

    /**
     * Deletes a patient.
     * 
     * DELETE /api/patients/{id}
     * 
     * @param id The patient ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        log.info("DELETE /api/patients/{} - Deleting patient", id);
        
        // Verify patient exists first
        if (patientService.getPatientById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        
        patientService.deletePatient(id);
        
        return ResponseEntity.noContent().build();
    }
}
