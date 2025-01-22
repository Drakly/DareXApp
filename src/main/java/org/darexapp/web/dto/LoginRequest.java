package org.darexapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    @Size(min = 6,  message = "Username must be at least 6 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "\\d{6}", message = "Password must be exactly 6 digits")
    private String password;
}
