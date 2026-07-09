package ru.ssau.virtualservers.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.virtualservers.dto.CreateUserRequestDTO;
import ru.ssau.virtualservers.dto.UserResponseDTO;
import ru.ssau.virtualservers.entity.AppUser;
import ru.ssau.virtualservers.entity.enums.UserRole;
import ru.ssau.virtualservers.repository.AppUserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//интеграционные тесты для UserController
@SpringBootTest
class UserControllerIT {
 
    @Autowired
    private UserController userController;
 
    @Autowired
    private AppUserRepository userRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    @BeforeEach
    void setUp() {
        AppUser user1 = new AppUser();
        user1.setUsername("test_user_1");
        user1.setPasswordHash(passwordEncoder.encode("password1"));
        user1.setUserRole(UserRole.USER);
        userRepository.save(user1);
 
        AppUser user2 = new AppUser();
        user2.setUsername("test_admin");
        user2.setPasswordHash(passwordEncoder.encode("adminpass"));
        user2.setUserRole(UserRole.ADMIN);
        userRepository.save(user2);
    }
 
    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }
 
    @Test
    void getAllUsers_returnsAllUsers() {
        ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();
 
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserResponseDTO> users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.username().equals("test_user_1")));
        assertTrue(users.stream().anyMatch(u -> u.username().equals("test_admin")));
    }
 
    @Test
    void register_newUser_savedInDB() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("new_user", "secret");
 
        ResponseEntity<UserResponseDTO> response = userController.register(request);
 
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO result = response.getBody();
        assertNotNull(result);
        assertEquals("new_user", result.username());
        assertEquals(UserRole.USER, result.role());
        assertTrue(userRepository.findByUsername("new_user").isPresent());
    }
 
    @Test
    void register_duplicateUsername_throwsException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("test_user_1", "another_password");
 
        assertThrows(
            IllegalArgumentException.class,
            () -> userController.register(request)
        );
    }
 
    @Test
    void deleteUser_userIsDeletedFromDB() {
        AppUser user = userRepository.findByUsername("test_user_1").orElseThrow();
        Long id = user.getId();
 
        ResponseEntity<Void> response = userController.deleteUser(id);
 
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(userRepository.existsById(id));
    }
}

