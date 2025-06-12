package com.lovecube.backend.controllers;

import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.ChatMessageRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.services.UserVisitorService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInteractionService interactionService;
    
    @Autowired
    private UserVisitorService visitorService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/messages/chat")
    public ResponseEntity<?> getChatList(@RequestHeader("Authorization") String authHeader) {
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

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();

            // 获取所有聊天对象
            List<Object[]> chatPartners = chatMessageRepository.findDistinctChatPartners(userId);
            
            List<Map<String, Object>> chatList = chatPartners.stream()
                .map(obj -> {
                    Long partnerId = ((Number) obj[0]).longValue();
                    String username = (String) obj[1];
                    String avatar = (String) obj[2];

                    // 获取最新一条消息
                    ChatMessage latestMessage = chatMessageRepository.findLatestChatMessage(userId, partnerId);
                    
                    Map<String, Object> chatItem = new HashMap<>();
                    chatItem.put("id", partnerId);
                    chatItem.put("nickname", username != null ? username : "未知用户");
                    chatItem.put("avatar", avatar != null ? avatar : "/images/default-avatar.png");
                    chatItem.put("lastMessage", latestMessage != null ? latestMessage.getContent() : "暂无消息");
                    chatItem.put("lastTime", latestMessage != null ? latestMessage.getTimestamp() : System.currentTimeMillis());
                    chatItem.put("unread", getUnreadMessageCount(userId, partnerId));
                    
                    return chatItem;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("lastTime"), (Long) a.get("lastTime"))) // 按时间倒序
                .collect(Collectors.toList());

            return ResponseEntity.ok(chatList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取聊天列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取互动列表（点赞、评论等）
     */
    @GetMapping("/messages/interact")
    public ResponseEntity<?> getInteractList(@RequestHeader("Authorization") String authHeader) {
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

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            // 获取互动列表（默认获取前20条）
            List<Map<String, Object>> interactList = interactionService.getInteractionList(userId, 0, 20);

            return ResponseEntity.ok(interactList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取互动列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/messages/visitor")
    public ResponseEntity<?> getVisitorList(@RequestHeader("Authorization") String authHeader) {
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

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            // 获取访客列表（默认获取前20条）
            List<Map<String, Object>> visitorList = visitorService.getVisitorList(userId, 0, 20);

            return ResponseEntity.ok(visitorList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取访客列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取未读消息数量统计
     */
    @GetMapping("/messages/unread")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
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

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();

            // 获取未读消息数量
            Long chatUnread = chatMessageRepository.countUnreadMessages(userId);
            
            // 获取未读互动数量
            Long interactUnread = interactionService.getUnreadInteractionCount(userId);
            
            // 获取今日新访客数量（作为访客未读数量）
            Long visitorUnread = visitorService.getTodayVisitorCount(userId);
            
            Map<String, Object> unreadCount = new HashMap<>();
            unreadCount.put("chat", chatUnread != null ? chatUnread.intValue() : 0);
            unreadCount.put("interact", interactUnread != null ? interactUnread.intValue() : 0);
            unreadCount.put("visitor", visitorUnread != null ? visitorUnread.intValue() : 0);

            return ResponseEntity.ok(unreadCount);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取未读消息数量失败: " + e.getMessage()));
        }
    }

    /**
     * 获取两个用户之间的未读消息数量
     */
    private int getUnreadMessageCount(Long userId, Long partnerId) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
            return (int) unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(partnerId))
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
} 