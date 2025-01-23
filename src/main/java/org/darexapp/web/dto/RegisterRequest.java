package org.darexapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.darexapp.user.model.Country;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 6)
    private String username;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "\\d{8}", message = "Password must be at least 8 digits")
    private String password;

    private Country country;
}
