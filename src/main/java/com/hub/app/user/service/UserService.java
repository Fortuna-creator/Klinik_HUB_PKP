package com.hub.app.user.service;

import com.hub.app.user.model.User;
import com.hub.app.user.model.UserActivityStatus;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User activateUser(Long id) {
        var user = userRepo.findById(id).orElseThrow();
        user.setActivityStatus(UserActivityStatus.ACTIVE);
        return userRepo.save(user);
    }

    public User deactivateUser(Long id) {
        var user = userRepo.findById(id).orElseThrow();
        user.setActivityStatus(UserActivityStatus.INACTIVE);
        return userRepo.save(user);
    }

    public User selfDeactivateUser(Long id) {
        var user = userRepo.findById(id).orElseThrow();
        user.setActivityStatus(UserActivityStatus.INACTIVE);
        return userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepo.findByEmail(username).orElseThrow();

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));


        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getActivityStatus() != UserActivityStatus.ACTIVE)
                .build();
    }
}
