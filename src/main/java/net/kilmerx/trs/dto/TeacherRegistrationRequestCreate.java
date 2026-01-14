package net.kilmerx.trs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherRegistrationRequestCreate {
    private String username;
    private String password;
    private String cvUrl;
    private List<String> skills;
}
