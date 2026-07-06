package com.xsh.trueused.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.entity.Notification;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.notification.repository.NotificationRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void markAsReadShouldRejectAnotherUsersNotification() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user(100L));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to read this notification", ex.getReason());
    }

    private static User user(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
