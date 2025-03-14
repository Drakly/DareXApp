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

    public ReferralRequest toReferralRequest(Referral referral) {
        return ReferralRequest.builder()
                .referralCode(referral.getReferralCode())
                .userId(referral.getUserId())
                .clickCount(referral.getClickCount())
                .createdAt(referral.getCreatedAt())
                .build();
    }


    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .country(user.getCountry())
                .active(user.isActive())
                .createdOn(user.getRegisteredAt())
                .updatedOn(user.getModifiedAt())
                .build();
    }


    public WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .type(wallet.getType())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }


    public CardResponse toCardResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardType(card.getCardType())
                .cardNumber(maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .expiryDate(card.getExpiryDate())
                .createdOn(card.getCreatedOn())
                .walletId(card.getWallet().getId())
                .build();
    }


    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .sender(transaction.getSender())
                .receiver(transaction.getReceiver())
                .amount(transaction.getAmount())
                .balanceLeft(transaction.getRemainingBalance())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


    public SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .type(subscription.getType())
                .period(subscription.getPeriod())
                .status(subscription.getStatus())
                .price(subscription.getPrice())
                .active(subscription.isActive())
                .createdAt(subscription.getCreatedAt())
                .completedAt(subscription.getCompletedAt())
                .build();
    }


    public List<WalletResponse> toWalletResponseList(List<Wallet> wallets) {
        return wallets.stream()
                .map(DtoMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    public List<CardResponse> toCardResponseList(List<Card> cards) {
        return cards.stream()
                .map(DtoMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions) {
        return transactions.stream()
                .map(DtoMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public List<SubscriptionResponse> toSubscriptionResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(DtoMapper::toSubscriptionResponse)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
