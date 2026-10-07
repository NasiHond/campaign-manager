package com.fhict.campaignmanager.dto;

import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import lombok.*;

import java.util.Map;
import java.util.List;

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
    private Integer ownerId;
    private List<InviteResponse> invites;
}