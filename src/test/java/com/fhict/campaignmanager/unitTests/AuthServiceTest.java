package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.security.JwtService;
import com.fhict.campaignmanager.service.AuthService;
import com.fhict.campaignmanager.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        authService = new AuthService(userService, passwordEncoder, jwtService, new UserMapper());
    }

    @Test
    void login_withValidCredentials_returnsLoginResponse() {
        User user = User.builder()
                .id(1)
                .username("robin")
                .email("robin@example.com")
                .password("$2a$10$storedEncodedPassword")
                .build();
        LoginRequest request = LoginRequest.builder()
                .username("robin")
                .password("plainPassword")
                .build();

        when(userService.getUserByUsername("robin")).thenReturn(user);
        when(passwordEncoder.matches("plainPassword", user.getPassword())).thenReturn(true);
        when(jwtService.createToken("robin")).thenReturn("test-jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("test-jwt", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertEquals("robin", result.getUser().getUsername());
        verify(jwtService).createToken("robin");
    }

    @Test
    void login_withInvalidPassword_returnsNull() {
        User user = User.builder().username("robin").password("encoded-password").build();
        LoginRequest request = LoginRequest.builder().username("robin").password("wrongPassword").build();

        when(userService.getUserByUsername("robin")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertNull(authService.login(request));
    }

    @Test
    void login_withUnknownUsername_returnsNull() {
        LoginRequest request = LoginRequest.builder().username("unknown").password("password").build();
        when(userService.getUserByUsername("unknown")).thenReturn(null);

        assertNull(authService.login(request));
    }
}
