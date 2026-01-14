package net.kilmerx.trs.controller;

import lombok.RequiredArgsConstructor;
import net.kilmerx.trs.dto.AuthRequest;
import net.kilmerx.trs.dto.AuthResponse;
import net.kilmerx.trs.dto.TeacherRegistrationRequestCreate;
import net.kilmerx.trs.dto.TeacherRegistrationRequestStatus;
import net.kilmerx.trs.service.AuthenticationService;
import net.kilmerx.trs.service.TeacherRegistrationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TeacherRegistrationRequestService registrationRequestService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<TeacherRegistrationRequestStatus> registerTeacherRequest(
            @RequestBody TeacherRegistrationRequestCreate request) {
        try {
            TeacherRegistrationRequestStatus response = registrationRequestService.createRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }
}
