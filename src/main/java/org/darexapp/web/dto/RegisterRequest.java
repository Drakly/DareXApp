package org.darexapp.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.darexapp.referral.client.dto.Referral;
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
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character")
    private String password;

    @NotNull
    private Country country;

    private String referral;
}