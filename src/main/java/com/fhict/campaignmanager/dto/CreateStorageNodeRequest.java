package com.fhict.campaignmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CreateStorageNodeRequest {
    private int campaignId;
    private Integer parentNodeId;
    private String name;
    private Integer sortOrder;
    private boolean isFolder;
}
