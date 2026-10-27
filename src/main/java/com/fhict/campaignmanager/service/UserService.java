package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.repository.UserRepository;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService
{
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEntityMapper entityMapper;

    @Autowired
    public UserService(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder,
                       DomainEntityMapper entityMapper) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityMapper = entityMapper;
    }

    public UserService(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userMapper, userRepository, passwordEncoder, new DomainEntityMapper());
    }

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toUser(createUserRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userMapper.toUserResponse(entityMapper.toDomain(userRepository.save(entityMapper.toEntity(user))));
    }

    @Override
    public UserResponse getUser(int id) {
        User user = entityMapper.toDomain(userRepository.findById(id).orElse(null));
        if (user != null) {
            return userMapper.toUserResponse(user);
        }
        return null;
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
    public UserResponse updateUser(int id, UpdateUserRequest updateUserRequest) {
        User user = entityMapper.toDomain(userRepository.findById(id).orElse(null));
        if (user != null)
        {
            user.setUsername(updateUserRequest.getUsername());
            user.setEmail(updateUserRequest.getEmail());
            User updatedUser = entityMapper.toDomain(userRepository.save(entityMapper.toEntity(user)));
            return userMapper.toUserResponse(updatedUser);
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
