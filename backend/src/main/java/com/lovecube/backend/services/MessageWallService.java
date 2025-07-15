package com.lovecube.backend.services;

import com.lovecube.backend.entity.MessageWall;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.MessageWallRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageWallService {
    
    @Autowired
    private MessageWallRepository messageWallRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInteractionService userInteractionService;

    /**
     * 获取用户的留言墙消息
     */
    public List<Map<String, Object>> getMessageWallMessages(Long receiverId) {
        List<MessageWall> messages = messageWallRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
        
        return messages.stream().map(message -> {
            Map<String, Object> messageMap = new HashMap<>();
            
            // 获取发送者信息
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            
            messageMap.put("id", message.getId());
            messageMap.put("content", message.getContent());
            messageMap.put("createdAt", message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            messageMap.put("senderId", message.getSenderId());
            messageMap.put("senderName", sender != null ? sender.getUsername() : "未知用户");
            messageMap.put("senderAvatar", sender != null ? sender.getProfilePhoto() : "/images/default-avatar.png");
            messageMap.put("isRead", message.getIsRead());
            
            return messageMap;
        }).collect(Collectors.toList());
    }

    /**
     * 发送留言
     */
    public MessageWall sendMessage(Long senderId, Long receiverId, String content) {
        MessageWall message = new MessageWall(senderId, receiverId, content);
        return messageWallRepository.save(message);
    }

    /**
     * 检查用户是否可以留言（需要相互关注）
     */
    public boolean canLeaveMessage(Long senderId, Long receiverId) {
        // 检查是否相互关注或者相互喜欢
        return userInteractionService.areMutuallyInteracted(senderId, receiverId);
    }

    /**
     * 获取用户未读留言数量
     */
    public Long getUnreadMessageCount(Long userId) {
        return messageWallRepository.countUnreadMessages(userId);
    }

    /**
     * 标记用户的所有留言为已读
     */
    public void markAllAsRead(Long userId) {
        messageWallRepository.markAllAsRead(userId);
    }
} 