package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    
    @Autowired
    private UserInteractionService interactionService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 点赞用户
     */
    @PostMapping("/like/{userId}")
    public ResponseEntity<?> likeUser(@PathVariable Long userId,
                                     @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            
            // 检查是否已经点赞
            boolean isLiked = interactionService.isLiked(currentUser.getUserid(), userId, userId);
            if (isLiked) {
                // 如果已经点赞，则取消点赞
                interactionService.unlikeUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "取消点赞成功",
                    "isLiked", false
                ));
            } else {
                // 如果没有点赞，则点赞
                interactionService.likeUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "点赞成功",
                    "isLiked", true
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 关注用户
     */
    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            
            // 检查是否已经关注
            boolean isFollowing = interactionService.isFollowing(currentUser.getUserid(), userId);
            if (isFollowing) {
                // 如果已经关注，则取消关注
                interactionService.unfollowUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "取消关注成功",
                    "isFollowing", false
                ));
            } else {
                // 如果没有关注，则关注
                interactionService.followUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "关注成功",
                    "isFollowing", true
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从token获取当前用户
     */
    private User getCurrentUser(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            
            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);
            
            if (openid == null) {
                return null;
            }
            
            return userRepository.findByOpenid(openid);
        } catch (Exception e) {
            return null;
        }
    }
}
