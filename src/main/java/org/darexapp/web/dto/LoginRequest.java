package org.darexapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {
    @Size(min = 6,message = "Username must be at least 6 symbols long")
    private String username;

    @Size(min = 9,message = "Password must be at least 9 symbols long")
    private String password;
} 