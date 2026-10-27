package com.fhict.campaignmanager.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "campaigns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Builder.Default
    private Set<CampaignParticipantEntity> participantEntities = new HashSet<>();
}
