package net.kilmerx.trs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher_registration_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRegistrationRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String cvUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "teacher_registration_request_skills",
            joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "skill_name", nullable = false)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
