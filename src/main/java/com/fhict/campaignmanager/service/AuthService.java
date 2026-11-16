package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService{

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(IUserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String login(LoginRequest loginRequest) {
        User user = loginRequest.getIdentifier().contains("@")
                ? userService.getUserByEmail(loginRequest.getIdentifier())
                : userService.getUserByUsername(loginRequest.getIdentifier());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtService.createToken(user.getUsername());
        }
        return null;
    }

    @Override
    public String createToken(User user) {
        return jwtService.createToken(user.getUsername());
    }

    @Override
    public boolean isAuthenticationValid(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            return false;
        }

        return userService.getUserByUsername(authentication.getName()) != null;
    }
}
