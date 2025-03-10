package org.darexapp.referral.client;

import org.darexapp.referral.client.dto.ReferralRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "referral-svc", url = "http://localhost:8081/api/v1/referral")
public interface ReferralClient {

    @GetMapping("/{userId}")
    ResponseEntity<Void> getReferralByUser(@PathVariable UUID userId);

    @PostMapping
    ResponseEntity<ReferralRequest> createReferral(@RequestBody ReferralRequest referral);

    @PutMapping("/track/{referralCode}")
    ResponseEntity<Void> trackReferral(@PathVariable String referralCode);

}
