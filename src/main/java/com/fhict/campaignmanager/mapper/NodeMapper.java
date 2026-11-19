package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.StorageNode;
import com.fhict.campaignmanager.dto.StorageNodeResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NodeMapper {
    private final CampaignMapper campaignMapper;

    public NodeMapper(CampaignMapper campaignMapper) {
        this.campaignMapper = campaignMapper;
    }

    public StorageNodeResponse toStorageNodeResponse(StorageNode storageNode) {
        if (storageNode == null) {
            return null;
        }

        return StorageNodeResponse.builder()
                .id(storageNode.getId())
                .name(storageNode.getName())
                .parentNode(toStorageNodeResponse(storageNode.getParentNode()))
                .campaign(campaignMapper.toCampaignResponse(storageNode.getCampaign()))
                .sortOrder(storageNode.getSortOrder())
                .isFolder(storageNode.isFolder())
                .build();
    }

    public List<StorageNodeResponse> toStorageNodeResponse(List<StorageNode> storageNodes) {
        if (storageNodes == null || storageNodes.isEmpty()) {
            return Collections.emptyList();
        }

        return storageNodes.stream()
                .map(this::toStorageNodeResponse)
                .toList();
    }
}
