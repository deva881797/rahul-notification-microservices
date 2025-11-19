package com.example.userservice.service;

import com.example.userservice.dto.UserMapper;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(UserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exist");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Username already taken");
        }
        User user=userMapper.toEntity(request);
        User saved=userRepository.save(user);

        return userMapper.toResponse(saved);
    }

    public UserResponse getByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toResponse(user);
    }

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
            existing.setPassword(request.getPassword());
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
}
