package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DynamicLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicLikeRepository extends JpaRepository<DynamicLike, Long> {
    
    /**
     * 检查用户是否已点赞
     */
    boolean existsByDynamicIdAndUserId(Long dynamicId, Long userId);
    
    /**
     * 获取动态的点赞数
     */
    Long countByDynamicId(Long dynamicId);
    
    /**
     * 删除点赞记录
     */
    void deleteByDynamicIdAndUserId(Long dynamicId, Long userId);
    
    /**
     * 获取用户点赞的动态ID列表
     */
    List<DynamicLike> findByUserId(Long userId);
} 