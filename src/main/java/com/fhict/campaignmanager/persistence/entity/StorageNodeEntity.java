package com.fhict.campaignmanager.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_nodes",
        uniqueConstraints = @UniqueConstraint(name = "uk_storage_node_campaign",
                columnNames = {"campaign_id", "id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StorageNodeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_node_id")
    private StorageNodeEntity parentNode;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private String sortOrder = "1";
    @Column(name = "is_folder", nullable = false)
    private boolean isFolder;
}
