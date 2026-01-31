package com.example.fhirpatientdemo.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for HAPI FHIR client setup.
 * 
 * Key Concepts:
 * - FhirContext: Thread-safe, expensive to create - should be singleton
 * - IGenericClient: The main interface for FHIR server communication
 * 
 * This pattern is recommended by HAPI FHIR documentation for Spring applications.
 */
@Configuration
public class FhirClientConfig {

    /**
     * The base URL of the FHIR server (injected from application.yml).
     * Example: https://hapi.fhir.org/baseR4
     */
    @Value("${fhir.server.base-url}")
    private String fhirServerBaseUrl;

    /**
     * Creates a singleton FhirContext for FHIR R4.
     * 
     * FhirContext is the central class in HAPI FHIR - it:
     * - Provides parsers for JSON/XML serialization
     * - Creates client instances
     * - Holds metadata about FHIR resource structures
     * 
     * IMPORTANT: FhirContext is thread-safe but expensive to create,
     * so we create it once as a Spring bean and reuse it.
     */
    @Bean
    public FhirContext fhirContext() {
        // forR4() creates a context configured for FHIR R4 (Release 4)
        // R4 is the most widely adopted FHIR version in production
        return FhirContext.forR4();
    }

    /**
     * Creates the FHIR client for communicating with the FHIR server.
     * 
     * IGenericClient provides a fluent API for FHIR operations:
     * - create(), read(), update(), delete() - CRUD operations
     * - search() - for querying resources with search parameters
     * - transaction() - for bundle operations
     * 
     * The client handles:
     * - HTTP communication with the FHIR server
     * - JSON/XML serialization/deserialization
     * - FHIR-specific headers and error handling
     */
    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        // Disable server capability statement validation for faster startup
        // In production, you might want to enable this for conformance checking
        fhirContext.getRestfulClientFactory()
                .setServerValidationMode(ServerValidationModeEnum.NEVER);
        
        // Set reasonable timeouts (in milliseconds)
        fhirContext.getRestfulClientFactory().setConnectTimeout(30000);
        fhirContext.getRestfulClientFactory().setSocketTimeout(30000);
        
        // Create and return the client configured for our FHIR server
        return fhirContext.newRestfulGenericClient(fhirServerBaseUrl);
    }
}
