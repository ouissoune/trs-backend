# Teacher Reservation System (TRS)

A Spring Boot application for managing teacher availability and student reservations.

## Features

### User Management
- JWT-based authentication and authorization
- Three user roles: Student, Teacher, Admin
- One-to-one relationship between User and each profile (Student, Teacher, Admin)
- Admin user seeded on startup (configurable credentials)

### Teacher Management
- Teacher registration request workflow (teacher submits request, admin approves)
- Admin-created teacher accounts (username/password)
- Skill management (add/remove skills after registration)
- Availability slot management using time ranges
- Automatic one-hour slot generation from time ranges

### Student Management  
- Student self-registration
- Browse available teachers and their slots
- Reserve one or more teacher slots
- Cancel reservations

### File Management
- Upload and download files (CVs, documents, etc.)
- Secured file access through REST endpoints

## Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **Build**: Maven
- **Containerization**: Docker & Docker Compose

## Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose

## Setup Instructions

### 1. Start PostgreSQL Container

The application uses PostgreSQL with the following credentials:
- Database: `trs_db`
- Username: `kilmerx`
- Password: `kilmerx`
- Port: `5432`

Start the container using Docker Compose:

```bash
cd c:\Users\pc\Desktop\work\Projects\SpringProjects\trs
docker-compose up -d
```

Verify the container is running:

```bash
docker ps
```

### 2. Build and Run the Application

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Authentication
- `POST /api/public/auth/login` - User login
- `POST /api/public/auth/register/teacher` - Create teacher registration request
- `POST /api/public/register/student` - Register as student

#### File Management
- `POST /api/public/files/upload` - Upload a file
- `GET /api/public/files/download/{fileName}` - Download a file

#### Browse Teachers
- `GET /api/public/teachers` - Get all teachers with their skills and available slots
- `GET /api/public/teachers/{teacherId}` - Get specific teacher details

### Student Endpoints (Requires STUDENT Role)

- `POST /api/student/reservations` - Reserve a slot
- `POST /api/student/reservations/{reservationId}/cancel` - Cancel a reservation
- `GET /api/student/reservations` - View student's reservations

### Teacher Endpoints (Requires TEACHER Role)

- `GET /api/teacher/profile` - Get teacher profile
- `POST /api/teacher/skills` - Add a skill
- `GET /api/teacher/skills` - Get all skills
- `DELETE /api/teacher/skills/{skillId}` - Delete a skill
- `POST /api/teacher/slots/range` - Add slots from a time range
- `POST /api/teacher/slots/ranges` - Add slots from multiple time ranges
- `GET /api/teacher/slots` - Get all slots
- `DELETE /api/teacher/slots/{slotId}` - Delete a slot
- `DELETE /api/teacher/files/{fileName}` - Delete a file

### Admin Endpoints (Requires ADMIN Role)

- `POST /api/admin/teachers` - Create a teacher account
- `POST /api/admin/teacher-requests/{requestId}/approve` - Approve teacher registration request

## Usage Examples

### 1. Request Teacher Registration

```bash
curl -X POST http://localhost:8080/api/public/auth/register/teacher \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_teacher",
    "password": "password123",
    "cvUrl": "https://example.com/cv.pdf",
    "skills": ["Mathematics", "Physics"]
  }'
```

**Important**: Time ranges must:
- Have no remaining minutes/seconds (must start and end on hour boundaries)
- Divide evenly into one-hour slots
- Example valid: 09:00:00 to 12:00:00 (3 hours = 3 slots)
- Example invalid: 09:30:00 to 12:00:00 (has remaining minutes)

### 2. Register as a Student

```bash
curl -X POST http://localhost:8080/api/public/register/student \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_student",
    "password": "password123"
  }'
```

### 3. Login

```bash
curl -X POST http://localhost:8080/api/public/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_teacher",
    "password": "password123"
  }'
```

Response will include a JWT token:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_teacher",
  "role": "TEACHER",
  "userId": 1
}
```

### 4. Browse Teachers

```bash
curl -X GET http://localhost:8080/api/public/teachers
```

### 5. Reserve a Slot (as Student)

```bash
curl -X POST http://localhost:8080/api/student/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "slotId": 1
  }'
```

### 6. Add Skill (as Teacher)

```bash
curl -X POST "http://localhost:8080/api/teacher/skills?skillName=English" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Add Availability Slots (as Teacher)

