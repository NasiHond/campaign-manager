package com.fhict.campaignmanager.domain;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class StorageNode {
    private int id;
    private Campaign campaign;
    private StorageNode parentNode;
    private String name;
    private String sortOrder;
    private boolean isFolder;
}
