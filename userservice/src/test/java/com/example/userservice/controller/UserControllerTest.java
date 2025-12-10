package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.VerifyOtpRequest;
import com.example.userservice.service.OtpService;
import com.example.userservice.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void greeting_shouldReturnMessage() throws Exception {
        mockMvc.perform(get("/user/"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello thank you for review"));
    }

    @Test
    void sendOtp_shouldReturnMessage() throws Exception {
        UserRequest req = new UserRequest();
        req.setEmail("test@gmail.com");
        req.setUsername("veer");
        req.setPassword("123");

        when(userService.sendOtp(any(UserRequest.class))).thenReturn("OTP Sent");

        mockMvc.perform(post("/user/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP Sent"));
    }

    @Test
    void verifyOtp_shouldReturnStatus() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest();
        req.setEmail("test@gmail.com");
        req.setOtp("111111");

        when(userService.verifyOtpAndRegister(any(VerifyOtpRequest.class)))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/user/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void getUser_shouldReturnUserResponse() throws Exception {
        UserResponse res = new UserResponse();
        res.setEmail("test@gmail.com");
        res.setUsername("veer");

        when(userService.getByUsername("veer")).thenReturn(res);

        mockMvc.perform(get("/user/veer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.username").value("veer"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequest req = new UserRequest();
        req.setUsername("updated");

        UserResponse res = new UserResponse();
        res.setUsername("updated");
        res.setEmail("test@gmail.com");

        when(userService.updateUser(any(UUID.class), any(UserRequest.class)))
                .thenReturn(res);

        mockMvc.perform(patch("/user/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void deleteUser_shouldReturnMessage() throws Exception {
        UUID id = UUID.randomUUID();

        when(userService.deleteUser(id)).thenReturn("User is deleted");

        mockMvc.perform(delete("/user/delete/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("User is deleted"));
    }

    @Test
    void checkUsername_shouldReturnBoolean() throws Exception {
        when(userService.isUsernameAvailable("veer")).thenReturn(true);

        mockMvc.perform(get("/user/api/users/check-username")
                        .param("username", "veer"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