```bash
curl -X POST http://localhost:8080/api/teacher/slots/range \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "startDateTime": "2024-01-20T10:00:00",
    "endDateTime": "2024-01-20T13:00:00"
  }'
```

### 8. Upload File

```bash
curl -X POST http://localhost:8080/api/public/files/upload \
  -F "file=@/path/to/file.pdf"
```

### 9. Approve Teacher Registration (as Admin)

```bash
curl -X POST http://localhost:8080/api/admin/teacher-requests/1/approve \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### 10. Create Teacher Account (as Admin)

```bash
curl -X POST http://localhost:8080/api/admin/teachers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -d '{
    "username": "mary_teacher",
    "password": "password123",
    "cvUrl": "https://example.com/cv.pdf"
  }'
```

## Project Structure

```
src/main/java/net/kilmerx/trs/
├── TrsApplication.java
├── controller/
│   ├── AuthenticationController.java
│   ├── FileController.java
│   ├── TeacherController.java
│   ├── StudentController.java
│   └── PublicController.java
├── model/
│   ├── User.java
│   ├── Student.java
│   ├── Teacher.java
│   ├── Admin.java
│   ├── Slot.java
│   ├── Skill.java
│   └── Reservation.java
├── repository/
│   ├── UserRepository.java
│   ├── StudentRepository.java
│   ├── TeacherRepository.java
│   ├── AdminRepository.java
│   ├── SlotRepository.java
│   ├── SkillRepository.java
│   └── ReservationRepository.java
├── service/
│   ├── AuthenticationService.java
│   ├── SkillService.java
│   ├── SlotService.java
│   ├── ReservationService.java
│   └── FileStorageService.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
├── dto/
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── TeacherRegistrationRequest.java
│   ├── SlotRangeRequest.java
│   ├── SlotDTO.java
│   ├── SkillDTO.java
│   ├── TeacherDTO.java
│   └── ReservationRequest.java
└── util/
    └── SlotGenerator.java
```

## Database Schema

### Users Table
- id (PK)
- username (UNIQUE, NOT NULL)
- password (NOT NULL)
- email (UNIQUE, NOT NULL)
- role (ENUM: STUDENT, TEACHER, ADMIN)
- enabled (default: true)
- createdAt
- updatedAt

### Student Profile
- id (PK)
- user_id (FK, UNIQUE, ONE-TO-ONE)
- createdAt
- updatedAt

### Teacher Profile
- id (PK)
- user_id (FK, UNIQUE, ONE-TO-ONE)
- cvUrl (NOT NULL)
- createdAt
- updatedAt

### Skills
- id (PK)
- skillName (NOT NULL)
- teacher_id (FK)
- createdAt

### Slots
- id (PK)
- teacher_id (FK)
- startDateTime (NOT NULL)
- endDateTime (NOT NULL)
- available (default: true)
- createdAt
- updatedAt

### Reservations
- id (PK)
- student_id (FK)
- slot_id (FK)
- status (ENUM: ACTIVE, COMPLETED, CANCELLED)
- createdAt
- updatedAt

### Admin Profile
- id (PK)
- user_id (FK, UNIQUE, ONE-TO-ONE)
- createdAt
- updatedAt

### Teacher Registration Request
- id (PK)
- username (NOT NULL)
- passwordHash (NOT NULL)
- cvUrl (NOT NULL)
- status (ENUM: PENDING, APPROVED, REJECTED)
- createdAt
- updatedAt

### Teacher Registration Request Skills
- request_id (FK)
- skill_name (NOT NULL)

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/trs_db
spring.datasource.username=kilmerx
spring.datasource.password=kilmerx

# JWT (Change in production!)
jwt.secret=your_super_secret_key_change_this_in_production_with_at_least_256_bits
jwt.expiration=86400000

# File Upload
file.upload.dir=uploads/
spring.servlet.multipart.max-file-size=10MB

# Admin seed
app.admin.username=admin
app.admin.password=admin123
app.admin.email=admin@trs.local
```

## Error Handling

The application provides meaningful error messages:
- `400 Bad Request` - Invalid input or logic errors
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - Resource not found
- `409 Conflict` - Username/email already taken

## Docker Management

### View logs
```bash
docker logs trs-postgres
```

### Stop the container
```bash
docker-compose down
```

### Remove all data
```bash
docker-compose down -v
```

## Future Enhancements

- Email notifications for reservations
- Rating system for teachers
- Recurring slots
- Admin dashboard
- API documentation with Swagger
- Unit and integration tests
