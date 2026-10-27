package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.*;
import com.fhict.campaignmanager.persistence.entity.*;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component
public class DomainEntityMapper {
    public User toDomain(UserEntity e) {
        return e == null ? null : User.builder().id(e.getId()).username(e.getUsername())
                .email(e.getEmail()).password(e.getPassword()).build();
    }
    public UserEntity toEntity(User d) {
        return d == null ? null : UserEntity.builder().id(d.getId()).username(d.getUsername())
                .email(d.getEmail()).password(d.getPassword()).build();
    }
    public Campaign toDomain(CampaignEntity e) {
        if (e == null) return null;
        var participants = new HashMap<User, Role>();
        e.getParticipantEntities().forEach(p -> participants.put(toDomain(p.getUser()), p.getRole()));
        return Campaign.builder().id(e.getId()).name(e.getName()).description(e.getDescription())
                .participants(participants).build();
    }
    public CampaignEntity toEntity(Campaign d) {
        if (d == null) return null;
        CampaignEntity e = CampaignEntity.builder().id(d.getId()).name(d.getName())
                .description(d.getDescription()).build();
        if (d.getParticipants() != null) d.getParticipants().forEach((user, role) ->
                e.getParticipantEntities().add(new CampaignParticipantEntity(0, e, toEntity(user), role)));
        return e;
    }
    public CampaignInvitation toDomain(CampaignInvitationEntity e) {
        return e == null ? null : CampaignInvitation.builder().id(e.getId())
                .campaign(toDomain(e.getCampaign())).invitedBy(toDomain(e.getInvitedBy()))
                .invitedUser(toDomain(e.getInvitedUser())).role(e.getRole()).status(e.getStatus())
                .expiresAt(e.getExpiresAt()).build();
    }
    public CampaignInvitationEntity toEntity(CampaignInvitation d) {
        return d == null ? null : CampaignInvitationEntity.builder().id(d.getId())
                .campaign(toEntity(d.getCampaign())).invitedBy(toEntity(d.getInvitedBy()))
                .invitedUser(toEntity(d.getInvitedUser())).role(d.getRole()).status(d.getStatus())
                .expiresAt(d.getExpiresAt()).build();
    }
}
