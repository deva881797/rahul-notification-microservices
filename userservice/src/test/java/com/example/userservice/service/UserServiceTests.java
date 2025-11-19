package com.example.userservice.service;

import com.example.userservice.dto.UserMapper;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;


    // ---------------- CREATE USER ----------------
    @Test
    void createUser_success() {

        UserRequest request = new UserRequest();
        request.setUsername("veer");
        request.setEmail("veer@test.com");
        request.setPassword("12345");

        User userEntity = new User();
        userEntity.setUsername("veer");
        userEntity.setEmail("veer@test.com");
        userEntity.setPassword("12345");

        User savedEntity = new User();
        savedEntity.setUserid(UUID.randomUUID());
        savedEntity.setUsername("veer");
        savedEntity.setEmail("veer@test.com");

        UserResponse response = new UserResponse();
        response.setUsername("veer");
        response.setEmail("veer@test.com");

        // email and username not taken
        Mockito.when(userRepository.existsByEmail("veer@test.com")).thenReturn(false);
        Mockito.when(userRepository.existsByUsername("veer")).thenReturn(false);

        // map DTO → Entity
        Mockito.when(userMapper.toEntity(request)).thenReturn(userEntity);

        // repository save
        Mockito.when(userRepository.save(userEntity)).thenReturn(savedEntity);

        // map Entity → Response DTO
        Mockito.when(userMapper.toResponse(savedEntity)).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertEquals("veer", result.getUsername());
        assertEquals("veer@test.com", result.getEmail());
    }


    // ---------------- GET USER ----------------
    @Test
    void getUserUsername_success() {

        String username = "veer";

        // Mock Entity
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setEmail("v@test.com");

        // Mock Response DTO
        UserResponse mockResponse = new UserResponse();
        mockResponse.setUsername(username);
        mockResponse.setEmail("v@test.com");

        // Mock behaviour
        Mockito.when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(userMapper.toResponse(mockUser))
                .thenReturn(mockResponse);

        // ---- CALL SERVICE ----
        UserResponse result = userService.getByUsername(username);

        // ---- ASSERT ----
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("v@test.com", result.getEmail());

        // ---- VERIFY REPO CALL ----
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }



    // ---------------- UPDATE USER ----------------
    @Test
    void updateUser_success() {

        UUID id = UUID.randomUUID();

        User existing = new User();
        existing.setUserid(id);
        existing.setUsername("old");
        existing.setEmail("old@test.com");
        existing.setPassword("123");

        UserRequest request = new UserRequest();
        request.setUsername("new");
        request.setEmail("new@test.com");
        request.setPassword("456");

        User saved = new User();
        saved.setUserid(id);
        saved.setUsername("new");
        saved.setEmail("new@test.com");

        UserResponse response = new UserResponse();
        response.setUsername("new");
        response.setEmail("new@test.com");

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(userRepository.save(existing)).thenReturn(saved);
        Mockito.when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userService.updateUser(id, request);

        assertEquals("new", result.getUsername());
        assertEquals("new@test.com", result.getEmail());
    }


    // ---------------- DELETE USER ----------------
    @Test
    void deleteUser_success() {

        UUID id = UUID.randomUUID();

        Mockito.when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        Mockito.verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound() {

        UUID id = UUID.randomUUID();

        Mockito.when(userRepository.existsById(id)).thenReturn(false);

        Assertions.assertThrows(RuntimeException.class, () -> userService.deleteUser(id));
    }
}
