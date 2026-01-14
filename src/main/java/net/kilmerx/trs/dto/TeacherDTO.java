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
public class TeacherDTO {
    private Long id;
    private Long userId;
    private String username;
    private String cvUrl;
    private List<SkillDTO> skills;
    private List<SlotDTO> availableSlots;
}
