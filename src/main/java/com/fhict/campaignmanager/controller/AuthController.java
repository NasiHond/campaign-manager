package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.service.IAuthService;
import com.fhict.campaignmanager.service.IUserService;
import com.fhict.campaignmanager.mapper.UserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final IAuthService authService;
    private final IUserService userService;
    private final UserMapper userMapper;

    public AuthController(IAuthService authService, IUserService userService, UserMapper userMapper) {
        this.authService = authService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/api/auth/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest)
    {
        String token = authService.login(loginRequest);
        if (token == null) {
            return null;
        }
        User user = loginRequest.getIdentifier().contains("@")
                ? userService.getUserByEmail(loginRequest.getIdentifier())
                : userService.getUserByUsername(loginRequest.getIdentifier());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @PostMapping("/api/auth/register")
    public LoginResponse register(@RequestBody CreateUserRequest createUserRequest)
    {
        User createdUser = userService.createUser(createUserRequest);
        if (createdUser == null) {
            return null;
        }
        String token = authService.createToken(createdUser);
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userMapper.toUserResponse(createdUser))
                .build();
    }
}
