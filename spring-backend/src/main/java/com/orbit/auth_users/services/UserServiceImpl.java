package com.orbit.auth_users.services;

import com.orbit.auth_users.dto.AcceptInvitationRequest;
import com.orbit.auth_users.dto.UserDTO;
import com.orbit.auth_users.entity.User;
import com.orbit.auth_users.repository.UserRepository;
import com.orbit.email_notification.dto.NotificationDTO;
import com.orbit.email_notification.services.NotificationService;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository _userRepository;
    private final PasswordEncoder _passwordEncoder;
    private final ModelMapper _modelMapper;
    private final NotificationService notificationService;
    private final TemplateEngine templateEngine;

    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return _userRepository.findByEmail(email).orElseThrow(()-> new NotFoundException("User not found."));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        List<User> users = _userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDTOS = _modelMapper.map(users, new TypeToken<List<UserDTO>>(){}.getType());
        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All Users Found")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = _modelMapper.map(user, UserDTO.class);
        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {

        User user = getCurrentLoggedInUser();

        if (userDTO.getName() != null && !userDTO.getName().isBlank()) {
            user.setName(userDTO.getName().trim());
        }

        if (userDTO.getPhoneNumber() != null
                && !userDTO.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(userDTO.getPhoneNumber().trim());
        }

        if (userDTO.getPassword() != null
                && !userDTO.getPassword().isBlank()) {
            user.setPassword(_passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getEmail() != null
                && !userDTO.getEmail().isBlank()
                && !userDTO.getEmail().trim().equalsIgnoreCase(user.getEmail())) {

            String newEmail = userDTO.getEmail().trim();

            if (_userRepository.findByEmail(newEmail).isPresent()) {
                throw new BadRequestException("Email already exists");
            }

            user.setEmail(newEmail);
        }

        Instant now = Instant.now();
        user.setUpdatedAt(now);
        _userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account Updated")
                .build();
    }

    @Override
    public Response<?> updateUserStatus(Long userId, boolean isActive) {

        User currentUser = getCurrentLoggedInUser();
        User user = _userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (currentUser.getId().equals(userId) && !isActive) {
            throw new BadRequestException("You cannot deactivate your own account.");
        }

        if (isActive && user.getPassword() == null) {
            throw new BadRequestException("User must accept the invitation before activation.");
        }

        if (!isActive && user.isActive() && user.getRoles().contains("ADMIN")) {
            long activeAdmins = _userRepository.countByRolesContainingAndActiveTrue("ADMIN");
            if (activeAdmins <= 1) {
                throw new BadRequestException("Cannot deactivate the last active administrator.");
            }
        }

        user.setActive(isActive);
        user.setUpdatedAt(Instant.now());

        _userRepository.save(user);

        String message = isActive ? "User account activated successfully." : "User account deactivated successfully.";

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    @Override
    public Response<?> acceptInvitation(AcceptInvitationRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        String hashedToken = sha256Hex(request.getToken());

        User user = _userRepository.findByInvitationTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired invitation."));

        if (user.getInvitationExpiresAt() == null ||
                !user.getInvitationExpiresAt().isAfter(Instant.now())) {
            throw new BadRequestException("Invalid or expired invitation.");
        }

        Instant now = Instant.now();
        user.setPassword(_passwordEncoder.encode(request.getNewPassword()));
        user.setActive(true);
        user.setInvitationAcceptedAt(now);
        user.setInvitationExpiresAt(null);
        user.setInvitationTokenHash(null);
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(now);
        user.setUpdatedAt(now);
        _userRepository.save(user);

        sendAccountActivationEmail(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your account has been successfully activated.")
                .build();
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

    private void sendAccountActivationEmail(User user) {
        String subject = "Account Activation Email";
        Context context = new Context();
        context.setVariable("customerName", user.getName());

        String emailBody = templateEngine.process("account-activation", context);

        notificationService.sendEmail(NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());
    }
}
