package com.fhict.campaignmanager.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private int id;
    private String username;
    private String email;
    private String password;
}
