package com.orbit.auth_users.services;

import com.orbit.auth_users.dto.AcceptInvitationRequest;
import com.orbit.auth_users.dto.UserDTO;
import com.orbit.auth_users.entity.User;
import com.orbit.response.Response;

import java.util.List;

public interface UserService {
    User getCurrentLoggedInUser();
    Response<List<UserDTO>> getAllUsers();
    Response<UserDTO> getOwnAccountDetails();
    Response<?> updateOwnAccount(UserDTO userDTO);
    Response<?> updateUserStatus(Long userId, boolean isActive);
    Response<?> acceptInvitation(AcceptInvitationRequest request);
}
