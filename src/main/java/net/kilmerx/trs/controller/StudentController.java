package net.kilmerx.trs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.ReservationRequest;
import net.kilmerx.trs.model.Reservation;
import net.kilmerx.trs.model.Student;
import net.kilmerx.trs.repository.StudentRepository;
import net.kilmerx.trs.security.CustomUserDetails;
import net.kilmerx.trs.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentRepository studentRepository;
    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<Reservation> reserveSlot(
            @RequestBody ReservationRequest request,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Student student = studentRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found"));

            Reservation reservation = reservationService.reserveSlot(student.getId(), request.getSlotId());
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (Exception e) {
            log.error("Error reserving slot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Student student = studentRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            reservationService.cancelReservation(reservationId, student.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error cancelling reservation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Reservation>> getReservations(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Student student = studentRepository.findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Reservation> reservations = reservationService.getStudentReservations(student.getId());
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error fetching reservations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
