package com.orbit.role.services;

import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.response.Response;
import com.orbit.role.dto.RoleDTO;
import com.orbit.role.entity.Role;
import com.orbit.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{

    private final RoleRepository _roleRepository;
    private final ModelMapper _modelMapper;

    @Override
    public Response<RoleDTO> createRole(RoleDTO roleDTO) {
        Role role = _modelMapper.map(roleDTO, Role.class);
        role = _roleRepository.save(role);
        return Response.<RoleDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(_modelMapper.map(role, RoleDTO.class)).build();
    }

    @Override
    public Response<RoleDTO> updateRole(RoleDTO roleDTO) {
        Role existingRole = _roleRepository.findById(roleDTO.getId())
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (_roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new BadRequestException("Role already exists");
        }

        existingRole.setName(roleDTO.getName());
        existingRole = _roleRepository.save(existingRole);


        return Response.<RoleDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role updated successfully")
                .data(_modelMapper.map(existingRole, RoleDTO.class)).build();
    }

    @Override
    public Response<?> deleteRole(Long roleId) {
        if (!_roleRepository.existsById(roleId)) {
            throw new NotFoundException("Role not found");
        }
        _roleRepository.deleteById(roleId);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role deleted successfully")
                .build();
    }

    @Override
    public Response<List<RoleDTO>> getAllRoles() {
        List<Role> roles = _roleRepository.findAll();
        List<RoleDTO> roleDTOS = roles.stream()
                .map(role -> _modelMapper.map(role, RoleDTO.class))
                .toList();

        return Response.<List<RoleDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roleDTOS).build();
    }
}
