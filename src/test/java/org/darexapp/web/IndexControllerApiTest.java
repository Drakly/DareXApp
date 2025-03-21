package org.darexapp.web;


import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.service.UserService;
import org.darexapp.web.controller.IndexController;
import org.darexapp.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.darexapp.UserTestBuilder.customUser;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexWithoutAuthentication_returnsIndexView() throws Exception {
        MockHttpServletRequestBuilder request = get("/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getLoginWithoutError_returnsLoginView() throws Exception {
        MockHttpServletRequestBuilder request = get("/login");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getLoginWithError_returnsLoginViewWithErrorMessage() throws Exception {
        MockHttpServletRequestBuilder request = get("/login").param("error", "");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void getRegister_returnsRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = get("/register");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRegisterValidData_redirectsToLogin() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Kris123")
                .formField("password", "Kirilov06@")
                .formField("email", "kris123@gmail.com")
                .formField("country", "UK")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void postRegisterInvalidData_returnsRegisterView() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void getHomeWithAuthentication_returnsHomeView() throws Exception {
        when(userService.findById(any())).thenReturn(customUser());

        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(userId, "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).findById(userId);
    }

    @Test
    void getContact_returnsContactView() throws Exception {
        MockHttpServletRequestBuilder request = get("/contact");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }
}
