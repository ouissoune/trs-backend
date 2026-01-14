package net.kilmerx.trs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.SkillDTO;
import net.kilmerx.trs.dto.SlotDTO;
import net.kilmerx.trs.dto.SlotRangeRequest;
import net.kilmerx.trs.dto.TeacherDTO;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.repository.TeacherRepository;
import net.kilmerx.trs.security.CustomUserDetails;
import net.kilmerx.trs.service.SkillService;
import net.kilmerx.trs.service.SlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final SkillService skillService;
    private final SlotService slotService;

    @GetMapping("/profile")
    public ResponseEntity<TeacherDTO> getProfile(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

            TeacherDTO dto = TeacherDTO.builder()
                    .id(teacher.getId())
                    .userId(teacher.getUser().getId())
                    .username(teacher.getUser().getUsername())
                    .cvUrl(teacher.getCvUrl())
                    .skills(teacher.getSkills().stream()
                            .map(s -> SkillDTO.builder()
                                    .id(s.getId())
                                    .skillName(s.getSkillName())
                                    .build())
                            .collect(Collectors.toList()))
                    .availableSlots(teacher.getAvailableSlots().stream()
                            .map(slot -> SlotDTO.builder()
                                    .id(slot.getId())
                                    .startDateTime(slot.getStartDateTime())
                                    .endDateTime(slot.getEndDateTime())
                                    .available(slot.getAvailable())
                                    .createdAt(slot.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error fetching teacher profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/skills")
    public ResponseEntity<SkillDTO> addSkill(
            @RequestParam String skillName,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            SkillDTO skill = skillService.addSkill(teacher.getId(), skillName);
            return ResponseEntity.status(HttpStatus.CREATED).body(skill);
        } catch (Exception e) {
            log.error("Error adding skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/skills")
    public ResponseEntity<List<SkillDTO>> getSkills(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            List<SkillDTO> skills = skillService.getTeacherSkills(teacher.getId());
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("Error fetching skills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long skillId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            skillService.deleteSkill(skillId, teacher.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting skill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/slots/range")
    public ResponseEntity<List<SlotDTO>> addSlotRange(
            @RequestBody SlotRangeRequest rangeRequest,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            List<SlotDTO> slots = slotService.addSlotsFromRange(teacher.getId(), rangeRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(slots);
        } catch (Exception e) {
            log.error("Error adding slot range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/slots/ranges")
    public ResponseEntity<List<SlotDTO>> addSlotRanges(
            @RequestBody List<SlotRangeRequest> rangeRequests,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            List<SlotDTO> slots = slotService.addSlotsFromRanges(teacher.getId(), rangeRequests);
            return ResponseEntity.status(HttpStatus.CREATED).body(slots);
        } catch (Exception e) {
            log.error("Error adding slot ranges: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/slots")
    public ResponseEntity<List<SlotDTO>> getSlots(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Teacher teacher = teacherRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));

            List<SlotDTO> slots = slotService.getTeacherSlots(teacher.getId());
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error fetching slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long slotId,
            Authentication authentication) {
        try {
            slotService.deleteSlot(slotId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting slot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
