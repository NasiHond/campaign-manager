package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.StorageNode;
import com.fhict.campaignmanager.dto.StorageNodeResponse;
import com.fhict.campaignmanager.dto.CreateStorageNodeRequest;
import com.fhict.campaignmanager.dto.UpdateStorageNodeRequest;
import com.fhict.campaignmanager.mapper.NodeMapper;
import com.fhict.campaignmanager.service.INodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {
    private final NodeMapper nodeMapper;
    private final INodeService nodeService;

    public NodeController(NodeMapper nodeMapper, INodeService nodeService) {
        this.nodeMapper = nodeMapper;
        this.nodeService = nodeService;
    }

    @GetMapping("/campaign/{id}")
    public List<StorageNodeResponse> getStorageNodesFromCampaign(@PathVariable int id)
    {
        List<StorageNode> storageNodes = nodeService.getStorageNodesForCampaign(id);
        return nodeMapper.toStorageNodeResponse(storageNodes);
    }

    @GetMapping("/{id}")
    public StorageNodeResponse getStorageNode(@PathVariable int id)
    {
        StorageNode storageNode = nodeService.getStorageNode(id);
        return nodeMapper.toStorageNodeResponse(storageNode);
    }

    @PostMapping()
    public StorageNodeResponse createStorageNode(@RequestBody CreateStorageNodeRequest request)
    {
        StorageNode storageNode = nodeService.createStorageNode(request);
        return nodeMapper.toStorageNodeResponse(storageNode);
    }

    @PutMapping()
    public StorageNodeResponse updateStorageNode(@RequestBody UpdateStorageNodeRequest request)
    {
        StorageNode updatedStorageNode = nodeService.updateStorageNode(request);
        return nodeMapper.toStorageNodeResponse(updatedStorageNode);
    }

    @PutMapping("/sort-order")
    public StorageNodeResponse updateStorageNodeSortOrder(@RequestBody UpdateStorageNodeRequest request)
    {
        StorageNode updatedStorageNode = nodeService.changeSortOrder(request);
        return nodeMapper.toStorageNodeResponse(updatedStorageNode);
    }

    @DeleteMapping("/{id}")
    public void deleteStorageNode(@PathVariable int id)
    {
        nodeService.deleteStorageNode(id);
    }
}
