package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoginResponse
{
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;
}
