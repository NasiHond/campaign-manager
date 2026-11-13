package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.persistence.entity.StorageNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageNodeRepository extends JpaRepository<StorageNodeEntity, Integer> {
    List<StorageNodeEntity> findAllByCampaignId(int i);
    List<StorageNodeEntity> findByParentNode_Id(int i);
}
