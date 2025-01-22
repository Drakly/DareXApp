package org.darexapp.user.property;

import lombok.Getter;
import lombok.Setter;
import org.darexapp.user.model.UserRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
//@ConfigurationProperties(prefix = "domain.user.properties")
public class UserProperties {
    private UserRole defaultRole;

    private boolean activeByDefault;

}
