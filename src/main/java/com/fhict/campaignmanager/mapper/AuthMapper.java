package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthMapper {
    public LoginResponse toLoginResponse(String accessToken, long expiresIn, String tokenType) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .build();
    }

    public LoginResponse toLoginResponse(
            String accessToken,
            long expiresIn,
            String tokenType,
            UserResponse user
    ) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}
