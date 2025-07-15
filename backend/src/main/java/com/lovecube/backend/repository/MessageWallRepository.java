package com.lovecube.backend.repository;

import com.lovecube.backend.entity.MessageWall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageWallRepository extends JpaRepository<MessageWall, Long> {
    
    /**
     * 获取用户的留言墙消息（只有公开的留言才会显示）
     */
    @Query("SELECT m FROM MessageWall m WHERE m.receiverId = :receiverId ORDER BY m.createdAt DESC")
    List<MessageWall> findByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId);
    
    /**
     * 获取用户未读的留言数量
     */
    @Query("SELECT COUNT(m) FROM MessageWall m WHERE m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadMessages(@Param("receiverId") Long receiverId);
    
    /**
     * 标记用户的所有留言为已读
     */
    @Query("UPDATE MessageWall m SET m.isRead = true WHERE m.receiverId = :receiverId")
    void markAllAsRead(@Param("receiverId") Long receiverId);
} 