package org.darexapp.web.mapper;

import lombok.experimental.UtilityClass;
import org.darexapp.card.model.Card;
import org.darexapp.referral.client.dto.Referral;
import org.darexapp.referral.client.dto.ReferralRequest;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.web.dto.*;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class DtoMapper {


    public EditUserRequest toEditUserRequest(User user) {
        return EditUserRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
