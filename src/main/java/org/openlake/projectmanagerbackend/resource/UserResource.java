package org.openlake.projectmanagerbackend.resource;

import org.openlake.projectmanagerbackend.domain.User;
import org.openlake.projectmanagerbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserResource {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/all/{username}")
    public User getUserById(@PathVariable String username) {
        return userService.getUser(username);
    }

    @DeleteMapping("/all/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }

    @PostMapping("/add")
    public void addUser(@RequestBody User user) {
        userService.createUser(user);
    }
}
