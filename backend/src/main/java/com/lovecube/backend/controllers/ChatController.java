package com.lovecube.backend.controllers;


import com.lovecube.backend.dto.ChatPartnerDTO;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.services.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatMessageService chatMessageService;

    // 获取聊天记录
    @GetMapping("/{userId}/{receiverId}")
    public List<ChatMessage> getChatHistory(@PathVariable Long userId, @PathVariable Long receiverId) {
        System.out.println("📩 收到请求: 获取 " + userId + " 和 " + receiverId + " 的聊天记录");
        return chatMessageService.getChatHistory(userId, receiverId);
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