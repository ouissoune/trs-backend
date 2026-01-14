package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.SkillDTO;
import net.kilmerx.trs.model.Skill;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.repository.SkillRepository;
import net.kilmerx.trs.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public SkillDTO addSkill(Long teacherId, String skillName) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Skill skill = Skill.builder()
                .skillName(skillName)
                .teacher(teacher)
                .build();

        skill = skillRepository.save(skill);
        teacher.getSkills().add(skill);

        log.info("Skill '{}' added for teacher: {}", skillName, teacherId);

        return SkillDTO.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .build();
    }

    public List<SkillDTO> getTeacherSkills(Long teacherId) {
        return skillRepository.findByTeacherId(teacherId).stream()
                .map(skill -> SkillDTO.builder()
                        .id(skill.getId())
                        .skillName(skill.getSkillName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSkill(Long skillId, Long teacherId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Skill does not belong to this teacher");
        }

        skillRepository.deleteById(skillId);
        log.info("Skill '{}' deleted", skillId);
    }
}
