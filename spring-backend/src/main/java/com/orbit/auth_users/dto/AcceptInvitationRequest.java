package com.orbit.auth_users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    @NotBlank(message = "Token must be provided.")
    private String token;

    @NotBlank(message = "New Password is required.")
    @Size(min = 8, max = 64)
    private String newPassword;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
