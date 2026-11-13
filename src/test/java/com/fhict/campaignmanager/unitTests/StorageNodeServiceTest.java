package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.StorageNode;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateStorageNodeRequest;
import com.fhict.campaignmanager.dto.UpdateStorageNodeRequest;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.CampaignEntity;
import com.fhict.campaignmanager.persistence.entity.StorageNodeEntity;
import com.fhict.campaignmanager.repository.StorageNodeRepository;
import com.fhict.campaignmanager.service.ICampaignService;
import com.fhict.campaignmanager.service.IAuthService;
import com.fhict.campaignmanager.service.IUserService;
import com.fhict.campaignmanager.service.StorageNodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageNodeServiceTest {

    @Mock private StorageNodeRepository storageNodeRepository;
    @Mock private IAuthService authService;
    @Mock private IUserService userService;
    @Mock private ICampaignService campaignService;
    @Mock private DomainEntityMapper entityMapper;
    @InjectMocks private StorageNodeService storageNodeService;

    @Test
    void getStorageNodesForCampaign_returnsNodesForAuthenticatedUser() {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder().id(10).build();
        StorageNode root = StorageNode.builder()
                .id(100)
                .campaign(campaign)
                .name("root")
                .isFolder(true)
                .build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(campaignService.getCampaign(10)).thenReturn(
                Campaign.builder()
                        .id(10)
                        .participants(java.util.Map.of(user, Role.OWNER))
                        .build());
        StorageNodeEntity rootEntity = new StorageNodeEntity();
        when(storageNodeRepository.findAllByCampaignId(10))
                .thenReturn(List.of(rootEntity));
        when(entityMapper.toDomain(rootEntity)).thenReturn(root);

        List<StorageNode> result =
                storageNodeService.getStorageNodesForCampaign(10);

        assertEquals(List.of(root), result);
        verify(storageNodeRepository).findAllByCampaignId(10);
    }

    @Test void getStorageNodesForCampaign_whileUserNotParticipant_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of()).build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(campaignService.getCampaign(10)).thenReturn(campaign);

        List<StorageNode> result = storageNodeService.getStorageNodesForCampaign(10);

        assertNull(result);
        verify(storageNodeRepository, never()).findAllByCampaignId(anyInt());
    }

    @Test void getStorageNodes_withoutAuthentication_returnsNull()
    {
        List<StorageNode> result = storageNodeService.getStorageNodesForCampaign(10);
        assertNull(result);
    }

    @Test void changeSortOrder_setsCorrectSortOrder()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();

        UpdateStorageNodeRequest request = new UpdateStorageNodeRequest();
        request.setId(101);
        request.setSortOrder(0);

        StorageNodeEntity parentNode = new StorageNodeEntity();
        parentNode.setId(20);
        CampaignEntity campaignEntity = new CampaignEntity();
        campaignEntity.setId(10);
        parentNode.setCampaign(campaignEntity);
        parentNode.setFolder(true);

        StorageNodeEntity child1 = new StorageNodeEntity();
        child1.setId(100);
        child1.setParentNode(parentNode);
        child1.setCampaign(campaignEntity);
        child1.setSortOrder("0.1");

        StorageNodeEntity child2 = new StorageNodeEntity();
        child2.setId(101);
        child2.setParentNode(parentNode);
        child2.setCampaign(campaignEntity);
        child2.setSortOrder("0.2");

        StorageNodeEntity child3 = new StorageNodeEntity();
        child3.setId(102);
        child3.setParentNode(parentNode);
        child3.setCampaign(campaignEntity);
        child3.setSortOrder("0.3");

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(storageNodeRepository.findById(101)).thenReturn(java.util.Optional.of(child2));
        when(storageNodeRepository.findByParentNode_Id(20)).thenReturn(List.of(child1, child2, child3));
        when(entityMapper.toDomain(campaignEntity)).thenReturn(campaign);
        when(entityMapper.toDomain(any(StorageNodeEntity.class))).thenReturn(
                StorageNode.builder().id(101).build());
        when(entityMapper.toDomain(child2)).thenReturn(
                StorageNode.builder()
                        .id(101)
                        .parentNode(StorageNode.builder().id(20).build())
                        .build());
        when(storageNodeRepository.save(any(StorageNodeEntity.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        storageNodeService.changeSortOrder(request);

        assertEquals("0.05", child2.getSortOrder());
        assertEquals("0.1", child1.getSortOrder());
        assertEquals("0.3", child3.getSortOrder());
    }

    @Test void createStorageNode_returnsNodeForAuthenticatedUser()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(null);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(
                User.builder().id(1).username("testuser").build());
        when(campaignService.getCampaign(10)).thenReturn(
                Campaign.builder()
                        .id(10)
                        .participants(java.util.Map.of(user, Role.OWNER))
                        .build());
        when(storageNodeRepository.save(any(StorageNodeEntity.class))).thenAnswer(invocation -> {
            StorageNodeEntity entity = invocation.getArgument(0);
            entity.setId(200);
            return entity;
        });
        when(entityMapper.toDomain(any(StorageNodeEntity.class))).thenReturn(
                StorageNode.builder()
                        .id(200)
                        .campaign(campaign)
                        .name("test")
                        .isFolder(false)
                        .build());

        StorageNode result = storageNodeService.createStorageNode(request);
        assertEquals(200, result.getId());

        verify(storageNodeRepository).save(any(StorageNodeEntity.class));
    }

    @Test void createdStorageNode_toParentNode_withExistingChild_setsSortOrderToNextAvailable()
    {
        setAuthenticatedUser("testuser");

        StorageNodeEntity parentNode = new StorageNodeEntity();
        parentNode.setId(20);
        CampaignEntity campaignEntity = new CampaignEntity();
        campaignEntity.setId(10);
        parentNode.setCampaign(campaignEntity);
        parentNode.setFolder(true);
        StorageNodeEntity childNode = new StorageNodeEntity();
        childNode.setParentNode(parentNode);
        childNode.setSortOrder("0.1");
        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(20);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(campaignService.getCampaign(10)).thenReturn(campaign);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(parentNode));
        when(storageNodeRepository.findByParentNode_Id(20)).thenReturn(List.of(childNode));
        when(storageNodeRepository.save(any(StorageNodeEntity.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
        when(entityMapper.toDomain(any(StorageNodeEntity.class))).thenAnswer(invocation -> {
            StorageNodeEntity entity = invocation.getArgument(0);
            return StorageNode.builder().sortOrder(entity.getSortOrder()).build();
        });

        StorageNode result = storageNodeService.createStorageNode(request);

        assertEquals("0.2", result.getSortOrder());
    }

    @Test void createStorageNode_whileUserNotParticipant_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(null);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of()).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(campaignService.getCampaign(10)).thenReturn(campaign);

        StorageNode result = storageNodeService.createStorageNode(request);
        assertNull(result);
        verify(storageNodeRepository, never()).save(any(StorageNodeEntity.class));
    }

    @Test void createStorageNode_withoutAuthentication_returnsNull()
    {
        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(null);
        request.setName("test");
        request.setFolder(false);

        StorageNode result = storageNodeService.createStorageNode(request);
        assertNull(result);
        verify(storageNodeRepository, never()).save(any(StorageNodeEntity.class));
    }

    @Test void createStorageNode_withParentFromAnotherCampaign_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(20);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(
                User.builder().id(1).username("testuser").build());
        when(campaignService.getCampaign(10)).thenReturn(campaign);

        CampaignEntity otherCampaign = new CampaignEntity();
        otherCampaign.setId(99);

        StorageNodeEntity parentNode = new StorageNodeEntity();
        parentNode.setId(20);
        parentNode.setCampaign(otherCampaign);
        parentNode.setFolder(true);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(parentNode));

        StorageNode result = storageNodeService.createStorageNode(request);
        assertNull(result);
        verify(storageNodeRepository, never()).save(any(StorageNodeEntity.class));
    }

    @Test void createStorageNode_withParentNotFolder_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(20);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(
                User.builder().id(1).username("testuser").build());
        when(campaignService.getCampaign(10)).thenReturn(campaign);

        StorageNodeEntity parentNode = new StorageNodeEntity();
        parentNode.setId(20);
        parentNode.setCampaign(new CampaignEntity());
        parentNode.setFolder(false);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(parentNode));

        StorageNode result = storageNodeService.createStorageNode(request);
        assertNull(result);
        verify(storageNodeRepository, never()).save(any(StorageNodeEntity.class));
    }

    @Test void createStorageNode_withParentNotFound_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        CreateStorageNodeRequest request = new CreateStorageNodeRequest();
        request.setCampaignId(10);
        request.setParentNodeId(20);
        request.setName("test");
        request.setFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(
                User.builder().id(1).username("testuser").build());
        when(campaignService.getCampaign(10)).thenReturn(campaign);

        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.empty());

        StorageNode result = storageNodeService.createStorageNode(request);
        assertNull(result);
        verify(storageNodeRepository, never()).save(any(StorageNodeEntity.class));
    }

    @Test void updateStorageNode_returnsUpdatedNodeForAuthenticatedUser()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        UpdateStorageNodeRequest request = new UpdateStorageNodeRequest();
        request.setId(20);
        request.setName("updated test");
        request.setIsFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of(user, Role.OWNER)).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(
                User.builder().id(1).username("testuser").build());

        StorageNodeEntity existingNode = new StorageNodeEntity();
        existingNode.setId(20);
        existingNode.setCampaign(new CampaignEntity());
        existingNode.setFolder(true);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(existingNode));

        when(entityMapper.toDomain(existingNode.getCampaign()))
                .thenReturn(campaign);
        when(storageNodeRepository.save(existingNode))
                .thenReturn(existingNode);
        when(entityMapper.toDomain(existingNode))
                .thenReturn(StorageNode.builder()
                        .id(20)
                        .campaign(campaign)
                        .name("updated test")
                        .isFolder(false)
                        .build());

        StorageNode result = storageNodeService.updateStorageNode(request);
        assertNotNull(result);
        assertEquals("updated test", result.getName());
    }

    @Test void updateStorageNode_whileUserNotParticipant_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        UpdateStorageNodeRequest request = new UpdateStorageNodeRequest();
        request.setId(20);
        request.setName("updated test");
        request.setIsFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of()).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);

        StorageNodeEntity existingNode = new StorageNodeEntity();
        existingNode.setId(20);
        existingNode.setCampaign(new CampaignEntity());
        existingNode.setFolder(true);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(existingNode));

        when(entityMapper.toDomain(existingNode.getCampaign()))
                .thenReturn(campaign);

        StorageNode result = storageNodeService.updateStorageNode(request);
        assertNull(result);
    }

    @Test void updateStorageNode_withoutAuthentication_returnsNull()
    {
        UpdateStorageNodeRequest request = new UpdateStorageNodeRequest();
        request.setId(20);
        request.setName("updated test");
        request.setIsFolder(false);

        StorageNode result = storageNodeService.updateStorageNode(request);
        assertNull(result);
    }

    @Test void updateStorageNode_withBadRequest_returnsNull()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();

        UpdateStorageNodeRequest request = new UpdateStorageNodeRequest();
        request.setId(20);
        request.setName(null);
        request.setIsFolder(false);

        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of()).build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);

        StorageNodeEntity existingNode = new StorageNodeEntity();
        existingNode.setId(20);
        existingNode.setCampaign(new CampaignEntity());
        existingNode.setFolder(true);
        when(storageNodeRepository.findById(20)).thenReturn(java.util.Optional.of(existingNode));

        when(entityMapper.toDomain(existingNode.getCampaign()))
                .thenReturn(campaign);

        StorageNode result = storageNodeService.updateStorageNode(request);
        assertNull(result);
    }

    @Test void deleteStorageNode_returnsTrueForAuthenticatedUser()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder()
                .id(10)
                .participants(java.util.Map.of(user, Role.PARTICIPANT))
                .build();
        CampaignEntity campaignEntity = new CampaignEntity();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        StorageNodeEntity storageNode = new StorageNodeEntity();
        storageNode.setCampaign(campaignEntity);
        when(storageNodeRepository.findById(100)).thenReturn(java.util.Optional.of(storageNode));

        when(entityMapper.toDomain(any(CampaignEntity.class))).thenReturn(campaign);

        assertTrue(storageNodeService.deleteStorageNode(100));
    }

    @Test void deleteStorageNode_whileUserNotParticipant_returnsFalse()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder().id(10).participants(java.util.Map.of()).build();
        CampaignEntity campaignEntity = new CampaignEntity();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        StorageNodeEntity storageNode = new StorageNodeEntity();
        storageNode.setCampaign(campaignEntity);
        when(storageNodeRepository.findById(100)).thenReturn(java.util.Optional.of(storageNode));

        when(entityMapper.toDomain(any(CampaignEntity.class))).thenReturn(campaign);
        assertFalse(storageNodeService.deleteStorageNode(100));
    }

    @Test void deleteStorageNode_withoutAuthentication_returnsFalse()
    {
        assertFalse(storageNodeService.deleteStorageNode(100));
    }

    @Test
    void deleteNonEmptyFolderDeletesAllChildren()
    {
        setAuthenticatedUser("testuser");

        User user = User.builder().id(1).username("testuser").build();
        Campaign campaign = Campaign.builder()
                .id(10)
                .participants(java.util.Map.of(user, Role.PARTICIPANT))
                .build();
        CampaignEntity campaignEntity = new CampaignEntity();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        StorageNodeEntity folder = new StorageNodeEntity();
        folder.setId(100);
        folder.setCampaign(campaignEntity);
        folder.setFolder(true);

        StorageNodeEntity childFolder = new StorageNodeEntity();
        childFolder.setId(101);
        childFolder.setCampaign(campaignEntity);
        childFolder.setParentNode(folder);
        childFolder.setFolder(true);

        StorageNodeEntity childFile = new StorageNodeEntity();
        childFile.setId(102);
        childFile.setCampaign(campaignEntity);
        childFile.setParentNode(folder);

        StorageNodeEntity grandchild = new StorageNodeEntity();
        grandchild.setId(103);
        grandchild.setCampaign(campaignEntity);
        grandchild.setParentNode(childFolder);

        when(storageNodeRepository.findById(100)).thenReturn(java.util.Optional.of(folder));
        when(storageNodeRepository.findByParentNode_Id(100)).thenReturn(List.of(childFolder, childFile));
        when(storageNodeRepository.findByParentNode_Id(101)).thenReturn(List.of(grandchild));
        when(storageNodeRepository.findByParentNode_Id(102)).thenReturn(List.of());
        when(storageNodeRepository.findByParentNode_Id(103)).thenReturn(List.of());

        when(entityMapper.toDomain(any(CampaignEntity.class))).thenReturn(campaign);
        assertTrue(storageNodeService.deleteStorageNode(folder.getId()));

        // Verify the deletion order
        InOrder deletionOrder = inOrder(storageNodeRepository);
        deletionOrder.verify(storageNodeRepository).delete(grandchild);
        deletionOrder.verify(storageNodeRepository).delete(childFolder);
        deletionOrder.verify(storageNodeRepository).delete(childFile);
        deletionOrder.verify(storageNodeRepository).delete(folder);
        verify(storageNodeRepository, times(4)).delete(any(StorageNodeEntity.class));
    }

    private void setAuthenticatedUser(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "credentials",
                        List.of(() -> "ROLE_USER")));
    }
}
