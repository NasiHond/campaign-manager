package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateCampaignRequest
{
    private String name;
    private String description;
}
