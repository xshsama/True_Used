package com.xsh.trueused.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.chat.dto.ChatMessageDTO;
import com.xsh.trueused.chat.dto.SendMessageRequest;
import com.xsh.trueused.security.user.UserPrincipal;
import com.xsh.trueused.chat.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

        private final ChatMessageService chatMessageService;

        @PostMapping
        public ResponseEntity<ChatMessageDTO> sendMessage(@RequestBody SendMessageRequest request,
                        @AuthenticationPrincipal UserPrincipal sender) {
                if (sender == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }

                // Save message to DB
                ChatMessageDTO savedMessage = chatMessageService.saveMessage(sender.getId(), request.getReceiverId(),
                                request.getContent());

                return ResponseEntity.ok(savedMessage);
        }
}
