package org.darexapp.user.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.service.CardService;
import org.darexapp.exception.DomainException;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.repository.UserRepository;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.LoginRequest;
import org.darexapp.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.darexapp.user.model.UserRole.USER;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final WalletService walletService;

    private final SubscriptionService subscriptionService;

    private final CardService cardService;



    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, SubscriptionService subscriptionService, CardService cardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.cardService = cardService;
    }

    @Transactional
    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new DomainException("Invalid username or password."));
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email [%s] is already registered.".formatted(registerRequest.getEmail()));
        }


        User user = initializeUser(registerRequest);
        user = userRepository.save(user);

        cardService.createDefaultVirtualCard(user);


        subscriptionService.createDefaultSubscription(user);

        log.info("User registered: username [{}], email [{}], id [{}]", user.getUsername(), user.getEmail(), user.getId());
        return user;
    }
    private User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .active(true)
                .country(registerRequest.getCountry())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }





    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] doesn't exist.".formatted(id)));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
