package org.darexapp.user;

import org.darexapp.card.model.CardType;
import org.darexapp.card.service.CardService;
import org.darexapp.exception.DomainException;
import org.darexapp.exception.EmailAddressAlreadyExist;
import org.darexapp.exception.UsernameExistException;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.user.model.Country;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.repository.UserRepository;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.EditUserRequest;
import org.darexapp.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {


    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private CardService cardService;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegister_whenEmailAlreadyExists_thenThrowEmailAddressAlreadyExist() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setCountry(Country.BULGARIA);


        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        EmailAddressAlreadyExist exception = assertThrows(EmailAddressAlreadyExist.class,
                () -> userService.register(registerRequest));
        assertTrue(exception.getMessage().contains(registerRequest.getEmail()));
    }

    @Test
    void testRegister_whenUsernameAlreadyExists_thenThrowUsernameExistException() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setCountry(Country.BULGARIA);


        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        UsernameExistException exception = assertThrows(UsernameExistException.class,
                () -> userService.register(registerRequest));
        assertTrue(exception.getMessage().contains(registerRequest.getUsername()));
    }

    @Test
    void testRegister_success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setCountry(Country.BULGARIA);


        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");


        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        when(walletService.createWalletForUser(any(User.class))).thenReturn(wallet);

        Subscription subscription = new Subscription();
        when(subscriptionService.createDefaultSubscription(any(User.class))).thenReturn(subscription);

        User result = userService.register(registerRequest);

        assertNotNull(result.getId());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(List.of(wallet), result.getWallets());
        assertEquals(List.of(subscription), result.getSubscriptions());

        verify(cardService).createCard(result, wallet.getId(), CardType.VIRTUAL, result.getUsername());
    }

    @Test
    void testUpdateUser_success() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setEmail("old@example.com");
        existingUser.setProfilePicture("oldPic.png");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        EditUserRequest editRequest = EditUserRequest.builder().build();
        editRequest.setFirstName("NewFirst");
        editRequest.setLastName("NewLast");
        editRequest.setEmail("new@example.com");
        editRequest.setProfilePicture("newPic.png");

        userService.updateUser(userId, editRequest);

        assertEquals("NewFirst", existingUser.getFirstName());
        assertEquals("NewLast", existingUser.getLastName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("newPic.png", existingUser.getProfilePicture());

        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> userService.updateUser(userId, EditUserRequest.builder().build()));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testFindById_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.findById(userId);
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void testFindById_notFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findById(userId));
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void testGetAllUsers() {
        List<User> userList = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void testSwitchUserStatus() {
        UUID userId = UUID.randomUUID();
        User userToSwitch = new User();
        userToSwitch.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToSwitch));


        userService.SwitchUserStatus(userId);
        assertFalse(userToSwitch.isActive());


        userService.SwitchUserStatus(userId);
        assertTrue(userToSwitch.isActive());

        verify(userRepository, times(2)).save(userToSwitch);
    }

    @Test
    void testSwitchUserStatus_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> userService.SwitchUserStatus(userId));
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void testUpdateUserRole() {
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User();
        userToUpdate.setRole(UserRole.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));

        userService.updateUserRole(userId);
        assertEquals(UserRole.ADMIN, userToUpdate.getRole());


        userToUpdate.setRole(UserRole.ADMIN);
        userService.updateUserRole(userId);
        assertEquals(UserRole.USER, userToUpdate.getRole());

        verify(userRepository, times(2)).save(userToUpdate);
    }

    @Test
    void testUpdateUserRole_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> userService.updateUserRole(userId));
        assertTrue(exception.getMessage().contains("User not found with id"));
    }

    @Test
    void testLoadUserByUsername_success() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
        user.setActive(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_notFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));
    }

}




