package org.darexapp.web;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.service.UserService;
import org.darexapp.web.controller.UserController;
import org.darexapp.web.dto.EditUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;



    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToUsers() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).updateUserRole(any());
    }



    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_asAdmin_shouldReturnAdminView() throws Exception {
        List<User> users = Collections.emptyList();
        when(userService.getAllUsers()).thenReturn(users);

        MockHttpServletRequestBuilder request = get("/users");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("users", users));
    }


    @Test
    void getUserProfile_asAnyUser_shouldReturnProfileMenu() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("user1");

        when(userService.findById(userId)).thenReturn(user);

        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "User123", "Kirilov06@", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/users/{id}/profile", userId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile-menu"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attributeExists("userEditRequest"));
    }

    @Test
    void putUserProfile_withInvalidData_shouldReturnProfileMenu() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("user1");

        when(userService.findById(userId)).thenReturn(user);

        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "User123", "Kirilov06@", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .with(user(principal))
                .with(csrf())
                .formField("email", "123");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile-menu"))
                .andExpect(model().attributeExists("user", "userEditRequest"));

        verify(userService, never()).updateUser(eq(userId), any(EditUserRequest.class));
    }

    @Test
    void putUserProfile_withValidData_shouldRedirectToHome() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "User123", "Kirilov06@", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/profile", userId)
                .with(user(principal))
                .with(csrf())
                .formField("email", "user@abv.bg");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).updateUser(eq(userId), any(EditUserRequest.class));
    }

    @Test
    void putUserStatus_shouldRedirectToUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "User123", "Kirilov06@", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{id}/status", userId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService, times(1)).SwitchUserStatus(userId);
    }
}
