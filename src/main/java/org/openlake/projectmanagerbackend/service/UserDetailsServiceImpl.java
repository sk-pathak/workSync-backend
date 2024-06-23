package org.openlake.projectmanagerbackend.service;

import lombok.AllArgsConstructor;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;
import org.openlake.projectmanagerbackend.repo.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
