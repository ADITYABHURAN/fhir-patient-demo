# FHIR Patient Demo

A Spring Boot REST API demonstrating healthcare interoperability using **HAPI FHIR** to interact with FHIR R4 resources.

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Runtime |
| Spring Boot | 3.2.4 | REST API Framework |
| HAPI FHIR | 7.2.0 | FHIR Client Library |
| FHIR | R4 | Healthcare Standard |

## 📁 Project Structure

```
src/main/java/com/example/fhirpatientdemo/
├── FhirPatientDemoApplication.java    # Spring Boot entry point
├── config/
│   └── FhirClientConfig.java          # FHIR client bean configuration
├── controller/
│   └── PatientController.java         # REST API endpoints
├── dto/
│   └── PatientDto.java                # Data Transfer Object
├── service/
│   └── PatientService.java            # FHIR operations & business logic
└── exception/
    └── GlobalExceptionHandler.java    # Error handling
```

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run the Application

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/fhir-patient-demo.git
cd fhir-patient-demo

# Build and run
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## 📚 API Endpoints

### Create Patient
```bash
POST /api/patients
Content-Type: application/json

{
  "givenName": "John",
  "familyName": "Doe",
  "gender": "male",
  "birthDate": "1990-05-15",
  "identifier": "MRN12345",
  "identifierSystem": "http://hospital.org/mrn"
}
```

### Get Patient by ID
```bash
GET /api/patients/{id}
```

### List All Patients
```bash
GET /api/patients?count=20
```

### Search by Name
```bash
GET /api/patients/search?name=John
```

### Search by Family Name
```bash
GET /api/patients/search/family?name=Doe
```

### Search by Identifier
```bash
GET /api/patients/search/identifier?system=http://hospital.org/mrn&value=MRN12345
```

### Update Patient
```bash
PUT /api/patients/{id}
Content-Type: application/json

{
  "givenName": "John",
  "familyName": "Smith",
  "gender": "male",
  "birthDate": "1990-05-15"
}
```

### Delete Patient
```bash
DELETE /api/patients/{id}
```

## 🔑 Key Concepts Demonstrated

### FHIR Fundamentals
- **Resources**: Patient as the primary FHIR resource type
- **CRUD Operations**: Create, Read, Update, Delete via RESTful API
- **Search Parameters**: Using `name`, `family`, and `identifier` search params
- **Bundles**: Handling search results returned as FHIR Bundles

### HAPI FHIR Client Usage
- **FhirContext**: Singleton context for R4 (expensive to create, thread-safe)
- **IGenericClient**: Fluent API for FHIR server communication
- **Resource Mapping**: Converting between DTOs and FHIR resources

## 🏥 About FHIR

**FHIR (Fast Healthcare Interoperability Resources)** is the modern standard for exchanging healthcare information electronically. Key points:

- Developed by **HL7 International**
- Uses RESTful APIs + JSON/XML
- **R4** is the current normative release
- Widely adopted by major healthcare systems

## 🔗 FHIR Server

This demo uses the public **HAPI FHIR R4 Test Server**:
- Base URL: `https://hapi.fhir.org/baseR4`
- Free for testing and learning
- Data may be periodically cleared

In production, you would connect to your organization's FHIR server (e.g., Smile CDR).

## 📝 Example Workflow

```bash
# 1. Create a patient
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"givenName":"Jane","familyName":"Smith","gender":"female","birthDate":"1985-03-20"}'

# Response: {"id":"1234567","givenName":"Jane","familyName":"Smith",...}

# 2. Search for the patient
curl http://localhost:8080/api/patients/search?name=Jane

# 3. Get patient by ID
curl http://localhost:8080/api/patients/1234567

# 4. Update the patient
curl -X PUT http://localhost:8080/api/patients/1234567 \
  -H "Content-Type: application/json" \
  -d '{"givenName":"Jane","familyName":"Doe","gender":"female","birthDate":"1985-03-20"}'

# 5. Delete the patient
curl -X DELETE http://localhost:8080/api/patients/1234567
```

## 👨‍💻 Author

**Aditya Bhuran**

Built for demonstrating healthcare interoperability skills for entry-level positions in health tech.

## 📄 License

MIT License - feel free to use this as a learning resource!
