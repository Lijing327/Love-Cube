package com.lovecube.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.services.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            logger.info("WebSocket连接建立成功，用户ID: {}", userId);
            
            // 发送连接成功消息
            try {
                Map<String, Object> response = Map.of(
                    "type", "connected",
                    "userId", userId,
                    "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            } catch (IOException e) {
                logger.error("发送连接成功消息失败", e);
            }
        } else {
            logger.error("无法获取用户ID，关闭连接");
            try {
                session.close(CloseStatus.BAD_DATA.withReason("无法获取用户ID"));
            } catch (IOException e) {
                logger.error("关闭WebSocket连接失败", e);
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (!(message instanceof TextMessage)) {
            logger.warn("收到非文本消息，忽略");
            return;
        }

        try {
            String payload = ((TextMessage) message).getPayload();
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
            
            // 处理心跳消息
            if ("ping".equals(chatMessage.getContent())) {
                handlePing(session);
                return;
            }

            // 保存消息到数据库
            chatMessage = chatService.saveMessage(chatMessage);
            
            // 发送消息给接收者
            WebSocketSession receiverSession = sessions.get(chatMessage.getReceiverId().toString());
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            }

            // 发送确认消息给发送者
            Map<String, Object> ack = Map.of(
                "type", "ack",
                "messageId", chatMessage.getId(),
                "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

        } catch (Exception e) {
            logger.error("处理消息失败", e);
            try {
                Map<String, Object> error = Map.of(
                    "type", "error",
                    "message", "消息处理失败",
                    "timestamp", System.currentTimeMillis()
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
            } catch (IOException ex) {
                logger.error("发送错误消息失败", ex);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userId = extractUserId(session);
        logger.error("WebSocket传输错误，用户ID: {}", userId, exception);
        
        if (userId != null) {
            sessions.remove(userId);
        }
        
        try {
            session.close(CloseStatus.SERVER_ERROR.withReason("传输错误"));
        } catch (IOException e) {
            logger.error("关闭WebSocket连接失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String userId = extractUserId(session);
        if (userId != null) {
            sessions.remove(userId);
            logger.info("WebSocket连接关闭，用户ID: {}, 状态: {}", userId, closeStatus);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractUserId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] segments = path.split("/");
        return segments.length > 0 ? segments[segments.length - 1] : null;
    }

    private void handlePing(WebSocketSession session) {
        try {
            Map<String, Object> pong = Map.of(
                "type", "pong",
                "timestamp", System.currentTimeMillis()
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
        } catch (IOException e) {
            logger.error("发送pong消息失败", e);
        }
    }
} 