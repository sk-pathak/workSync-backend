package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.domain.enumeration.Role;
import org.openlake.workSync.app.dto.AuthResponseDTO;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.mapper.UserMapper;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.domain.exception.AuthenticationException;
import org.openlake.workSync.app.domain.exception.ValidationException;
import org.openlake.workSync.app.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponseDTO register(UserRequestDTO request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            throw ValidationException.usernameAlreadyTaken(request.getUsername());
        }
        if (userRepo.existsByEmail(request.getEmail())) {
            throw ValidationException.emailAlreadyRegistered(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepo.save(user);
        String token = jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setUser(userMapper.toResponseDTO(user));
        response.setExpirationTime("86400000");
        return response;
    }

    public AuthResponseDTO login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            User user = (User) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(authentication);
            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(token);
            response.setRole(user.getRole().name());
            response.setUser(userMapper.toResponseDTO(user));
            response.setExpirationTime("86400000");
            return response;
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw AuthenticationException.invalidCredentials();
        }
    }
}
