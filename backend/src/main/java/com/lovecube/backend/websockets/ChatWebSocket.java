package com.lovecube.backend.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.services.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocket extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocket.class);
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            logger.info("用户 {} 已连接", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            chatMessage.setTimestamp(System.currentTimeMillis());
            
            // 保存消息
            chatMessageService.saveMessage(chatMessage);
            
            // 发送给接收者
            WebSocketSession receiverSession = userSessions.get(chatMessage.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                logger.info("消息已发送给用户 {}", chatMessage.getReceiverId());
            } else {
                logger.info("用户 {} 不在线，消息已存储", chatMessage.getReceiverId());
            }
        } catch (IOException e) {
            logger.error("处理消息时发生错误", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            logger.info("用户 {} 已断开连接", userId);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            logger.error("无法从会话中提取用户ID", e);
            return null;
        }
    }

    // 发送消息给特定用户
    public void sendMessageToUser(Long userId, ChatMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                logger.error("发送消息给用户 {} 时发生错误", userId, e);
            }
        }
    }
}
