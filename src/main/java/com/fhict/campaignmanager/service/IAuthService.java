package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import org.springframework.security.core.Authentication;

public interface IAuthService
{
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse register(CreateUserRequest createUserRequest);

    /**
     * Verifies that the supplied request authentication represents an existing user.
     */
    boolean isAuthenticationValid(Authentication authentication);
}
