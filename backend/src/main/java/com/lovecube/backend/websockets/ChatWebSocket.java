package com.lovecube.backend.websockets;

import com.fasterxml.jackson.databind.JsonNode;
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
            String payload = message.getPayload();
            logger.info("收到WebSocket消息: {}", payload);
            
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // 检查必要字段是否存在
            if (!jsonNode.has("type")) {
                logger.error("消息缺少type字段: {}", payload);
                return;
            }
            
            String messageType = jsonNode.get("type").asText();
            
            // 处理心跳消息
            if ("ping".equals(messageType)) {
                // 回复心跳
                session.sendMessage(new TextMessage("{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}"));
                return;
            }
            
            // 处理聊天消息
            if ("chat".equals(messageType)) {
                // 安全地获取字段值
                Long senderId = null;
                Long receiverId = null;
                String content = null;
                
                if (jsonNode.has("senderId") && !jsonNode.get("senderId").isNull()) {
                    senderId = jsonNode.get("senderId").asLong();
                }
                
                if (jsonNode.has("receiverId") && !jsonNode.get("receiverId").isNull()) {
                    receiverId = jsonNode.get("receiverId").asLong();
                }
                
                if (jsonNode.has("content") && !jsonNode.get("content").isNull()) {
                    content = jsonNode.get("content").asText();
                }
                
                // 验证必要字段
                if (senderId == null || receiverId == null || content == null || content.trim().isEmpty()) {
                    logger.error("消息字段不完整: senderId={}, receiverId={}, content={}", senderId, receiverId, content);
                    logger.error("原始消息: {}", payload);
                    
                    // 发送错误响应给客户端
                    String errorResponse = objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", "消息字段不完整",
                        "timestamp", System.currentTimeMillis()
                    ));
                    session.sendMessage(new TextMessage(errorResponse));
                    return;
                }
                
                logger.info("解析消息成功: senderId={}, receiverId={}, content={}", senderId, receiverId, content);
                
                // 创建聊天消息对象
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderId(senderId);
                chatMessage.setReceiverId(receiverId);
                chatMessage.setContent(content);
                // 暂时注释掉type字段设置，因为数据库表中没有这个字段
                // chatMessage.setType("chat");
                chatMessage.setTimestamp(System.currentTimeMillis());
                chatMessage.setRead(false);
                
                // 保存消息
                ChatMessage savedMessage = chatMessageService.saveMessage(chatMessage);
                logger.info("消息已保存: ID={}, 发送者={}, 接收者={}", savedMessage.getId(), senderId, receiverId);
                
                // 准备要发送的消息（只发送给接收者）
                String responseMessage = objectMapper.writeValueAsString(Map.of(
                    "id", savedMessage.getId(),
                    "type", "chat",
                    "senderId", senderId,
                    "receiverId", receiverId,
                    "content", content,
                    "timestamp", savedMessage.getTimestamp(),
                    "isRead", false
                ));
                
                // 只发送给接收者（如果在线）
                WebSocketSession receiverSession = userSessions.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.sendMessage(new TextMessage(responseMessage));
                    logger.info("消息已发送给接收者用户 {}", receiverId);
                } else {
                    logger.info("接收者用户 {} 不在线，消息已存储", receiverId);
                }
                
                // 向发送者发送成功确认（不包含消息内容，避免重复显示）
                String confirmMessage = objectMapper.writeValueAsString(Map.of(
                    "type", "sent_confirm",
                    "messageId", savedMessage.getId(),
                    "timestamp", savedMessage.getTimestamp(),
                    "status", "success"
                ));
                
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(confirmMessage));
                    logger.info("发送确认已发送给发送者用户 {}", senderId);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理消息时发生错误: {}", message.getPayload(), e);
            try {
                // 发送错误响应
                String errorResponse = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "消息处理失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
                session.sendMessage(new TextMessage(errorResponse));
            } catch (IOException ex) {
                logger.error("发送错误响应失败", ex);
            }
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
            logger.error("无法从会话中提取用户ID: {}", path, e);
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
