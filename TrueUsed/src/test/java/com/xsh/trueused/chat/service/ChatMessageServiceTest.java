package com.xsh.trueused.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.chat.repository.ChatMessageRepository;
import com.xsh.trueused.chat.repository.ChatSessionRepository;
import com.xsh.trueused.entity.ChatSession;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpUserRegistry userRegistry;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    void markAsReadShouldRejectNonParticipant() {
        when(chatSessionService.getChatSession(1L)).thenReturn(session(100L, 200L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatMessageService.markAsRead(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to update this conversation", ex.getReason());
    }

    private static ChatSession session(Long userAId, Long userBId) {
        ChatSession session = new ChatSession();
        session.setUserA(user(userAId));
        session.setUserB(user(userBId));
        return session;
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
