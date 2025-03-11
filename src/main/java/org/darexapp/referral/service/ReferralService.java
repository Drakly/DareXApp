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
                .clickCount(0)
                .build();

        ResponseEntity<ReferralRequest> response = referralClient.createReferral(referral);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully created referral with id {}", response.getBody().getId());
        }else {
            log.error("Failed to create referral with id {}", response.getBody().getId());
        }
    }

    public ReferralRequest getReferral(UUID userId) {
        ResponseEntity<ReferralRequest> httpResponse = referralClient.getReferralByUser(userId);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get referral with id " + userId);
        }
        return httpResponse.getBody();
    }

    public void incrementClickCount(String referralCode) {
        ResponseEntity<Void> httpResponse = referralClient.trackReferral(referralCode);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to track referral with id " + referralCode);
        }

    }
}
