package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Dynamic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicRepository extends JpaRepository<Dynamic, Long> {
    
    /**
     * 获取未删除的动态列表（分页）
     */
    Page<Dynamic> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 获取指定用户的动态列表
     */
    Page<Dynamic> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 获取动态详情
     */
    Dynamic findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 获取用户动态数量
     */
    Long countByUserIdAndIsDeletedFalse(Long userId);
    
    /**
     * 获取热门动态（按点赞数排序）
     */
    @Query("SELECT d FROM Dynamic d WHERE d.isDeleted = false ORDER BY d.likeCount DESC, d.createdAt DESC")
    Page<Dynamic> findHotDynamics(Pageable pageable);
} 