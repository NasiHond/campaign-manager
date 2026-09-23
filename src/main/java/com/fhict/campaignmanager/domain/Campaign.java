package com.fhict.campaignmanager.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Campaign {
    private int id;
    private String name;
    private String description;
    private User creator;
}
