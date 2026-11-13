package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class StorageNodeResponse {
    private int id;
    private CampaignResponse campaign;
    private StorageNodeResponse parentNode;
    private String name;
    private String sortOrder;
    private boolean isFolder;
}
