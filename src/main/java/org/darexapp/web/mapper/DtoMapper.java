package org.darexapp.web.mapper;

import lombok.experimental.UtilityClass;
import org.darexapp.user.model.User;
import org.darexapp.web.dto.EditUserRequest;

@UtilityClass
public class DtoMapper {

    public EditUserRequest editUserMapper(User user) {

        return EditUserRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
