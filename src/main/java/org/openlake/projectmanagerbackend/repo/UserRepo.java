package org.openlake.projectmanagerbackend.repo;

import org.openlake.projectmanagerbackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
