package com.example.userservice.dto;

import com.example.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRequest dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreated_at(user.getCreated_at());
        return dto;
    }
}
