package net.kilmerx.trs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.AuthRequest;
import net.kilmerx.trs.dto.TeacherDTO;
import net.kilmerx.trs.dto.TeacherRegistrationRequestCreate;
import net.kilmerx.trs.dto.TeacherRegistrationRequestStatus;
import net.kilmerx.trs.model.Student;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.model.User;
import net.kilmerx.trs.repository.StudentRepository;
import net.kilmerx.trs.repository.TeacherRepository;
import net.kilmerx.trs.repository.UserRepository;
import net.kilmerx.trs.service.TeacherRegistrationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRegistrationRequestService registrationRequestService;

    @PostMapping("/register/student")
    public ResponseEntity<String> registerStudent(@RequestBody AuthRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username already taken");
            }

            // Create user
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getUsername() + "@trs.local") // Default email
                    .role(User.UserRole.STUDENT)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);

            // Create student profile
            Student student = Student.builder()
                    .user(user)
                    .build();

            studentRepository.save(student);

            log.info("Student registered: {}", request.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Student registered successfully");
        } catch (Exception e) {
            log.error("Error registering student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error registering student: " + e.getMessage());
        }
    }

    @PostMapping("/teacher-requests")
    public ResponseEntity<TeacherRegistrationRequestStatus> createTeacherRegistrationRequest(
            @RequestBody TeacherRegistrationRequestCreate request) {
        try {
            TeacherRegistrationRequestStatus response = registrationRequestService.createRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating teacher registration request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        try {
            List<TeacherDTO> teachers = teacherRepository.findAll().stream()
                    .map(teacher -> TeacherDTO.builder()
                            .id(teacher.getId())
                            .userId(teacher.getUser().getId())
                            .username(teacher.getUser().getUsername())
                            .cvUrl(teacher.getCvUrl())
                            .skills(teacher.getSkills().stream()
                                    .map(s -> new net.kilmerx.trs.dto.SkillDTO(s.getId(), s.getSkillName()))
                                    .collect(Collectors.toList()))
                            .availableSlots(teacher.getAvailableSlots().stream()
                                    .filter(slot -> slot.getAvailable())
                                    .map(slot -> new net.kilmerx.trs.dto.SlotDTO(
                                            slot.getId(),
                                            slot.getStartDateTime(),
                                            slot.getEndDateTime(),
                                            slot.getAvailable(),
                                            slot.getCreatedAt()))
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error fetching teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<TeacherDTO> getTeacher(@PathVariable Long teacherId) {
        try {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            TeacherDTO dto = TeacherDTO.builder()
                    .id(teacher.getId())
                    .userId(teacher.getUser().getId())
                    .username(teacher.getUser().getUsername())
                    .cvUrl(teacher.getCvUrl())
                    .skills(teacher.getSkills().stream()
                            .map(s -> new net.kilmerx.trs.dto.SkillDTO(s.getId(), s.getSkillName()))
                            .collect(Collectors.toList()))
                    .availableSlots(teacher.getAvailableSlots().stream()
                            .filter(slot -> slot.getAvailable())
                            .map(slot -> new net.kilmerx.trs.dto.SlotDTO(
                                    slot.getId(),
                                    slot.getStartDateTime(),
                                    slot.getEndDateTime(),
                                    slot.getAvailable(),
                                    slot.getCreatedAt()))
                            .collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error fetching teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
