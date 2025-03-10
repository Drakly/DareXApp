package org.darexapp.referral.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.referral.client.ReferralClient;
import org.darexapp.referral.client.dto.ReferralRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ReferralService {

    private final ReferralClient referralClient;

    @Autowired
    public ReferralService(ReferralClient referralClient) {
        this.referralClient = referralClient;
    }

    public void createReferral(UUID userId, String referralCode, LocalDateTime createdAt, int clickCount) {
        ReferralRequest referral = ReferralRequest.builder()
                .userId(userId)
                .referralCode(referralCode)
                .createdAt(createdAt)
                .clickCount(clickCount = 0)
                .build();

        ResponseEntity<ReferralRequest> response = referralClient.createReferral(referral);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully created referral with id {}", response.getBody().getId());
        }


    }
}
