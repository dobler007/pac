package com.example.mas_implementation.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegisterForm {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 20, message = "Username must be 2–20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers and underscores")
    private String login;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
             message = "Password must contain at least one uppercase letter and one digit")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 60, message = "Name must be 2–60 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @Size(min = 3, max = 30, message = "Phone number must be 3–30 characters")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthdate;
}
