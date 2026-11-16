package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.security.JwtService;
import com.fhict.campaignmanager.service.AuthService;
import com.fhict.campaignmanager.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private IUserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, passwordEncoder, jwtService);
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        User user = User.builder()
                .id(1)
                .username("robin")
                .email("robin@example.com")
                .password("$2a$10$storedEncodedPassword")
                .build();
        LoginRequest request = LoginRequest.builder()
                .identifier("robin")
                .password("plainPassword")
                .build();

        when(userService.getUserByUsername("robin")).thenReturn(user);
        when(passwordEncoder.matches("plainPassword", user.getPassword())).thenReturn(true);
        when(jwtService.createToken("robin")).thenReturn("test-jwt");

        assertEquals("test-jwt", authService.login(request));
        verify(jwtService).createToken("robin");
    }

    @Test
    void login_withInvalidPassword_returnsNull() {
        User user = User.builder().username("robin").password("encoded-password").build();
        LoginRequest request = LoginRequest.builder()
                .identifier("robin")
                .password("wrongPassword")
                .build();

        when(userService.getUserByUsername("robin")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertNull(authService.login(request));
    }

    @Test
    void login_withUnknownUsername_returnsNull() {
        LoginRequest request = LoginRequest.builder()
                .identifier("unknown")
                .password("password")
                .build();
        when(userService.getUserByUsername("unknown")).thenReturn(null);

        assertNull(authService.login(request));
    }

    @Test
    void login_withEmail_looksUpUserByEmail() {
        User user = User.builder()
                .username("robin")
                .email("robin@example.com")
                .password("encoded")
                .build();
        LoginRequest request = LoginRequest.builder()
                .identifier("robin@example.com")
                .password("plainPassword")
                .build();

        when(userService.getUserByEmail("robin@example.com")).thenReturn(user);
        when(passwordEncoder.matches("plainPassword", "encoded")).thenReturn(true);
        when(jwtService.createToken("robin")).thenReturn("email-jwt");

        assertEquals("email-jwt", authService.login(request));
        verify(userService).getUserByEmail("robin@example.com");
    }

    @Test
    void isAuthenticationValid_returnsTrueForExistingAuthenticatedUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "robin", "credentials", java.util.List.of(() -> "ROLE_USER"));
        when(userService.getUserByUsername("robin"))
                .thenReturn(User.builder().username("robin").build());

        assertTrue(authService.isAuthenticationValid(authentication));
    }

    @Test
    void isAuthenticationValid_returnsFalseForUnknownUserOrInvalidAuthentication() {
        Authentication unknownUser = new UsernamePasswordAuthenticationToken(
                "unknown", "credentials", java.util.List.of(() -> "ROLE_USER"));
        when(userService.getUserByUsername("unknown")).thenReturn(null);

        assertFalse(authService.isAuthenticationValid(unknownUser));
        assertFalse(authService.isAuthenticationValid(null));
        assertFalse(authService.isAuthenticationValid(new AnonymousAuthenticationToken(
                "key", "anonymous", java.util.List.of(() -> "ROLE_ANONYMOUS"))));
    }
}
