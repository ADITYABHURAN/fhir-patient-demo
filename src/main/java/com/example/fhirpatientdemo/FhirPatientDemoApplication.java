package com.example.fhirpatientdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the FHIR Patient Demo application.
 * 
 * This application demonstrates healthcare interoperability using:
 * - Spring Boot 3.2.4 for the REST API framework
 * - HAPI FHIR 7.2.0 for FHIR R4 client operations
 * 
 * @author Aditya Bhuran
 */
@SpringBootApplication
public class FhirPatientDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhirPatientDemoApplication.class, args);
    }
}
