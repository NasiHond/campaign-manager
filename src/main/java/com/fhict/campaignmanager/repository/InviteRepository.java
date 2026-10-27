package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.persistence.entity.CampaignInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InviteRepository extends JpaRepository<CampaignInvitationEntity, Integer> {
    List<CampaignInvitationEntity> findAllByInvitedUser_Id(int userId);

    List<CampaignInvitationEntity> findAllByCampaign_Id(int campaignId);
}
