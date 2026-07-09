package com.xsh.trueused.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.xsh.trueused.auth.dto.RegisterRequest;
import com.xsh.trueused.auth.repository.RoleRepository;
import com.xsh.trueused.entity.Role;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.RoleName;
import com.xsh.trueused.enums.UserStatus;
import com.xsh.trueused.user.dto.UserDTO;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterService registerService;

    @Test
    void registerShouldCreateActiveUserWithDefaultRole() {
        RegisterRequest request = new RegisterRequest("xsh", "xsh@example.com", "secret123");
        Role role = role(RoleName.ROLE_USER);

        when(userRepository.existsByUsername("xsh")).thenReturn(false);
        when(userRepository.existsByEmail("xsh@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(100L);
            return user;
        });

        UserDTO dto = registerService.register(request);

        assertEquals(100L, dto.id());
        assertEquals("xsh", dto.username());
        assertEquals("xsh@example.com", dto.email());
        assertEquals(UserStatus.ACTIVE, dto.status());
        assertTrue(dto.roles().contains("ROLE_USER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.getRoles().contains(role));
    }

    @Test
    void registerShouldRejectDuplicateUsernameBeforeEncodingPassword() {
        RegisterRequest request = new RegisterRequest("xsh", "xsh@example.com", "secret123");
        when(userRepository.existsByUsername("xsh")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerService.register(request));

        assertEquals("用户名已存在", ex.getMessage());
        verify(userRepository, never()).existsByEmail("xsh@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldRejectDuplicateEmailBeforeSavingUser() {
        RegisterRequest request = new RegisterRequest("xsh", "xsh@example.com", "secret123");
        when(userRepository.existsByUsername("xsh")).thenReturn(false);
        when(userRepository.existsByEmail("xsh@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerService.register(request));

        assertEquals("邮箱已存在", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    private static Role role(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
