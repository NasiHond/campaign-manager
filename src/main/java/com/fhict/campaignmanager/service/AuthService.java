package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public AuthService(IUserService userService, PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = loginRequest.getIdentifier().contains("@")
                ? userService.getUserByEmail(loginRequest.getIdentifier())
                : userService.getUserByUsername(loginRequest.getIdentifier());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtService.createToken(user.getUsername());
            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationSeconds())
                    .user(userMapper.toUserResponse(user))
                    .build();
        }
        return null;
    }

    @Override
    public LoginResponse register(CreateUserRequest createUserRequest) {
        UserResponse createdUser = userService.createUser(createUserRequest);
        String token = jwtService.createToken(createdUser.getUsername());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(createdUser)
                .build();
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
