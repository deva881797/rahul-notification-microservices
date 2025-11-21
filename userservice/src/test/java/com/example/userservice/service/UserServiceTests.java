package com.example.userservice.service;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.userservice.dto.*;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.external.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private OtpService otpService;
    @Mock private NotificationService notificationClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ObjectMapper objectMapper;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private UserService userService;
    @Mock private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);  // Mock opsForValue()
    }

    @Test
    void testSendOtp_EmailExists() {
        UserRequest req = new UserRequest();
        req.setEmail("test@example.com");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        var ex = assertThrows(IllegalArgumentException.class, () -> userService.sendOtp(req));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    void testSendOtp_UsernameExists() {
        UserRequest req = new UserRequest();
        req.setEmail("new@example.com");
        req.setUsername("user1");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(true);
        var ex = assertThrows(IllegalArgumentException.class, () -> userService.sendOtp(req));
        assertEquals("Username already taken", ex.getMessage());
    }

    @Test
    void testSendOtp_Success() throws Exception {
        UserRequest req = new UserRequest();
        req.setEmail("test@example.com");
        req.setUsername("user1");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(otpService.generateOtp(anyString())).thenReturn("123456");
        when(objectMapper.writeValueAsString(any(UserRequest.class))).thenReturn("{\"email\":\"test@example.com\"}");

        // Mock redisTemplate.opsForValue()
        ValueOperations<String, String> valueOpsMock = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOpsMock);

        String response = userService.sendOtp(req);

        verify(notificationClient).sendOtp(argThat(reqt ->
                reqt.getEmail().equals("test@example.com") &&
                        reqt.getOtp().equals("123456") &&
                        reqt.getPurpose().equals("Registration")
        ));

        // Verify Redis set called
        verify(valueOpsMock).set(anyString(), anyString(), any());

        assertEquals("OTP sent. Complete verification to register.", response);
    }


    @Test
    void testVerifyOtpAndRegister_InvalidOtp() {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("test@example.com");
        req.setOtp("wrongOtp");
        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(false);
        String result = userService.verifyOtpAndRegister(req);
        assertEquals("Invalid or expired OTP", result);
    }

    @Test
    void testVerifyOtpAndRegister_Success() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("test@example.com");
        req.setOtp("123456");
        UserRequest tempUser = new UserRequest();
        tempUser.setEmail(req.getEmail());
        tempUser.setUsername("user1");
        tempUser.setPassword("encodedPassword");
        // Mock OTP validation
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        // Mock Redis get value
        String json = "{\"email\":\"test@example.com\",\"username\":\"user1\",\"password\":\"encodedPassword\"}";
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(json);
        // Mock deserialization
        UserRequest deserializedUser = new UserRequest();
        deserializedUser.setEmail("test@example.com");
        deserializedUser.setUsername("user1");
        deserializedUser.setPassword("encodedPassword");
        when(objectMapper.readValue(json, UserRequest.class)).thenReturn(deserializedUser);
        // Mock user existence checks
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        // Mock save
        User user = new User();
        UUID id=UUID.randomUUID();
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        String result = userService.verifyOtpAndRegister(req);
        assertEquals("User registered successfully", result);
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        // fake user entity
        User entity = new User();
        entity.setUsername("veer");
        entity.setEmail("john@example.com");

        // fake response DTO
        UserResponse response = new UserResponse();
        response.setUsername("veer");
        response.setEmail("john@example.com");

        // mocking repository + mapper
        Mockito.when(userRepository.findByUsername("veer"))
                .thenReturn(Optional.of(entity));

        Mockito.when(userMapper.toResponse(entity))
                .thenReturn(response);

        // call the method
        UserResponse result = userService.getByUsername("veer");

        // assertions
        assertEquals("veer", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testUpdateUser() {
        UUID id = UUID.randomUUID();

        // Existing user in DB
        User existing = new User();
        existing.setUsername("oldUser");
        existing.setEmail("old@example.com");
        existing.setPassword("oldpass");

        // Incoming update request
        UserRequest request = new UserRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("newpass");

        // User after update
        User updated = new User();
        updated.setUsername("newUser");
        updated.setEmail("new@example.com");
        updated.setPassword("encodedPassword"); // encoded
        updated.setUpdated_at(LocalDateTime.now());

        // Response DTO
        UserResponse response = new UserResponse();
        response.setUsername("newUser");
        response.setEmail("new@example.com");

        // Mocks
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedPassword");
        when(userRepository.save(existing)).thenReturn(updated);
        when(userMapper.toResponse(updated)).thenReturn(response);

        // Call service
        UserResponse result = userService.updateUser(id, request);

        // Verify changes
        assertEquals("newUser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedPassword", existing.getPassword());

        verify(userRepository).findById(id);
        verify(userRepository).save(existing);
    }

    @Test
    void testDeleteUser() {
        UUID id = UUID.randomUUID();

        when(userRepository.existsById(id)).thenReturn(true);

        String result = userService.deleteUser(id);

        assertEquals("User is deleted", result);

        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

}
