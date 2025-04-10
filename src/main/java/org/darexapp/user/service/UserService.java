package org.darexapp.user.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.model.CardType;
import org.darexapp.card.service.CardService;
import org.darexapp.exception.DomainException;
import org.darexapp.exception.EmailAddressAlreadyExist;
import org.darexapp.exception.UsernameExistException;
import org.darexapp.referral.service.ReferralService;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.repository.UserRepository;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.EditUserRequest;
import org.darexapp.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final WalletService walletService;

    private final SubscriptionService subscriptionService;

    private final CardService cardService;

    private final ReferralService referralService;



    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, SubscriptionService subscriptionService, CardService cardService, ReferralService referralService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.cardService = cardService;
        this.referralService = referralService;
    }


    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAddressAlreadyExist("Email [%s] is already registered.".formatted(registerRequest.getEmail()));
        }
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UsernameExistException("Username [%s] is already registered.".formatted(registerRequest.getUsername()));
        }

        User user = initializeUser(registerRequest);
        user = userRepository.save(user);

        Wallet defaultWallet = walletService.createWalletForUser(user);
        user.setWallets(List.of(defaultWallet));

        cardService.createCard(user, defaultWallet.getId(), CardType.VIRTUAL, user.getUsername());

        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);
        user.setSubscriptions(List.of(defaultSubscription));

        if (registerRequest.getReferralCode() != null && !registerRequest.getReferralCode().isBlank()) {
            referralService.incrementClickCount(registerRequest.getReferralCode());
        }

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
                .registeredAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public void updateUser(UUID id, EditUserRequest userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DomainException("User not found"));

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setProfilePicture(userDto.getProfilePicture());

        user.setModifiedAt(LocalDateTime.now());
        userRepository.save(user);

    }



    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void SwitchUserStatus(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new DomainException("User not found with id: " + userId);
        }
        User user = optionalUser.get();

        if (user.isActive()) {
            user.setActive(false);
        }else {
            user.setActive(true);
        }
        userRepository.save(user);
    }

    public void updateUserRole(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new DomainException("User not found with id: " + userId);
        }

        User user = optionalUser.get();

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        }else {
            user.setRole(UserRole.USER);
        }


        userRepository.save(user);
    }

    public int getActiveUserCount() {
        return userRepository.countUserByActiveTrue();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserDetails(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

}
