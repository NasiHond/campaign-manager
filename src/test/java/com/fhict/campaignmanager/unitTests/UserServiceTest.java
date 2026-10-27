package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.UserEntity;
import com.fhict.campaignmanager.repository.UserRepository;
import com.fhict.campaignmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Spy
    private DomainEntityMapper entityMapper = new DomainEntityMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private UserEntity entity(User user) { return new DomainEntityMapper().toEntity(user); }

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, userRepository, passwordEncoder, entityMapper);
    }

    @Test
    void createUser_encodesPasswordSavesUserAndReturnsResponse() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("NasiHond")
                .email("test@mail.com")
                .password("12345")
                .build();

        User user = User.builder().password("12345").build();
        User savedUser = User.builder()
                .id(1)
                .username("NasiHond")
                .email("test@mail.com")
                .password("hashed-password")
                .build();
        UserResponse expected = UserResponse.builder()
                .id(1)
                .username("NasiHond")
                .email("test@mail.com")
                .build();

        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode("12345")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity(savedUser));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        UserResponse result = userService.createUser(request);

        assertEquals(expected, result);
        assertEquals("hashed-password", user.getPassword());
        verify(passwordEncoder).encode("12345");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void getUser_returnsMappedUserWhenFound() {
        User user = User.builder().id(1).username("NasiHond").build();
        UserResponse expected = UserResponse.builder().id(1).username("NasiHond").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(entity(user)));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        assertEquals(expected, userService.getUser(1));
    }

    @Test
    void getUser_returnsNullWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertNull(userService.getUser(99));
        verifyNoInteractions(userMapper);
    }

    @Test
    void getUserById_returnsDomainUserFromRepository() {
        User expected = User.builder().id(1).username("NasiHond").build();
        when(userRepository.findById(1)).thenReturn(Optional.of(entity(expected)));

        assertEquals(expected, userService.getUserById(1));
        verify(userRepository).findById(1);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_updatesUserAndReturnsMappedResponse() {
        User user = User.builder()
                .id(1)
                .username("old-name")
                .email("old@mail.com")
                .build();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("new-name")
                .email("new@mail.com")
                .build();
        UserResponse expected = UserResponse.builder()
                .id(1)
                .username("new-name")
                .email("new@mail.com")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(entity(user)));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expected);

        UserResponse result = userService.updateUser(1, request);

        assertEquals(expected, result);
        ArgumentCaptor<UserEntity> savedCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedCaptor.capture());
        UserEntity saved = savedCaptor.getValue();
        assertEquals("new-name", saved.getUsername());
        assertEquals("new@mail.com", saved.getEmail());
    }

    @Test
    void deleteUser_delegatesToRepository() {
        userService.deleteUser(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void getUserByUsername_delegatesToRepository() {
        User expected = User.builder().username("NasiHond").build();
        when(userRepository.findByUsername("NasiHond")).thenReturn(entity(expected));

        assertEquals(expected, userService.getUserByUsername("NasiHond"));
        verify(userRepository).findByUsername("NasiHond");
    }

    @Test
    void getUserByEmail_delegatesToRepository() {
        User expected = User.builder().email("test@mail.com").build();
        when(userRepository.findByEmail("test@mail.com")).thenReturn(entity(expected));

        assertEquals(expected, userService.getUserByEmail("test@mail.com"));
        verify(userRepository).findByEmail("test@mail.com");
    }

    @Test
    void updateUser_returnsNullWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertNull(userService.updateUser(99, UpdateUserRequest.builder()
                .username("new-name")
                .email("new@mail.com")
                .build()));
        verify(userRepository, never()).save(any());
    }
}
