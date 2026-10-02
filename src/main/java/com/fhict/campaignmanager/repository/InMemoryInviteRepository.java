package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.CampaignInvitation;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryInviteRepository implements InviteRepository {

    private final Map<Integer, CampaignInvitation> invitations = new HashMap<>();
    private int nextId = 1;

    @Override
    public CampaignInvitation save(CampaignInvitation invitation) {
        if (invitation.getId() == 0) {
            invitation.setId(nextId++);
        }

        invitations.put(invitation.getId(), invitation);
        return invitation;
    }

    @Override
    public CampaignInvitation findById(int id) {
        return invitations.get(id);
    }

    @Override
    public List<CampaignInvitation> findAllByUserId(int userId) {
        return invitations.values().stream()
                .filter(invitation -> invitation.getInvitedUser().getId() == userId)
                .toList();
    }

    @Override
    public List<CampaignInvitation> findAllByCampaignId(int campaignId) {
        return invitations.values().stream()
                .filter(invitation -> invitation.getCampaign().getId() == campaignId)
                .toList();
    }
}
