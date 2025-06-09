package com.lovecube.backend.services;


import com.lovecube.backend.dto.ChatPartnerDTO;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 存储消息
    public void saveMessage(ChatMessage msg) {
        msg.setRead(false);
        chatMessageRepository.save(msg);
    }

    // 获取未读消息
    public List<ChatMessage> getOfflineMessages(Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
        messages.forEach(msg -> msg.setRead(true));  // 标记为已读
        chatMessageRepository.saveAll(messages);
        return messages;
    }

    // 获取聊天记录

    public List<ChatMessage> getChatHistory(Long userId, Long receiverId) {
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(userId, receiverId);
        System.out.println("🔍 获取聊天记录 - 用户 " + userId + " 和 " + receiverId);
        messages.forEach(msg -> System.out.println(msg));
        return messages;
    }

    // 获取所有有过聊天的用户
    public List<ChatPartnerDTO> getChatPartners(Long userId) {
        return chatMessageRepository.findDistinctChatPartners(userId)
                .stream()
                .map(obj -> {
                    Long partnerId = ((Number) obj[0]).longValue(); // 对方用户 ID
                    String username = (String) obj[1];  // 用户名
                    String avatar = (String) obj[2];  // 头像 URL

                    // 获取最近一条消息
                    ChatMessage latestMessage = chatMessageRepository.findLatestMessage(userId, partnerId);

                    String lastMessage = latestMessage != null ? latestMessage.getContent() : "暂无聊天记录";
                    String time = latestMessage != null ? formatTimestamp(latestMessage.getTimestamp()) : "";

                    return new ChatPartnerDTO(partnerId, username, avatar, lastMessage, time);
                })
                .collect(Collectors.toList());
    }

    // 格式化时间
    private String formatTimestamp(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        return sdf.format(new Date(timestamp));
    }
    // 删除聊天记录（彻底删除）
    public void deleteChat(Long userId, Long receiverId) {
        chatMessageRepository.deleteChat(userId, receiverId);
    }
}