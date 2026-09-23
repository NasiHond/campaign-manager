package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreateUserRequest
{
    private String username;
    private String email;
    private String password;
}
