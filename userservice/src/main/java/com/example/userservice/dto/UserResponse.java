package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String username;
    private String email;
    private LocalDateTime created_at;
    private LocalDateTime update_at;
}
