package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.MessageWall;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.MessageWallService;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message-wall")
public class MessageWallController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageWallController.class);
    
    @Autowired
    private MessageWallService messageWallService;
    
    @Autowired
    private UserInteractionService userInteractionService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 通过token获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return null;
        }
        User user = userRepository.findByOpenid(openid);
        return user != null ? user.getUserid() : null;
    }

    /**
     * 获取用户的留言墙消息
     */
    @GetMapping("/messages/{userId}")
    public ResponseEntity<?> getMessageWallMessages(@PathVariable Long userId,
                                                   @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "token 无效"));
            }

            List<Map<String, Object>> messages = messageWallService.getMessageWallMessages(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取留言墙消息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "获取留言墙消息失败: " + e.getMessage()));
        }
    }

    /**
     * 发送留言
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> messageData,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "token 无效"));
            }

            // 从token中获取当前用户ID
            Long senderId = getUserIdFromToken(token);
            Long receiverId = Long.parseLong(messageData.get("targetUserId").toString());
            String content = messageData.get("content").toString();

            // 检查是否可以留言
            if (!messageWallService.canLeaveMessage(senderId, receiverId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "需要相互关注或喜欢后才能留言"));
            }

            MessageWall message = messageWallService.sendMessage(senderId, receiverId, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "id", message.getId(),
                "content", message.getContent(),
                "createdAt", message.getCreatedAt().toString()
            ));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("发送留言失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "发送留言失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否可以留言
     */
    @GetMapping("/can-leave-message/{targetUserId}")
    public ResponseEntity<?> canLeaveMessage(@PathVariable Long targetUserId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "token 无效"));
            }

            Long currentUserId = getUserIdFromToken(token);
            boolean canLeave = messageWallService.canLeaveMessage(currentUserId, targetUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", canLeave);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("检查留言权限失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "检查留言权限失败: " + e.getMessage()));
        }
    }

    /**
     * 获取未读留言数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadMessageCount(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "token 无效"));
            }

            Long userId = getUserIdFromToken(token);
            Long unreadCount = messageWallService.getUnreadMessageCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取未读留言数量失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "获取未读留言数量失败: " + e.getMessage()));
        }
    }

    /**
     * 标记所有留言为已读
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "token 无效"));
            }

            Long userId = getUserIdFromToken(token);
            messageWallService.markAllAsRead(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已标记所有留言为已读");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("标记留言已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "标记留言已读失败: " + e.getMessage()));
        }
    }
} 