package org.balczukiewicz.notekeeperservice.service.impl;

import org.balczukiewicz.notekeeperservice.entity.Role;
import org.balczukiewicz.notekeeperservice.entity.User;
import org.balczukiewicz.notekeeperservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Given
        String username = "testuser";
        User mockUser = User.builder()
                .id(1L)
                .username(username)
                .password("encoded-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // When
        UserDetails result = userService.loadUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_NonExistingUser_ThrowsException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void existsByUsername_ExistingUser_ReturnsTrue() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void existsByUsername_NonExistingUser_ReturnsFalse() {
        // Given
        String username = "nonexistent";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // When
        boolean result = userService.existsByUsername(username);

        // Then
        assertFalse(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void createDefaultUsers_NoExistingUsers_CreatesAdminAndUser() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        // When
        userService.createDefaultUsers();

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());

        var savedUsers = userCaptor.getAllValues();

        User admin = savedUsers.stream()
                .filter(u -> u.getUsername().equals("admin"))
                .findFirst()
                .orElseThrow();
        assertEquals("admin", admin.getUsername());
        assertEquals("encoded-password", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());

        User user = savedUsers.stream()
                .filter(u -> u.getUsername().equals("user"))
                .findFirst()
                .orElseThrow();
        assertEquals("user", user.getUsername());
        assertEquals("encoded-password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void createDefaultUsers_ExistingUsers_DoesNotCreateDuplicates() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("user")).thenReturn(true);

        // When
        userService.createDefaultUsers();

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createDefaultUsers_PartiallyExistingUsers_CreatesOnlyMissing() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(true);  // Admin exists
        when(userRepository.existsByUsername("user")).thenReturn(false);   // User doesn't exist
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        // When
        userService.createDefaultUsers();

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("user", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());

        // Verify both usernames were checked
        verify(userRepository).existsByUsername("admin");
        verify(userRepository).existsByUsername("user");
    }
}