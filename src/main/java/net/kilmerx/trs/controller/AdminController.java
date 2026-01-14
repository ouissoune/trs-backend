package net.kilmerx.trs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kilmerx.trs.dto.AdminTeacherCreateRequest;
import net.kilmerx.trs.dto.AdminTeacherCreateResponse;
import net.kilmerx.trs.dto.TeacherRegistrationRequestStatus;
import net.kilmerx.trs.dto.TeacherRegistrationRequestSummary;
import net.kilmerx.trs.service.TeacherRegistrationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final TeacherRegistrationRequestService registrationRequestService;

    @PostMapping("/teachers")
    public ResponseEntity<AdminTeacherCreateResponse> createTeacher(@RequestBody AdminTeacherCreateRequest request) {
        try {
            AdminTeacherCreateResponse response = registrationRequestService.createTeacherByAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating teacher by admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/teacher-requests/{requestId}/approve")
    public ResponseEntity<TeacherRegistrationRequestStatus> approveTeacherRequest(@PathVariable Long requestId) {
        try {
            TeacherRegistrationRequestStatus status = registrationRequestService.approveRequest(requestId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error approving teacher request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/teacher-requests")
    public ResponseEntity<List<TeacherRegistrationRequestSummary>> listTeacherRequests(
            @RequestParam(required = false) String status) {
        try {
            List<TeacherRegistrationRequestSummary> requests = registrationRequestService.listRequests(status);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            log.error("Invalid teacher request status: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error listing teacher requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
