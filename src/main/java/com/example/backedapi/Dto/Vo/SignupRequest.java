package com.example.backedapi.Dto.Vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter@NoArgsConstructor
public class SignupRequest {
    private String email;
    private String password;

    // Getters and Setters
}