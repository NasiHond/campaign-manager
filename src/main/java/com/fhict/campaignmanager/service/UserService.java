package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.repository.UserRepository;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEntityMapper entityMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       DomainEntityMapper entityMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityMapper = entityMapper;
    }

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, new DomainEntityMapper());
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) {
        User user = User.builder()
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .password(createUserRequest.getPassword())
                .build();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return entityMapper.toDomain(userRepository.save(entityMapper.toEntity(user)));
    }

    @Override
    public User getUser(int id) {
        return entityMapper.toDomain(userRepository.findById(id).orElse(null));
    }

    @Override
    public User getUserById(int id) {
        return entityMapper.toDomain(userRepository.findById(id).orElse(null));
    }

    @Override
    public User getUserByUsername(String username) {
        return entityMapper.toDomain(userRepository.findByUsername(username));
    }

    @Override
    public User getUserByEmail(String email) {
        return entityMapper.toDomain(userRepository.findByEmail(email));
    }

    @Override
    public User updateUser(int id, UpdateUserRequest updateUserRequest) {
        User user = entityMapper.toDomain(userRepository.findById(id).orElse(null));
        if (user != null)
        {
            user.setUsername(updateUserRequest.getUsername());
            user.setEmail(updateUserRequest.getEmail());
            return entityMapper.toDomain(userRepository.save(entityMapper.toEntity(user)));
        }
        return null;
    }

    @Override
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean checkUsername(String username) {

        return false;
    }

    @Override
    public boolean checkEmail(String email) {
        return false;
    }

    @Override
    public boolean checkPassword(String password) {
        return false;
    }


}
