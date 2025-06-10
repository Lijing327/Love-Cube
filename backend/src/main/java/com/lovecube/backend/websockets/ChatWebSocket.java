package com.lovecube.backend.websockets;

import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.services.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocket {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        chatMessageService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/chat." + message.getReceiverId(), message);
    }
}
