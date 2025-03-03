package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.user.model.Country;
import org.darexapp.user.model.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private UserRole role;
    private Country country;
    private boolean active;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
} 