package net.kilmerx.trs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRegistrationRequestSummary {
    private Long requestId;
    private String username;
    private String cvUrl;
    private List<String> skills;
    private String status;
    private LocalDateTime createdAt;
}
