package org.openlake.projectmanagerbackend.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.entities.User;
import org.openlake.projectmanagerbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/all/{username}")
    public User getUserById(@PathVariable @Valid String username) {
        return userService.getUser(username);
    }

    @DeleteMapping("/all/{username}")
    public void deleteUser(@PathVariable @Valid String username) {
        userService.deleteUser(username);
    }

    @PostMapping("/create")
    public void createUser(@RequestBody @Valid User user) {
        System.out.println("in resource");
        userService.createUser(user);
    }
}
