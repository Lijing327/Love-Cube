package com.lovecube.backend.repository;


import com.lovecube.backend.dto.ChatPartnerDTO;
import com.lovecube.backend.models.ChatMessage;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>
{
    List<ChatMessage> findByReceiverIdAndIsReadFalse(Long receiverId);
    @Query("SELECT c FROM ChatMessage c WHERE (c.senderId = :userId AND c.receiverId = :receiverId) " +
            "OR (c.senderId = :receiverId AND c.receiverId = :userId) ORDER BY c.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("userId") Long userId, @Param("receiverId") Long receiverId);

    // 查询所有有过聊天的用户
    @Query("SELECT DISTINCT u.userid, u.username, u.profile_photo " +
            "FROM User u " +
            "WHERE u.userid IN ( " +
            "    SELECT c.senderId FROM ChatMessage c WHERE c.receiverId = :userId " +
            "    UNION " +
            "    SELECT c.receiverId FROM ChatMessage c WHERE c.senderId = :userId " +
            ")")
    List<Object[]> findDistinctChatPartners(@Param("userId") Long userId);

    // 查询最近一条聊天消息
    @Query("SELECT c FROM ChatMessage c WHERE (c.senderId = :userId AND c.receiverId = :partnerId) " +
            "OR (c.senderId = :partnerId AND c.receiverId = :userId) " +
            "ORDER BY c.timestamp DESC LIMIT 1")
    ChatMessage findLatestMessage(@Param("userId") Long userId, @Param("partnerId") Long partnerId);


    // 删除聊天记录
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage c WHERE (c.senderId = :userId AND c.receiverId = :receiverId) " +
            "OR (c.senderId = :receiverId AND c.receiverId = :userId)")
    void deleteChat(@Param("userId") Long userId, @Param("receiverId") Long receiverId);
}
