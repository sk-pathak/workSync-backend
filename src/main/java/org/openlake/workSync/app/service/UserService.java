package org.openlake.workSync.app.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.domain.dto.AuthResponse;
import org.openlake.workSync.app.domain.dto.User;
import org.openlake.workSync.app.domain.entity.UserEntity;
import org.openlake.workSync.app.domain.enumeration.Role;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.utils.JwtUtils;
import org.openlake.workSync.app.utils.Utils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class UserService{
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthResponse getAllUsers() {
        AuthResponse authResponse = new AuthResponse();
        try{
            List<UserEntity> userEntityList = userRepo.findAll();
            List<User> users = Utils.mapUserListEntityToUserList(userEntityList);
            authResponse.setStatusCode(200);
            authResponse.setMessage("Success");
            authResponse.setUserList(users);
        }
        catch(Exception e){
            authResponse.setStatusCode(500);
            authResponse.setMessage("Error getting all userEntities "+e.getMessage());
        }
        return authResponse;
    }

    public AuthResponse getUserByUsername(String username) {
        AuthResponse authResponse = new AuthResponse();

        try{
            UserEntity userEntity = userRepo.findByUsername(username).orElseThrow( ()-> new UsernameNotFoundException("Username "+username+" not found"));
            User user = Utils.mapUserEntitytoUser(userEntity);
            authResponse.setStatusCode(200);
            authResponse.setMessage("Success");
            authResponse.setUser(user);
        }
        catch(UsernameNotFoundException e){
            authResponse.setStatusCode(404);
            authResponse.setMessage(e.getMessage());
        }
        catch(Exception e){
            authResponse.setStatusCode(500);
            authResponse.setMessage(e.getMessage());
        }
        return authResponse;
    }

    public AuthResponse deleteUser(String username) {
        AuthResponse authResponse = new AuthResponse();
        try{
            userRepo.delete(userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username+" not found")));
            authResponse.setStatusCode(200);
            authResponse.setMessage("Success, Deleted user "+username);
        }
        catch (UsernameNotFoundException e){
            authResponse.setStatusCode(404);
            authResponse.setMessage(e.getMessage());
        }
        catch(Exception e){
            authResponse.setStatusCode(500);
            authResponse.setMessage(e.getMessage());
        }
        return authResponse;
    }

    public AuthResponse createUser(UserEntity userEntity, MultipartFile image) {
        AuthResponse authResponse = new AuthResponse();
        try{
            if(userEntity.getRole() == null || userEntity.getRole().name().isBlank()){
                userEntity.setRole(Role.USER);
            }
            if(userRepo.existsByUsername(userEntity.getUsername())){
                throw new Exception(userEntity.getUsername()+" already exists");
            }
            userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));
            if(image!=null){
                try {
                    if(image.getSize() > 5*1024*1024){
                        authResponse.setStatusCode(400);
                        authResponse.setMessage("Image too large");
                        return authResponse;
                    }
                    String imagePath = Utils.saveImage(image);
                    userEntity.setUserProfile(imagePath);
                }
                catch (IOException e){
                    authResponse.setStatusCode(400);
                    authResponse.setMessage(e.getMessage());
                    return authResponse;
                }
            }
            UserEntity savedUserEntity = userRepo.save(userEntity);
            User savedUser = Utils.mapUserEntitytoUser(savedUserEntity);

            AuthResponse loginAuthResponse = loginUser(userEntity.getUsername(), savedUser.getPassword());
            if(loginAuthResponse.getStatusCode() == 200){
                authResponse.setStatusCode(201);
                authResponse.setUser(savedUser);
                authResponse.setToken(loginAuthResponse.getToken());
                authResponse.setRole(loginAuthResponse.getRole());
                authResponse.setExpirationTime(loginAuthResponse.getExpirationTime());
                authResponse.setMessage("Success, saved user and logged in");
            }
            else if(loginAuthResponse.getStatusCode() == 500) {
                authResponse.setStatusCode(201);
                authResponse.setUser(savedUser);
                authResponse.setMessage("Success, saved user but failed to login");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return authResponse;
    }

    public AuthResponse loginUser(@NotBlank String username, @NotBlank String password) {
        AuthResponse authResponse = new AuthResponse();

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            var user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username "+username+" not found"));

            var token = jwtUtils.generateToken(user);
            authResponse.setStatusCode(200);
            authResponse.setToken(token);
            authResponse.setRole(user.getRole().name());
            authResponse.setUser(Utils.mapUserEntitytoUser(user));
            authResponse.setExpirationTime("7 Days");
            authResponse.setMessage("successful");
        }
        catch (UsernameNotFoundException e){
            authResponse.setStatusCode(404);
            authResponse.setMessage(e.getMessage());
        }
        catch (Exception e){
            authResponse.setStatusCode(500);
            authResponse.setMessage(e.getMessage());
        }
        return authResponse;
    }
}
