package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.AdminTeacherCreateRequest;
import net.kilmerx.trs.dto.AdminTeacherCreateResponse;
import net.kilmerx.trs.dto.TeacherRegistrationRequestCreate;
import net.kilmerx.trs.dto.TeacherRegistrationRequestStatus;
import net.kilmerx.trs.dto.TeacherRegistrationRequestSummary;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.model.TeacherRegistrationRequestEntity;
import net.kilmerx.trs.model.User;
import net.kilmerx.trs.repository.TeacherRegistrationRequestRepository;
import net.kilmerx.trs.repository.TeacherRepository;
import net.kilmerx.trs.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherRegistrationRequestService {

    private static final String DEFAULT_TEACHER_EMAIL_DOMAIN = "@trs.local";
    private static final String DEFAULT_CV_URL = "pending";

    private final TeacherRegistrationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final SkillService skillService;

    @Transactional
    public TeacherRegistrationRequestStatus createRequest(TeacherRegistrationRequestCreate request) {
        validateRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (requestRepository.existsByUsernameAndStatus(
                request.getUsername(),
                TeacherRegistrationRequestEntity.RequestStatus.PENDING)) {
            throw new RuntimeException("Registration request already pending");
        }

        TeacherRegistrationRequestEntity entity = TeacherRegistrationRequestEntity.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .cvUrl(request.getCvUrl())
                .skills(normalizeSkills(request.getSkills()))
                .status(TeacherRegistrationRequestEntity.RequestStatus.PENDING)
                .build();

        entity = requestRepository.save(entity);
        log.info("Teacher registration request created: {}", entity.getId());

        return TeacherRegistrationRequestStatus.builder()
                .requestId(entity.getId())
                .username(entity.getUsername())
                .status(entity.getStatus().name())
                .build();
    }

    @Transactional
    public TeacherRegistrationRequestStatus approveRequest(Long requestId) {
        TeacherRegistrationRequestEntity entity = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (entity.getStatus() != TeacherRegistrationRequestEntity.RequestStatus.PENDING) {
            throw new RuntimeException("Registration request is not pending");
        }

        if (userRepository.existsByUsername(entity.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        String email = entity.getUsername() + DEFAULT_TEACHER_EMAIL_DOMAIN;
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .username(entity.getUsername())
                .password(entity.getPasswordHash())
                .email(email)
                .role(User.UserRole.TEACHER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(user)
                .cvUrl(entity.getCvUrl())
                .build();

        teacher = teacherRepository.save(teacher);

        for (String skillName : entity.getSkills()) {
            skillService.addSkill(teacher.getId(), skillName);
        }

        entity.setStatus(TeacherRegistrationRequestEntity.RequestStatus.APPROVED);
        requestRepository.save(entity);

        log.info("Teacher registration request approved: {}", entity.getId());

        return TeacherRegistrationRequestStatus.builder()
                .requestId(entity.getId())
                .username(entity.getUsername())
                .status(entity.getStatus().name())
                .build();
    }

    @Transactional
    public AdminTeacherCreateResponse createTeacherByAdmin(AdminTeacherCreateRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new RuntimeException("Username and password are required");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        String email = request.getUsername() + DEFAULT_TEACHER_EMAIL_DOMAIN;
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(email)
                .role(User.UserRole.TEACHER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        String cvUrl = isBlank(request.getCvUrl()) ? DEFAULT_CV_URL : request.getCvUrl();
        Teacher teacher = Teacher.builder()
                .user(user)
                .cvUrl(cvUrl)
                .build();

        teacher = teacherRepository.save(teacher);

        log.info("Teacher created by admin: {}", user.getUsername());

        return AdminTeacherCreateResponse.builder()
                .userId(user.getId())
                .teacherId(teacher.getId())
                .username(user.getUsername())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TeacherRegistrationRequestSummary> listRequests(String status) {
        List<TeacherRegistrationRequestEntity> entities;
        if (isBlank(status)) {
            entities = requestRepository.findAll();
        } else {
            TeacherRegistrationRequestEntity.RequestStatus parsedStatus =
                    TeacherRegistrationRequestEntity.RequestStatus.valueOf(status.trim().toUpperCase());
            entities = requestRepository.findAllByStatus(parsedStatus);
        }
        return entities.stream()
                .map(entity -> TeacherRegistrationRequestSummary.builder()
                        .requestId(entity.getId())
                        .username(entity.getUsername())
                        .cvUrl(entity.getCvUrl())
                        .skills(entity.getSkills())
                        .status(entity.getStatus().name())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void validateRequest(TeacherRegistrationRequestCreate request) {
        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getPassword())
                || isBlank(request.getCvUrl())) {
            throw new RuntimeException("Username, password, and cvUrl are required");
        }
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) {
            return java.util.Collections.emptyList();
        }
        return skills.stream()
                .filter(skill -> !isBlank(skill))
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
