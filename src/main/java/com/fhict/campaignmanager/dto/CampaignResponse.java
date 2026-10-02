package com.fhict.campaignmanager.dto;

import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CampaignResponse {
    private int id;
    private String name;
    private String description;
    private Map<User, Role> participants;
}