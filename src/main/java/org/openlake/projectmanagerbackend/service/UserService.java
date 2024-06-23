package org.openlake.projectmanagerbackend.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.projectmanagerbackend.domain.Response;
import org.openlake.projectmanagerbackend.domain.dto.User;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;
import org.openlake.projectmanagerbackend.domain.enumeration.Role;
import org.openlake.projectmanagerbackend.repo.UserRepo;
import org.openlake.projectmanagerbackend.utils.JwtUtils;
import org.openlake.projectmanagerbackend.utils.Utils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class UserService{
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public Response getAllUsers() {
        Response response = new Response();
        try{
            List<UserEntity> userEntityList = userRepo.findAll();
            List<User> users = Utils.mapUserListEntityToUserList(userEntityList);
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setUserList(users);
        }
        catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error getting all userEntities "+e.getMessage());
        }
        return response;
    }

    public Response getUserByUsername(String username) {
        Response response = new Response();

        try{
            UserEntity userEntity = userRepo.findByUsername(username).orElseThrow( ()-> new UsernameNotFoundException("Username "+username+" not found"));
            User user = Utils.mapUserEntitytoUser(userEntity);
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setUser(user);
        }
        catch(UsernameNotFoundException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }
        catch(Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public Response deleteUser(String username) {
        Response response = new Response();
        try{
            userRepo.delete(userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username+" not found")));
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setMessage("Deleted user "+username);
        }
        catch (UsernameNotFoundException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }
        catch(Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public Response createUser(UserEntity userEntity) {
        // EMPTY PROJECT LIST THEN ADD LATER
        Response response = new Response();
        try{
            if(userEntity.getRole() == null || userEntity.getRole().name().isBlank()){
                userEntity.setRole(Role.USER);
            }
            if(userRepo.existsByUsername(userEntity.getUsername())){
                throw new Exception(userEntity.getUsername()+" already exists");
            }
            userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));
            UserEntity savedUserEntity = userRepo.save(userEntity);
            User savedUser = Utils.mapUserEntitytoUser(savedUserEntity);
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setUser(savedUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public Response loginUser(@NotBlank String username, @NotBlank String password) {
        Response response = new Response();

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            var user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username "+username+" not found"));

            var token = jwtUtils.generateToken(user);
            response.setStatusCode(200);
            response.setToken(token);
            response.setRole(user.getRole().name());
            response.setExpirationTime("7 Days");
            response.setMessage("successful");
        }
        catch (UsernameNotFoundException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
