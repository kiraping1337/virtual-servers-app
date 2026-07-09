package ru.ssau.virtualservers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.virtualservers.dto.UserResponseDTO;
import ru.ssau.virtualservers.entity.AppUser;
import ru.ssau.virtualservers.entity.enums.UserRole;
import ru.ssau.virtualservers.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

//модульные тесты для AppUserService.

class AppUserServiceTest {

    private AppUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AppUserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new AppUserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_successfulRegistration_returnsDTO() {
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.empty());

        AppUser saved = new AppUser();
        saved.setId(1L);
        saved.setUsername("ivan");
        saved.setPasswordHash("hashed");
        saved.setUserRole(UserRole.USER);
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);

        UserResponseDTO result = userService.registerUser("ivan", "password123");

        assertEquals(1L, result.id());
        assertEquals("ivan", result.username());
        assertEquals(UserRole.USER, result.role());
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void registerUser_userAlreadyExists_throwsException() {
        AppUser existing = new AppUser();
        existing.setUsername("ivan");
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser("ivan", "password123")
        );
        assertTrue(ex.getMessage().contains("уже существует"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_userFound_returnsUserDetails() {
        AppUser user = new AppUser();
        user.setUsername("ivan");
        user.setPasswordHash("hashed_password");
        user.setUserRole(UserRole.USER);
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("ivan");

        assertEquals("ivan", details.getUsername());
        assertTrue(details.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("unknown")
        );
    }

    @Test
    void getAllUsers_returnsListDTO() {
        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setUsername("ivan");
        user1.setUserRole(UserRole.USER);

        AppUser user2 = new AppUser();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setUserRole(UserRole.ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("ivan", result.get(0).username());
        assertEquals("admin", result.get(1).username());
    }

    @Test
    void deleteUser_userExists_deleted() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_userDoesNotExist_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> userService.deleteUser(99L)
        );
        verify(userRepository, never()).deleteById(any());
    }
}