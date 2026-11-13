package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.StorageNode;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateStorageNodeRequest;
import com.fhict.campaignmanager.dto.UpdateStorageNodeRequest;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.StorageNodeEntity;
import com.fhict.campaignmanager.repository.StorageNodeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StorageNodeService implements INodeService{
    private final StorageNodeRepository storageNodeRepository;
    private final IAuthService authService;
    private final IUserService userService;
    private final ICampaignService campaignService;
    private final DomainEntityMapper entityMapper;

    public StorageNodeService(StorageNodeRepository storageNodeRepository,
                              IAuthService authService,
                              IUserService userService,
                              ICampaignService campaignService,
                              DomainEntityMapper entityMapper) {
        this.storageNodeRepository = storageNodeRepository;
        this.authService = authService;
        this.userService = userService;
        this.campaignService = campaignService;
        this.entityMapper = entityMapper;
    }

    public List<StorageNode> getStorageNodesForCampaign(int i) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = campaignService.getCampaign(i);
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return null;
        }
        return storageNodeRepository.findAllByCampaignId(i).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    public StorageNode getStorageNode(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        StorageNodeEntity entity = storageNodeRepository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        Campaign campaign = entityMapper.toDomain(entity.getCampaign());
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return null;
        }
        return entityMapper.toDomain(entity);
    }

    public StorageNode createStorageNode(CreateStorageNodeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = campaignService.getCampaign(request.getCampaignId());
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return null;
        }

        StorageNodeEntity entity = new StorageNodeEntity();
        entity.setCampaign(entityMapper.toEntity(campaign));
        entity.setName(request.getName());
        entity.setFolder(request.isFolder());

        //check if parent node exists and is a folder and belongs to the same campaign
        if (request.getParentNodeId() != null) {
            StorageNodeEntity parentNode = storageNodeRepository.findById(request.getParentNodeId()).orElse(null);
            if (parentNode == null
                    || parentNode.isFolder() == false
                    || parentNode.getCampaign() == null
                    || parentNode.getCampaign().getId() != request.getCampaignId()) {
                return null;
            }
            //check if parent has existing children
            List<StorageNodeEntity> children = storageNodeRepository.findByParentNode_Id(request.getParentNodeId());
            if (children != null && children.isEmpty() == false) {
                //check if sort order is provided
                if(request.getSortOrder() != null)
                {
                    //check if the sort order is within the bounds of the children
                    if(request.getSortOrder() < 0 || request.getSortOrder() >= children.size()) {
                        return null;
                    }
                    //check if the sort order is the first in the list, if so set the sort order to half of the first child's sort order
                    if(request.getSortOrder() == 0) {
                        double firstChildSortOrder = Double.parseDouble(children.get(0).getSortOrder());
                        entity.setSortOrder(String.valueOf(firstChildSortOrder / 2));
                    }
                    //check if the sort order is the last in the list, if so set the sort order to 0.1 more than the last child's sort order
                    else if(request.getSortOrder() == children.size() - 1) {
                        double lastChildSortOrder = Double.parseDouble(children.get(children.size() - 1).getSortOrder());
                        //check if the last child's sort order is >= 0.9, if so set it to the average of the last child's sort order and 1
                        if(lastChildSortOrder >= 0.9) {
                            entity.setSortOrder(String.valueOf((lastChildSortOrder + 1) / 2));
                        }
                        entity.setSortOrder(String.valueOf(lastChildSortOrder + 0.1));
                    }
                    //get the average sort order of the children before and after the provided sort order
                    String sortOrderBefore = children.get(request.getSortOrder() - 1).getSortOrder();
                    String sortOrderAfter = children.get(request.getSortOrder() + 1).getSortOrder();
                    //set the sort order of the new node to the average of the two
                    double avg = (Double.parseDouble(sortOrderBefore) + Double.parseDouble(sortOrderAfter)) / 2;
                    entity.setSortOrder(String.valueOf(avg));
                    //if the average is equal to one of the two, set the sort order to the average of the two and 1 more decimal place
                    if(entity.getSortOrder().equals(sortOrderBefore) || entity.getSortOrder().equals(sortOrderAfter))
                    {
                        entity.setSortOrder(String.valueOf(avg + 0.5));
                    }
                }
                    else
                    {
                        //if no sort order is provided, set the sort order to the next highest decimal place based on the previous sibling's sort order
                        double previousSiblingSortOrder = Double.parseDouble(children.getLast().getSortOrder());
                        if(previousSiblingSortOrder >= 0.9) {
                            double newSortOrder = previousSiblingSortOrder + 1;
                            newSortOrder = newSortOrder / 2;
                            entity.setSortOrder(String.valueOf(newSortOrder));
                        }
                        else {
                            entity.setSortOrder(String.valueOf(previousSiblingSortOrder + 0.1));
                        }
                    }
            }
                else {
                    //if no children exist, set the sort order to 0.1
                    entity.setSortOrder("0.1");
                }

            }

        if (request.getParentNodeId() != null) {
            StorageNodeEntity parentNode = storageNodeRepository.findById(request.getParentNodeId()).orElse(null);
            if (parentNode == null
                    || parentNode.isFolder() == false
                    || parentNode.getCampaign() == null
                    || parentNode.getCampaign().getId() != request.getCampaignId()) {
                return null;
            }
            entity.setParentNode(parentNode);
        }
        return entityMapper.toDomain(storageNodeRepository.save(entity));
    }

    public StorageNode changeSortOrder(UpdateStorageNodeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        StorageNodeEntity entity = storageNodeRepository.findById(request.getId()).orElse(null);
        if (entity == null) {
            return null;
        }
        Campaign campaign = entityMapper.toDomain(entity.getCampaign());
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return null;
        }

        List<StorageNodeEntity> children = new ArrayList<>(storageNodeRepository.findByParentNode_Id(
                getStorageNode(request.getId()).getParentNode().getId()));
        children.sort(Comparator.comparingDouble(child -> Double.parseDouble(child.getSortOrder())));

        // The requested sort order is the zero-based target index in the final child list.
        if(request.getSortOrder() < 0 || request.getSortOrder() >= children.size()) {
            return null;
        }
        children.removeIf(child -> child.getId() == entity.getId());
        if(request.getSortOrder() == 0) {
            double firstChildSortOrder = Double.parseDouble(children.get(0).getSortOrder());
            entity.setSortOrder(String.valueOf(firstChildSortOrder / 2));
        }
        else if(request.getSortOrder() == children.size()) {
            double lastChildSortOrder = Double.parseDouble(children.get(children.size() - 1).getSortOrder());
            if(lastChildSortOrder >= 0.9) {
                lastChildSortOrder = (lastChildSortOrder + 1) / 2;
            }
            entity.setSortOrder(String.valueOf(lastChildSortOrder + 0.1));
        }
        else {
            String sortOrderBefore = children.get(request.getSortOrder() - 1).getSortOrder();
            String sortOrderAfter = children.get(request.getSortOrder()).getSortOrder();
            double avg = (Double.parseDouble(sortOrderBefore) + Double.parseDouble(sortOrderAfter)) / 2;
            entity.setSortOrder(String.valueOf(avg));
        }
        return entityMapper.toDomain(storageNodeRepository.save(entity));
    }

    public StorageNode updateStorageNode(UpdateStorageNodeRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        StorageNodeEntity entity = storageNodeRepository.findById(request.getId()).orElse(null);
        if (entity == null) {
            return null;
        }
        Campaign campaign = entityMapper.toDomain(entity.getCampaign());
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return null;
        }
        entity.setName(request.getName());
        entity.setFolder(request.getIsFolder());
        return entityMapper.toDomain(storageNodeRepository.save(entity));
    }

    public boolean deleteStorageNode(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return false;
        }
        User user = userService.getUserByUsername(authentication.getName());
        StorageNodeEntity entity = storageNodeRepository.findById(id).orElse(null);
        if (entity == null) {
            return false;
        }
        Campaign campaign = entityMapper.toDomain(entity.getCampaign());
        //check if user is participant of campaign
        if (campaign.getParticipants().keySet().stream().noneMatch(participant -> participant.getId() == user.getId())) {
            return false;
        }
        deleteChildren(entity);
        storageNodeRepository.delete(entity);
        return true;
    }

    private void deleteChildren(StorageNodeEntity parent) {
        List<StorageNodeEntity> children = storageNodeRepository.findByParentNode_Id(parent.getId());
        for (StorageNodeEntity child : children) {
            deleteChildren(child);
            storageNodeRepository.delete(child);
        }
    }
}
