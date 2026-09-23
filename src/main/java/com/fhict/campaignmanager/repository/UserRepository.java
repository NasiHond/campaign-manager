package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.User;
import org.springframework.stereotype.Service;

public interface UserRepository {
    User save(User user);

    User findById(int id);

    User findByUsername(String username);

    User update(User user);

    void delete(int id);
}
