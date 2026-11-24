package com.orbit.role.services;

import com.orbit.response.Response;
import com.orbit.role.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    Response<RoleDTO> createRole(RoleDTO roleDTO);
    Response<RoleDTO> updateRole(RoleDTO roleDTO);
    Response<?> deleteRole(Long roleId);
    Response<List<RoleDTO>> getAllRoles();
}
