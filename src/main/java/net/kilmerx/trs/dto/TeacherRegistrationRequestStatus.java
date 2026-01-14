package net.kilmerx.trs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRegistrationRequestStatus {
    private Long requestId;
    private String username;
    private String status;
}
