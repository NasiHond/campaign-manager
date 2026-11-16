package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.LoginRequest;
import org.springframework.security.core.Authentication;

public interface IAuthService
{
    String login(LoginRequest loginRequest);

    String createToken(User user);

    /**
     * Verifies that the supplied request authentication represents an existing user.
     */
    boolean isAuthenticationValid(Authentication authentication);
}
