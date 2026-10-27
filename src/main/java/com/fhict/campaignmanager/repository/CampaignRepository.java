package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.persistence.entity.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<CampaignEntity, Integer> {
    List<CampaignEntity> findAllByParticipantEntities_User_Id(int userId);
}
