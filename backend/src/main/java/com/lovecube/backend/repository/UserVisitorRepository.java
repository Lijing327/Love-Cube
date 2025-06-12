package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserVisitor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVisitorRepository extends JpaRepository<UserVisitor, Long> {
    
    /**
     * 获取用户的所有访客（按时间倒序）
     */
    List<UserVisitor> findByVisitedUserIdOrderByCreatedAtDesc(Long visitedUserId);
    
    /**
     * 获取用户的所有访客（分页）
     */
    List<UserVisitor> findByVisitedUserIdOrderByCreatedAtDesc(Long visitedUserId, Pageable pageable);
    
    /**
     * 获取今日访客数量
     */
    @Query("SELECT COUNT(uv) FROM UserVisitor uv WHERE uv.visitedUserId = :userId AND uv.createdAt >= :startOfDay")
    Long countTodayVisitors(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * 获取总访客数量
     */
    Long countByVisitedUserId(Long visitedUserId);
    
    /**
     * 获取新访客数量（首次访问）
     */
    Long countByVisitedUserIdAndIsNewVisitorTrue(Long visitedUserId);
    
    /**
     * 获取特定访客的访问记录
     */
    List<UserVisitor> findByVisitorUserIdAndVisitedUserIdOrderByCreatedAtDesc(
        Long visitorUserId, Long visitedUserId);
    
    /**
     * 检查是否为首次访问
     */
    boolean existsByVisitorUserIdAndVisitedUserId(Long visitorUserId, Long visitedUserId);
    
    /**
     * 获取访客的访问次数
     */
    Long countByVisitorUserIdAndVisitedUserId(Long visitorUserId, Long visitedUserId);
    
    /**
     * 获取最近的访问记录
     */
    Optional<UserVisitor> findFirstByVisitorUserIdAndVisitedUserIdOrderByCreatedAtDesc(
        Long visitorUserId, Long visitedUserId);
    
    /**
     * 获取用户访问别人的记录
     */
    List<UserVisitor> findByVisitorUserIdOrderByCreatedAtDesc(Long visitorUserId);
    
    /**
     * 获取不重复的访客列表（每个访客只显示最新一次访问）
     */
    @Query("SELECT uv FROM UserVisitor uv WHERE uv.visitedUserId = :userId " +
           "AND uv.createdAt = (SELECT MAX(uv2.createdAt) FROM UserVisitor uv2 " +
           "WHERE uv2.visitorUserId = uv.visitorUserId AND uv2.visitedUserId = :userId) " +
           "ORDER BY uv.createdAt DESC")
    List<UserVisitor> findUniqueVisitorsByVisitedUserId(@Param("userId") Long userId);
    
    /**
     * 获取指定时间范围内的访客
     */
    @Query("SELECT uv FROM UserVisitor uv WHERE uv.visitedUserId = :userId " +
           "AND uv.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY uv.createdAt DESC")
    List<UserVisitor> findVisitorsByTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取访问时长统计
     */
    @Query("SELECT AVG(uv.durationSeconds) FROM UserVisitor uv WHERE uv.visitedUserId = :userId " +
           "AND uv.durationSeconds IS NOT NULL")
    Double getAverageVisitDuration(@Param("userId") Long userId);
    
    /**
     * 删除过期的访客记录（超过指定天数）
     */
    @Query("DELETE FROM UserVisitor uv WHERE uv.createdAt < :cutoffDate")
    void deleteOldVisitorRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
} 