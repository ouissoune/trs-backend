package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.SlotDTO;
import net.kilmerx.trs.dto.SlotRangeRequest;
import net.kilmerx.trs.model.Slot;
import net.kilmerx.trs.model.Teacher;
import net.kilmerx.trs.repository.SlotRepository;
import net.kilmerx.trs.repository.TeacherRepository;
import net.kilmerx.trs.util.SlotGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {

    private final SlotRepository slotRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public List<SlotDTO> addSlotsFromRange(Long teacherId, SlotRangeRequest rangeRequest) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        try {
            List<Slot> slots = SlotGenerator.generateSlotsFromRange(rangeRequest, teacher);
            slots = slotRepository.saveAll(slots);
            teacher.getAvailableSlots().addAll(slots);
            teacherRepository.save(teacher);

            log.info("Added {} slots for teacher: {}", slots.size(), teacherId);

            return slots.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid slot range: " + e.getMessage());
        }
    }

    @Transactional
    public List<SlotDTO> addSlotsFromRanges(Long teacherId, List<SlotRangeRequest> ranges) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<SlotDTO> allSlots = new java.util.ArrayList<>();

        for (SlotRangeRequest range : ranges) {
            try {
                allSlots.addAll(addSlotsFromRange(teacherId, range));
            } catch (Exception e) {
                log.error("Error adding slots for range: {}", e.getMessage());
                throw new RuntimeException("Error processing slot ranges: " + e.getMessage());
            }
        }

        return allSlots;
    }

    public List<SlotDTO> getTeacherSlots(Long teacherId) {
        return slotRepository.findByTeacherId(teacherId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SlotDTO> getAvailableSlots(Long teacherId) {
        return slotRepository.findByTeacherAndAvailable(
                teacherRepository.findById(teacherId).orElseThrow(() -> new RuntimeException("Teacher not found")),
                true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markSlotUnavailable(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slot.setAvailable(false);
        slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slotRepository.deleteById(slotId);
        log.info("Slot {} deleted", slotId);
    }

    private SlotDTO convertToDTO(Slot slot) {
        return SlotDTO.builder()
                .id(slot.getId())
                .startDateTime(slot.getStartDateTime())
                .endDateTime(slot.getEndDateTime())
                .available(slot.getAvailable())
                .createdAt(slot.getCreatedAt())
                .build();
    }
}
