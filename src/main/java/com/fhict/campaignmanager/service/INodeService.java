package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.StorageNode;
import com.fhict.campaignmanager.dto.CreateStorageNodeRequest;
import com.fhict.campaignmanager.dto.UpdateStorageNodeRequest;

import java.util.List;

public interface INodeService
{
    List<StorageNode> getStorageNodesForCampaign(int campaignId);

    StorageNode getStorageNode(int id);

    StorageNode createStorageNode(CreateStorageNodeRequest request);

    StorageNode changeSortOrder(UpdateStorageNodeRequest request);

    StorageNode updateStorageNode(UpdateStorageNodeRequest request);

    boolean deleteStorageNode(int id);
}
