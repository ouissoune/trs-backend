package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.AuthRequest;
import net.kilmerx.trs.dto.AuthResponse;
import net.kilmerx.trs.dto.TeacherRegistrationRequest;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.model.User;
import net.kilmerx.trs.repository.TeacherRepository;
import net.kilmerx.trs.repository.UserRepository;
import net.kilmerx.trs.security.CustomUserDetails;
import net.kilmerx.trs.security.JwtTokenProvider;
import net.kilmerx.trs.util.SlotGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SkillService skillService;

    @Transactional
    public AuthResponse registerTeacher(TeacherRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(User.UserRole.TEACHER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Create teacher profile
        Teacher teacher = Teacher.builder()
                .user(user)
                .cvUrl(request.getCvUrl())
                .build();

        teacher = teacherRepository.save(teacher);

        // Add skills
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            for (String skillName : request.getSkills()) {
                skillService.addSkill(teacher.getId(), skillName);
            }
        }

        // Add slots from ranges
        if (request.getSlotRanges() != null && !request.getSlotRanges().isEmpty()) {
            try {
                var slots = SlotGenerator.generateSlotsFromRanges(request.getSlotRanges(), teacher);
                teacher.getAvailableSlots().addAll(slots);
                teacherRepository.save(teacher);
            } catch (Exception e) {
                log.error("Error adding slots for teacher: {}", e.getMessage());
                throw new RuntimeException("Invalid slot ranges: " + e.getMessage());
            }
        }

        // Generate token
        String token = tokenProvider.generateToken(request.getUsername(), java.util.Map.of("userId", user.getId()));

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().toString())
                .userId(user.getId())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = tokenProvider.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().toString())
                .userId(user.getId())
                .build();
    }
}
