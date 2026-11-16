package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.UserEntity;
import com.fhict.campaignmanager.repository.UserRepository;
import com.fhict.campaignmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private UserEntity entity(User user) {
        return new DomainEntityMapper().toEntity(user);
    }

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_encodesPasswordSavesUserAndReturnsDomainUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("NasiHond")
                .email("test@mail.com")
                .password("12345")
                .build();
        User savedUser = User.builder()
                .id(1)
                .username("NasiHond")
                .email("test@mail.com")
                .password("hashed-password")
                .build();

        when(passwordEncoder.encode("12345")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity(savedUser));

        User result = userService.createUser(request);

        assertEquals(savedUser, result);
        ArgumentCaptor<UserEntity> savedCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedCaptor.capture());
        assertEquals("NasiHond", savedCaptor.getValue().getUsername());
        assertEquals("test@mail.com", savedCaptor.getValue().getEmail());
        assertEquals("hashed-password", savedCaptor.getValue().getPassword());
        verify(passwordEncoder).encode("12345");
    }

    @Test
    void getUser_returnsDomainUserWhenFound() {
        User expected = User.builder().id(1).username("NasiHond").build();
        when(userRepository.findById(1)).thenReturn(Optional.of(entity(expected)));

        assertEquals(expected, userService.getUser(1));
    }

    @Test
    void getUser_returnsNullWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertNull(userService.getUser(99));
    }

    @Test
    void getUserById_returnsDomainUserFromRepository() {
        User expected = User.builder().id(1).username("NasiHond").build();
        when(userRepository.findById(1)).thenReturn(Optional.of(entity(expected)));

        assertEquals(expected, userService.getUserById(1));
        verify(userRepository).findById(1);
    }

    @Test
    void updateUser_updatesUserAndReturnsDomainUser() {
        User user = User.builder()
                .id(1)
                .username("old-name")
                .email("old@mail.com")
                .build();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("new-name")
                .email("new@mail.com")
                .build();
        User expected = User.builder()
                .id(1)
                .username("new-name")
                .email("new@mail.com")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(entity(user)));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(expected, userService.updateUser(1, request));
        ArgumentCaptor<UserEntity> savedCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedCaptor.capture());
        assertEquals("new-name", savedCaptor.getValue().getUsername());
        assertEquals("new@mail.com", savedCaptor.getValue().getEmail());
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
