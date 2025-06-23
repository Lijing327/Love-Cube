package com.lovecube.backend.services;

import com.lovecube.backend.dto.ChatPartnerDTO;
import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 存储消息
    public ChatMessage saveMessage(ChatMessage msg) {
        // 暂时注释掉type字段相关代码，因为数据库表中没有这个字段
        /*
        if (msg.getType() == null) {
            msg.setType("chat");
        }
        */
        msg.setRead(false);
        msg.setTimestamp(System.currentTimeMillis());
        return chatMessageRepository.save(msg);
    }

    // 获取未读消息
    public List<ChatMessage> getUnreadMessages(Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
        messages.forEach(msg -> msg.setRead(true));  // 标记为已读
        chatMessageRepository.saveAll(messages);
        return messages;
    }

    // 获取未读消息数量
    public Long getUnreadMessageCount(Long userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    // 获取聊天记录
    public List<ChatMessage> getChatHistory(Long userId, Long receiverId) {
        List<ChatMessage> messages = chatMessageRepository.findByUserPair(userId, receiverId);
        logger.info("获取聊天记录 - 用户 {} 和 {}, 共 {} 条消息", userId, receiverId, messages.size());
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
                    ChatMessage latestMessage = chatMessageRepository.findLatestChatMessage(userId, partnerId);

                    String lastMessage = latestMessage != null ? latestMessage.getContent() : "暂无聊天记录";
                    String time = latestMessage != null ? formatTimestamp(latestMessage.getTimestamp()) : "";
                    Long unreadCount = chatMessageRepository.countUnreadMessages(userId);

                    return new ChatPartnerDTO(partnerId, username, avatar, lastMessage, time, unreadCount.intValue());
                })
                .collect(Collectors.toList());
    }

    // 格式化时间
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            logger.error("时间格式化失败: {}", timestamp, e);
            return "";
        }
    }

    // 删除聊天记录
    public void deleteChat(Long userId, Long receiverId) {
        chatMessageRepository.deleteChat(userId, receiverId);
        logger.info("删除聊天记录 - 用户 {} 和 {}", userId, receiverId);
    }

    // 标记消息为已读
    public void markMessagesAsRead(Long userId, Long senderId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
        unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(senderId))
                .forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
        logger.info("标记消息为已读 - 接收者: {}, 发送者: {}", userId, senderId);
    }
}