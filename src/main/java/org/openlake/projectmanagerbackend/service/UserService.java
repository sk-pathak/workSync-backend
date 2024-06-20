package org.openlake.projectmanagerbackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.projectmanagerbackend.domain.User;
import org.openlake.projectmanagerbackend.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public User getUser(String username) {
        return userRepo.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User createUser(User user) {
        // EMPTY PROJECTLIST THEN ADD LATER
        return userRepo.save(user);
    }

    public void deleteUser(String username) {
        userRepo.delete(userRepo.findByUsername(username));
    }
}
