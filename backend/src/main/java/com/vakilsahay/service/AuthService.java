package com.vakilsahay.service;

import com.vakilsahay.entity.User;
import com.vakilsahay.exception.EmailAlreadyExistsException;
import com.vakilsahay.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — handles registration and login.
 * Also implements UserDetailsService for Spring Security.
 *
 * NOTE: AuthenticationManager is injected @Lazy to break the circular
 * dependency:  SecurityConfig → AuthService → AuthenticationManager → SecurityConfig
 *
 * © 2025 VakilSahay
 */
@Service
@Slf4j
public class AuthService implements UserDetailsService {

    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager) {
        this.userRepository        = userRepository;
        this.passwordEncoder       = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User register(String email, String fullName, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email.toLowerCase().trim())
                .fullName(fullName.trim())
                .password(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());
        return saved;
    }

    public User authenticate(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email.toLowerCase().trim(), password)
        );
        return (User) auth.getPrincipal();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }
}