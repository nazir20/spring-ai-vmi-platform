package com.orbit.auth_users.controller;

import com.orbit.auth_users.dto.LoginRequest;
import com.orbit.auth_users.dto.RegistrationRequest;
import com.orbit.auth_users.services.AuthService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> register(@RequestBody @Valid RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<?>> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/resend-invitation")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> resendInvitation(@Valid @RequestBody @NotBlank @Email String email) {
        return ResponseEntity.ok(authService.resendInvitation(email));
    }
}
