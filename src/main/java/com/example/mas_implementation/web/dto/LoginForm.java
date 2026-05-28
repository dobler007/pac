package com.example.mas_implementation.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {

    @NotBlank(message = "Username is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}
