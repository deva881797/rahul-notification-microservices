package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.VerifyOtpRequest;
import com.example.userservice.service.OtpService;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final OtpService otpService;

    @GetMapping("/")
    public ResponseEntity<String> greeting(){
        return ResponseEntity.ok("hello thank you for review");
    }
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.sendOtp(userRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(userService.verifyOtpAndRegister(request));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username){
        return ResponseEntity.ok(userService.getByUsername(username));
    }
    @PatchMapping("/update/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody UserRequest request) {

        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> removeUser(@PathVariable UUID id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/api/users/check-username")
    public ResponseEntity<Boolean> isUsernameAvailable(@RequestParam String username) {
        return ResponseEntity.ok(userService.isUsernameAvailable(username));
    }

}
