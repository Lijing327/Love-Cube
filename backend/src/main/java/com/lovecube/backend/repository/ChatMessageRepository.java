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
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findByUserPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    List<ChatMessage> findByReceiverIdAndIsReadFalse(Long receiverId);
    @Query("SELECT c FROM ChatMessage c WHERE (c.senderId = :userId AND c.receiverId = :receiverId) " +
            "OR (c.senderId = :receiverId AND c.receiverId = :userId) ORDER BY c.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("userId") Long userId, @Param("receiverId") Long receiverId);

    // 查询所有有过聊天的用户
    @Query("SELECT DISTINCT u.userid, u.username, u.profilePhoto " +
            "FROM User u " +
            "WHERE u.userid IN ( " +
            "    SELECT c.senderId FROM ChatMessage c WHERE c.receiverId = :userId " +
            "    UNION " +
            "    SELECT c.receiverId FROM ChatMessage c WHERE c.senderId = :userId " +
            ")")
    List<Object[]> findDistinctChatPartners(@Param("userId") Long userId);

    // 查询最近一条聊天消息
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "ORDER BY m.timestamp DESC LIMIT 1")
    ChatMessage findLatestChatMessage(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);

    // 删除聊天记录
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage c WHERE (c.senderId = :userId AND c.receiverId = :receiverId) " +
            "OR (c.senderId = :receiverId AND c.receiverId = :userId)")
    void deleteChat(@Param("userId") Long userId, @Param("receiverId") Long receiverId);

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.receiverId = :userId AND cm.isRead = false " +
           "ORDER BY cm.timestamp DESC")
    List<ChatMessage> findUnreadMessages(@Param("userId") Long userId);
}
