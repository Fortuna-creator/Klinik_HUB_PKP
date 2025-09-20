package com.hub.app.auth.service;

import com.hub.app.auth.dto.AuthResponse;
import com.hub.app.auth.dto.LoginRequest;
import com.hub.app.auth.dto.RegisterRequest;
import com.hub.app.auth.model.User;
import com.hub.app.auth.repository.UserRepository;
import com.hub.app.auth.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole("ROLE_USER");
        userRepository.save(u);
        String token = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        Optional<User> userOpt = userRepository.findByUsername(req.getUsername());
        if (userOpt.isEmpty()) throw new RuntimeException("Invalid credentials");
        User u = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) throw new RuntimeException("Invalid credentials");
        String token = jwtUtil.generateToken(u.getUsername());
        return new AuthResponse(token);
    }
}
