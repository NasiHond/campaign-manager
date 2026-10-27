package com.fhict.campaignmanager.domain;

import lombok.*;
import java.util.Map;
import java.util.HashMap;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Campaign {
    private int id;
    private String name;
    private String description;
    @Builder.Default
    private Map<User, Role> participants = new HashMap<>();
}
