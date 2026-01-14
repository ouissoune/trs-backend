package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.model.Admin;
import net.kilmerx.trs.model.User;
import net.kilmerx.trs.repository.AdminRepository;
import net.kilmerx.trs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@trs.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        Optional<User> existingUser = userRepository.findByUsername(adminUsername);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getRole() != User.UserRole.ADMIN) {
                log.warn("Seed admin username '{}' exists but is not an admin; skipping seeding", adminUsername);
                return;
            }

            if (adminRepository.findByUserId(user.getId()).isEmpty()) {
                adminRepository.save(Admin.builder().user(user).build());
                log.info("Seed admin profile created for existing user '{}'", adminUsername);
            } else {
                log.info("Seed admin already exists: '{}'", adminUsername);
            }
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.warn("Seed admin email '{}' already exists; skipping seeding", adminEmail);
            return;
        }

        User user = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .role(User.UserRole.ADMIN)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        adminRepository.save(Admin.builder().user(user).build());

        log.info("Seed admin created: '{}'", adminUsername);
    }
}
