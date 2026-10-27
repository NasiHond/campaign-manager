package com.fhict.campaignmanager.persistence.entity;

import com.fhict.campaignmanager.domain.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campaign_participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_participants_campaign_user",
                columnNames = {"campaign_id", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CampaignParticipantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
}
