package com.fhict.campaignmanager.persistence.entity;

import com.fhict.campaignmanager.domain.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "campaign_invitations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampaignInvitationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private UserEntity invitedBy;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private UserEntity invitedUser;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private InvitationStatus status;
    @Column(nullable = false)
    private Instant expiresAt;
}
