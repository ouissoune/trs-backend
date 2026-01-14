package net.kilmerx.trs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.model.Reservation;
import net.kilmerx.trs.model.Slot;
import net.kilmerx.trs.model.Student;
import net.kilmerx.trs.repository.ReservationRepository;
import net.kilmerx.trs.repository.SlotRepository;
import net.kilmerx.trs.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StudentRepository studentRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;

    @Transactional
    public Reservation reserveSlot(Long studentId, Long slotId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getAvailable()) {
            throw new RuntimeException("Slot is not available");
        }

        // Check if student already reserved this slot
        reservationRepository.findByStudentAndSlotId(student, slotId)
                .ifPresent(r -> {
                    throw new RuntimeException("Student already has a reservation for this slot");
                });

        Reservation reservation = Reservation.builder()
                .student(student)
                .slot(slot)
                .status(Reservation.ReservationStatus.ACTIVE)
                .build();

        reservation = reservationRepository.save(reservation);
        slotService.markSlotUnavailable(slotId);

        log.info("Reservation created - Student: {}, Slot: {}", studentId, slotId);

        return reservation;
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long studentId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Reservation does not belong to this student");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Mark slot as available again
        Slot slot = reservation.getSlot();
        slot.setAvailable(true);
        slotRepository.save(slot);

        log.info("Reservation {} cancelled", reservationId);
    }

    public List<Reservation> getStudentReservations(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return reservationRepository.findByStudent(student);
    }
}
