package com.fhict.campaignmanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
                    || !parentNode.isFolder()
                    || parentNode.getCampaign() == null
                    || parentNode.getCampaign().getId() != request.getCampaignId()) {
                return null;
            }
            //check if parent has existing children
            List<StorageNodeEntity> children = storageNodeRepository.findByParentNode_Id(request.getParentNodeId());
            if (!setSortOrder(entity, children, request.getSortOrder())) {
                return null;
            }
        }

        if (request.getParentNodeId() != null) {
            StorageNodeEntity parentNode = storageNodeRepository.findById(request.getParentNodeId()).orElse(null);
            if (parentNode == null
                    || !parentNode.isFolder()
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

        children.removeIf(child -> child.getId() == entity.getId());
        if (!setSortOrder(entity, children, request.getSortOrder())) {
            return null;
        }
        return entityMapper.toDomain(storageNodeRepository.save(entity));
    }

    private boolean setSortOrder(StorageNodeEntity entity, List<StorageNodeEntity> children,
                                 Integer targetIndex) {
        //if no siblings, set sort order to 0.1 and return true if targetIndex is null or 0
        if (children == null || children.isEmpty()) {
            entity.setSortOrder("0.1");
            return targetIndex == null || targetIndex == 0;
        }
        //if targetIndex is null, set it to the end of the list
        if (targetIndex == null) {
            targetIndex = children.size();
        }
        //check if targetIndex is within bounds
        if (targetIndex < 0 || targetIndex > children.size()) {
            return false;
        }
        if (targetIndex == 0) {
            double firstChildSortOrder = Double.parseDouble(children.get(0).getSortOrder());
            entity.setSortOrder(formatSortOrder(firstChildSortOrder / 2));
            return true;
        }
        if (targetIndex == children.size()) {
            double lastChildSortOrder = Double.parseDouble(children.get(targetIndex - 1).getSortOrder());
            if (lastChildSortOrder >= 0.9) {
                entity.setSortOrder(formatSortOrder((lastChildSortOrder + 1) / 2));
            } else {
                entity.setSortOrder(formatSortOrder(lastChildSortOrder + 0.1));
            }
            return true;
        }

        //calculate average of sort order of the two children at targetIndex - 1 and targetIndex
        String sortOrderBefore = children.get(targetIndex - 1).getSortOrder();
        String sortOrderAfter = children.get(targetIndex).getSortOrder();
        double average = (Double.parseDouble(sortOrderBefore)
                + Double.parseDouble(sortOrderAfter)) / 2;
        entity.setSortOrder(formatSortOrder(average));
        if (entity.getSortOrder().equals(sortOrderBefore)
                || entity.getSortOrder().equals(sortOrderAfter)) {
            entity.setSortOrder(formatSortOrder(average + 0.5));
        }
        return true;
    }

    private String formatSortOrder(double sortOrder) {
        return BigDecimal.valueOf(sortOrder)
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
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
