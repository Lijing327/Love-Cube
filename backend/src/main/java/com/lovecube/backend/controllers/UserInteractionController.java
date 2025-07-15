package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user-interactions")
public class UserInteractionController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserInteractionController.class);
    
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
     * 关注用户
     */
    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<?> followUser(@PathVariable Long targetUserId,
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
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户不存在"));
            }

            // 检查是否已经关注
            if (userInteractionService.isFollowing(currentUserId, targetUserId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "已经关注该用户"));
            }

            userInteractionService.followUser(currentUserId, targetUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关注成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("关注用户失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "关注失败: " + e.getMessage()));
        }
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/follow/{targetUserId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long targetUserId,
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
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户不存在"));
            }

            userInteractionService.unfollowUser(currentUserId, targetUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "取消关注成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("取消关注用户失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "取消关注失败: " + e.getMessage()));
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
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户不存在"));
            }

            boolean canLeave = userInteractionService.areMutuallyInteracted(currentUserId, targetUserId);

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
} 