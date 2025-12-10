package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.entity.User;
import com.example.userservice.external.service.NotificationService;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock OtpService otpService;
    @Mock NotificationCaller notificationCaller;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RedisTemplate<String,String> redisTemplate;
    @Mock ValueOperations<String,String> valueOperations;
    @Mock ObjectMapper objectMapper;

    @InjectMocks
    UserService userService;

    // helper to avoid null
    private void mockRedisOps() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ------------------------------------------------------
    // TEST 1: sendOtp()
    // ------------------------------------------------------
    @Test
    void sendOtp_shouldStoreTempUserAndSendOtp() throws Exception {
        UserRequest req = new UserRequest();
        req.setEmail("test@gmail.com");
        req.setUsername("rahul");
        req.setPassword("12345");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsername("rahul")).thenReturn(false);

        when(passwordEncoder.encode("12345")).thenReturn("encoded");
        when(objectMapper.writeValueAsString(any(UserRequest.class))).thenReturn("{json}");

        mockRedisOps();

        when(otpService.generateOtp("test@gmail.com")).thenReturn("999999");
        when(notificationCaller.sendOtp(any())).thenReturn("OTP sent successfully.");

        String response = userService.sendOtp(req);

        assertEquals("OTP sent successfully.", response);
        verify(valueOperations).set(eq("tmp:user:test@gmail.com"), eq("{json}"), any());
        verify(otpService).generateOtp("test@gmail.com");
        verify(notificationCaller).sendOtp(any(OtpSendRequest.class));
    }

    // ------------------------------------------------------
    // TEST 2: verifyOtpAndRegister() - success flow
    // ------------------------------------------------------
    @Test
    void verifyOtpAndRegister_shouldRegisterUserOnValidOtp() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("test@gmail.com");
        req.setOtp("111111");

        when(otpService.verifyOtp("test@gmail.com", "111111")).thenReturn(true);

        mockRedisOps();
        when(valueOperations.get("tmp:user:test@gmail.com"))
                .thenReturn("{\"email\":\"test@gmail.com\",\"username\":\"rahul\",\"password\":\"encoded\"}");

        UserRequest tempReq = new UserRequest();
        tempReq.setEmail("test@gmail.com");
        tempReq.setUsername("rahul");
        tempReq.setPassword("encoded");

        when(objectMapper.readValue(anyString(), eq(UserRequest.class))).thenReturn(tempReq);
        when(userRepository.existsByUsername("rahul")).thenReturn(false);
        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);

        User savedUser = new User();
        savedUser.setUserid(UUID.randomUUID());
        savedUser.setEmail("test@gmail.com");
        savedUser.setUsername("rahul");
        savedUser.setCreated_at(LocalDateTime.now());

        when(userMapper.toEntity(tempReq)).thenReturn(savedUser);

        String result = userService.verifyOtpAndRegister(req);

        assertEquals("User registered successfully", result);
        verify(userRepository).save(any(User.class));
        verify(redisTemplate).delete("tmp:user:test@gmail.com");
    }

    // ------------------------------------------------------
    // TEST 3: verifyOtpAndRegister() - OTP invalid
    // ------------------------------------------------------
    @Test
    void verifyOtpAndRegister_shouldFailOnInvalidOtp() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("x@gmail.com");
        req.setOtp("222222");

        when(otpService.verifyOtp("x@gmail.com", "222222")).thenReturn(false);

        String result = userService.verifyOtpAndRegister(req);

        assertEquals("Invalid or expired OTP", result);
    }

    // ------------------------------------------------------
    // TEST 4: getByUsername()
    // ------------------------------------------------------
    @Test
    void getByUsername_shouldReturnUserResponse() {
        User u = new User();
        u.setUsername("veer");

        UserResponse response = new UserResponse();
        response.setUsername("veer");

        when(userRepository.findByUsername("veer")).thenReturn(Optional.of(u));
        when(userMapper.toResponse(u)).thenReturn(response);

        UserResponse result = userService.getByUsername("veer");

        assertEquals("veer", result.getUsername());
    }

    // ------------------------------------------------------
    // TEST 5: updateUser()
    // ------------------------------------------------------
    @Test
    void updateUser_shouldUpdateFields() {
        UUID id = UUID.randomUUID();

        User existing = new User();
        existing.setUserid(id);
        existing.setUsername("old");
        existing.setEmail("old@gmail.com");

        UserRequest req = new UserRequest();
        req.setUsername("new");
        req.setEmail("new@gmail.com");
        req.setPassword("pass");

        User saved = new User();
        saved.setUserid(id);
        saved.setUsername("new");
        saved.setEmail("new@gmail.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse mapped = new UserResponse();
        mapped.setUsername("new");
        mapped.setEmail("new@gmail.com");

        when(userMapper.toResponse(saved)).thenReturn(mapped);

        UserResponse result = userService.updateUser(id, req);

        assertEquals("new", result.getUsername());
        assertEquals("new@gmail.com", result.getEmail());
    }

    // ------------------------------------------------------
    // TEST 6: deleteUser()
    // ------------------------------------------------------
    @Test
    void deleteUser_shouldDeleteWhenExists() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        String msg = userService.deleteUser(id);

        assertEquals("User is deleted", msg);
        verify(userRepository).deleteById(id);
    }

    // ------------------------------------------------------
    // TEST 7: isUsernameAvailable()
    // ------------------------------------------------------
    @Test
    void isUsernameAvailable_whenUserNotPresent() {
        when(userRepository.existsByUsername("veer")).thenReturn(false);
        assertTrue(userService.isUsernameAvailable("veer"));
    }
}
