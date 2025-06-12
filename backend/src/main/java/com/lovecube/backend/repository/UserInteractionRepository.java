package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    
    /**
     * 获取用户收到的所有互动（按时间倒序）
     */
    List<UserInteraction> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    
    /**
     * 获取用户收到的所有互动（分页）
     */
    List<UserInteraction> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);
    
    /**
     * 获取用户收到的未读互动数量
     */
    Long countByToUserIdAndIsReadFalse(Long toUserId);
    
    /**
     * 获取用户收到的未读互动
     */
    List<UserInteraction> findByToUserIdAndIsReadFalseOrderByCreatedAtDesc(Long toUserId);
    
    /**
     * 获取特定类型的互动
     */
    List<UserInteraction> findByToUserIdAndInteractionTypeOrderByCreatedAtDesc(
        Long toUserId, UserInteraction.InteractionType interactionType);
    
    /**
     * 获取两个用户之间的特定互动
     */
    Optional<UserInteraction> findByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 检查用户是否已经对目标进行了某种互动
     */
    boolean existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 检查用户是否关注了另一个用户
     */
    boolean existsByFromUserIdAndToUserIdAndInteractionType(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType);
    
    /**
     * 获取用户的关注列表
     */
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.fromUserId = :userId AND ui.interactionType = 'FOLLOW'")
    List<UserInteraction> findFollowingList(@Param("userId") Long userId);
    
    /**
     * 获取用户的粉丝列表
     */
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.toUserId = :userId AND ui.interactionType = 'FOLLOW'")
    List<UserInteraction> findFollowersList(@Param("userId") Long userId);
    
    /**
     * 获取今日收到的互动数量
     */
    @Query("SELECT COUNT(ui) FROM UserInteraction ui WHERE ui.toUserId = :userId AND ui.createdAt >= :startOfDay")
    Long countTodayInteractions(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * 获取用户收到的点赞数量
     */
    Long countByToUserIdAndInteractionType(Long toUserId, UserInteraction.InteractionType interactionType);
    
    /**
     * 批量标记互动为已读
     */
    @Query("UPDATE UserInteraction ui SET ui.isRead = true WHERE ui.toUserId = :userId AND ui.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 删除特定互动（如取消点赞、取消关注）
     */
    void deleteByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 删除关注记录
     */
    void deleteByFromUserIdAndToUserIdAndInteractionType(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType);
} 