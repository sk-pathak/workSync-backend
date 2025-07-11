package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.dto.UserResponseDTO;
import org.openlake.workSync.app.mapper.UserMapper;
import org.openlake.workSync.app.mapper.ProjectMapper;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.repo.ProjectRepo;
import org.openlake.workSync.app.domain.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final ProjectRepo projectRepo;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO getUserById(UUID id) {
        return userRepo.findById(id)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));
    }

    public User loadUserById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));
    }

    public PagedResponse<UserResponseDTO> listUsers(Pageable pageable) {
        Page<User> page = userRepo.findAll(pageable);
        return new PagedResponse<>(page.map(userMapper::toResponseDTO));
    }

    public UserResponseDTO updateUser(UUID id, UserRequestDTO request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(id));
        userMapper.updateUserFromDTO(request, user);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepo.save(user);
        return userMapper.toResponseDTO(user);
    }

    public void deleteUser(UUID id) {
        if (!userRepo.existsById(id)) {
            throw ResourceNotFoundException.userNotFound(id);
        }
        userRepo.deleteById(id);
    }

    public PagedResponse<?> getOwnedProjects(UUID userId, Pageable pageable) {
        return new PagedResponse<>(projectRepo.findByOwnerId(userId, pageable).map(projectMapper::toResponseDTO));
    }

    public PagedResponse<?> getJoinedProjects(UUID userId, Pageable pageable) {
        return new PagedResponse<>(projectRepo.findByMemberId(userId, pageable).map(projectMapper::toResponseDTO));
    }

    public PagedResponse<?> getStarredProjects(UUID userId, Pageable pageable) {
        return new PagedResponse<>(projectRepo.findByStarredUserId(userId, pageable).map(projectMapper::toResponseDTO));
    }
}
