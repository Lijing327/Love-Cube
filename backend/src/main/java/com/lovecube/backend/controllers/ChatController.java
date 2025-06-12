package com.lovecube.backend.controllers;

import com.lovecube.backend.dto.ChatPartnerDTO;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.repository.ChatMessageRepository;
import com.lovecube.backend.services.ChatMessageService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 获取聊天历史记录
     */
    @GetMapping("/chat/history/{userId}/{receiverId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long userId, 
                                          @PathVariable Long receiverId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取聊天历史记录
            List<ChatMessage> messages = chatMessageRepository.findChatHistory(userId, receiverId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> formattedMessages = messages.stream()
                .map(msg -> {
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("id", msg.getId());
                    messageMap.put("content", msg.getContent());
                    messageMap.put("type", msg.getType());
                    messageMap.put("senderId", msg.getSenderId());
                    messageMap.put("receiverId", msg.getReceiverId());
                    messageMap.put("timestamp", msg.getTimestamp());
                    messageMap.put("isRead", msg.isRead());
                    return messageMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(formattedMessages);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取聊天记录失败: " + e.getMessage()));
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/chat/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> messageData,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 创建新消息
            ChatMessage message = new ChatMessage();
            message.setSenderId(Long.parseLong(messageData.get("senderId").toString()));
            message.setReceiverId(Long.parseLong(messageData.get("receiverId").toString()));
            message.setContent(messageData.get("content").toString());
            message.setType(messageData.getOrDefault("type", "chat").toString());

            // 保存消息
            ChatMessage savedMessage = chatMessageRepository.save(message);

            // 返回保存的消息
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedMessage.getId());
            response.put("content", savedMessage.getContent());
            response.put("type", savedMessage.getType());
            response.put("senderId", savedMessage.getSenderId());
            response.put("receiverId", savedMessage.getReceiverId());
            response.put("timestamp", savedMessage.getTimestamp());
            response.put("isRead", savedMessage.isRead());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "发送消息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/chat/unread/{userId}")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            Long unreadCount = chatMessageRepository.countUnreadMessages(userId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取未读消息数量失败: " + e.getMessage()));
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/chat/markRead/{userId}/{senderId}")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Long userId,
                                              @PathVariable Long senderId,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取未读消息并标记为已读
            List<ChatMessage> unreadMessages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
            unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(senderId))
                .forEach(msg -> {
                    msg.setRead(true);
                    chatMessageRepository.save(msg);
                });

            return ResponseEntity.ok(Map.of("message", "消息已标记为已读"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "标记消息已读失败: " + e.getMessage()));
        }
    }

    // 获取所有有过聊天的用户
    @GetMapping("/partners/{userId}")
    public List<ChatPartnerDTO> getChatPartners(@PathVariable Long userId) {
        List<ChatPartnerDTO> partners = chatMessageService.getChatPartners(userId);
        System.out.println("✅ 获取聊天用户列表: " + partners);
        return partners;
    }

    // 删除聊天记录（彻底删除）
    @DeleteMapping("/delete/{userId}/{receiverId}")
    public ResponseEntity<?> deleteChat(@PathVariable Long userId, @PathVariable Long receiverId) {
        chatMessageService.deleteChat(userId, receiverId);
        return ResponseEntity.ok("✅ 聊天删除成功");
    }
}