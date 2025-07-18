package org.balczukiewicz.notekeeperservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balczukiewicz.notekeeperservice.entity.Role;
import org.balczukiewicz.notekeeperservice.entity.User;
import org.balczukiewicz.notekeeperservice.repository.UserRepository;
import org.balczukiewicz.notekeeperservice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void createDefaultUsers() {
        log.info("Creating default users if they don't exist...");

        createDefaultUserIfNotExists("admin", "password", Role.ADMIN);
        createDefaultUserIfNotExists("user", "password", Role.USER);

        log.info("Default users initialization completed");
    }

    private void createDefaultUserIfNotExists(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            log.debug("User {} already exists, skipping creation", username);
            return;
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("Created default {} user: {}", role.name().toLowerCase(), username);
    }

    @Override
    public void run(String... args) {
        createDefaultUsers();
    }
}