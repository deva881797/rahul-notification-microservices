package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.entity.User;
import com.example.userservice.external.service.NotificationService;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String TEMP_USER_KEY_PREFIX = "tmp:user:"; // tmp:user:{email}
    private static final Duration TEMP_USER_TTL = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final NotificationService notificationClient;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationCaller notificationCaller;


    public String sendOtp(UserRequest request) {
        // Validate early, throw IllegalArgumentException for bad input
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Store unsaved user temporarily in Redis
        UserRequest safe = new UserRequest();
        safe.setEmail(request.getEmail());
        safe.setUsername(request.getUsername());
        if (request.getPassword() != null) {
            safe.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        String key = TEMP_USER_KEY_PREFIX + request.getEmail();
        try {
            String json = objectMapper.writeValueAsString(safe);
            redisTemplate.opsForValue().set(key, json, TEMP_USER_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Internal error storing temp user", e);
        }

        String otp = otpService.generateOtp(request.getEmail());
        OtpSendRequest otpReq = new OtpSendRequest(request.getEmail(), otp, "Registration");

        return notificationCaller.sendOtp(otpReq);
    }
    //

    /**
     * Step 2: verify OTP and register using temp user stored in Redis
     */
    @Transactional
    public String verifyOtpAndRegister(VerifyOtpRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            return "Invalid or expired OTP";
        }


        String key = TEMP_USER_KEY_PREFIX + request.getEmail();
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
        // temp data missing — could be expired or user tampered
            return "No pending registration found for this email. Please start again.";
        }


        UserRequest temp;
        try {
            temp = objectMapper.readValue(json, UserRequest.class);
        } catch (JsonProcessingException e) {
            return "Internal error reading registration data";
        }


        // Double-check uniqueness right before write (DB unique constraints are final guard)
        if (userRepository.existsByUsername(temp.getUsername())){
            return "Username taken";
        }
        if (userRepository.existsByEmail(temp.getEmail())){
            return "Email already registered";
        }


        User user = userMapper.toEntity(temp);
        user.setCreated_at(LocalDateTime.now());
        user.setUpdated_at(LocalDateTime.now());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // This handles race conditions where another transaction created the same username/email
            return "Registration failed: username or email already in use";
        }
        // temp data to avoid replay
        redisTemplate.delete(key);


        return "User registered successfully";
    }

    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getUsername() != null) {
            existing.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
        // Always encode passwords
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        existing.setUpdated_at(LocalDateTime.now());
        User saved = userRepository.save(existing);
        return userMapper.toResponse(saved);
    }

    public String deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);

        return "User is deleted";
    }

    public boolean isUsernameAvailable(String username){
        return !userRepository.existsByUsername(username);
    }
}
