package com.orbit.auth_users.services;

import com.orbit.auth_users.dto.LoginRequest;
import com.orbit.auth_users.dto.RegistrationRequest;
import com.orbit.response.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);
    Response<?> login(LoginRequest loginRequest);
    Response<?> resendInvitation(String email);
}
