package org.darexwallet.user.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.darexwallet.user.model.UserRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
//@ConfigurationProperties(prefix = "domain.user.properties")
public class UserProperties {


    @NotNull
    private UserRole defaultRole;

    private boolean activeByDefault;

}
