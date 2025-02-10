package org.darexapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class EditUserRequest {

    @Size(max = 30, message = "First name can not have more than 30 symbols.")
    private String firstName;

    @Size(max = 30, message = "Last name can not have more than 30 symbols.")
    private String lastName;

    @Email(message = "Please provide a correct email.")
    private String email;

    @URL(message = "Please place a correct url format.")
    private String profilePicture;

}
