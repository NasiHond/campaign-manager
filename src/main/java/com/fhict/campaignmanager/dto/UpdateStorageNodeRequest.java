package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateStorageNodeRequest {
    private int id;
    private String name;
    private Boolean isFolder;
    private int sortOrder;
}
