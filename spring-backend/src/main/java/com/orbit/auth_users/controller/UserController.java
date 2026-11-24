package com.orbit.auth_users.controller;

import com.orbit.auth_users.dto.AcceptInvitationRequest;
import com.orbit.auth_users.dto.UserDTO;
import com.orbit.auth_users.services.UserService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/accept-invitation")
    public ResponseEntity<Response<?>> acceptInvitation(@RequestBody @Valid AcceptInvitationRequest request){
        return ResponseEntity.ok(userService.acceptInvitation(request));
    }

    @GetMapping("/account")
    public ResponseEntity<Response<UserDTO>> getOwnAccount(){
        return ResponseEntity.ok(userService.getOwnAccountDetails());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/update")
    public ResponseEntity<Response<?>> updateOwnAccount(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }

    @PutMapping("/update-status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<?>> updateStatus(@RequestBody Long userId, boolean isActive) {
        return  ResponseEntity.ok(userService.updateUserStatus(userId, isActive));
    }
}
