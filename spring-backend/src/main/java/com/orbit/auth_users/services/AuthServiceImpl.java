package com.orbit.auth_users.services;

import com.orbit.auth_users.dto.LoginRequest;
import com.orbit.auth_users.dto.LoginResponse;
import com.orbit.auth_users.dto.RegistrationRequest;
import com.orbit.auth_users.entity.User;
import com.orbit.auth_users.repository.UserRepository;
import com.orbit.email_notification.dto.NotificationDTO;
import com.orbit.email_notification.services.NotificationService;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.response.Response;
import com.orbit.role.entity.Role;
import com.orbit.role.repository.RoleRepository;
import com.orbit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository _userRepository;
    private final PasswordEncoder _passwordEncoder;
    private final JwtUtils _jwtUtils;
    private final RoleRepository _roleRepository;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;

    @Value("${frontend.base.url}")
    private String frontendUrl;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        if (_userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        List<Role> userRoles;
        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()) {
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> _roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role with name: " + roleName + " not found.")))
                    .toList();
        }else {
            Role defaultRole = _roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("CUSTOMER role not found."));
            userRoles = List.of(defaultRole);
        }

        String invitationToken = generateToken();
        String invitationTokenHash = sha256Hex(invitationToken);
        Instant now = Instant.now();
        Instant invitationExpiresAt = now.plus(24, ChronoUnit.HOURS);

        User newUser = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .password(_passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(userRoles)
                .invitationTokenHash(invitationTokenHash)
                .invitationExpiresAt(invitationExpiresAt)
                .isActive(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        _userRepository.save(newUser);
        sendAccountCreationEmail(newUser, invitationToken);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User registered successfully")
                .build();
    }

    @Override
    public Response<?> login(LoginRequest loginRequest) {
        User user = _userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid email or password."));

        if (!_passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new NotFoundException("Account is not active, please contact customer support.");
        }

        if (user.isMustChangePassword()) {
            throw new BadRequestException("Please first change your password.");
        }

        String token = _jwtUtils.generateToken(user.getEmail());
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleNames);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
    }

    @Override
    public Response<?> resendInvitation(String email) {
        User user = _userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            throw new BadRequestException("User has already set a password. Invitation cannot be resent.");
        }

        String invitationToken = generateToken();
        String invitationTokenHash = sha256Hex(invitationToken);
        Instant now = Instant.now();
        user.setInvitationTokenHash(invitationTokenHash);
        user.setInvitationExpiresAt(now.plus(24, ChronoUnit.HOURS));
        user.setUpdatedAt(now);

        _userRepository.save(user);

        sendAccountCreationEmail(user, invitationToken);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Invitation email resent successfully.")
                .build();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void sendAccountCreationEmail(User user, String invitationToken) {
        String subject = "Account creation email";
        Context context = new Context();
        String invitationUrl = frontendUrl + "/accept-invitation?token=" + invitationToken;

        context.setVariable("customerName", user.getName());
        context.setVariable("invitationUrl", invitationUrl);

        String emailBody = templateEngine.process("account-creation", context);

        notificationService.sendEmail(NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());
    }
}
