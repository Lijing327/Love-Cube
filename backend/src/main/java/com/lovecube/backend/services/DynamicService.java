package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.entity.DynamicLike;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DynamicLikeRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DynamicService {
    
    @Autowired
    private DynamicRepository dynamicRepository;
    
    @Autowired
    private DynamicLikeRepository dynamicLikeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取动态列表
     */
    public Map<String, Object> getDynamicList(int pageNum, int pageSize, Long currentUserId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Dynamic> dynamicPage = dynamicRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        
        List<Dynamic> dynamics = dynamicPage.getContent().stream()
            .map(dynamic -> enrichDynamicInfo(dynamic, currentUserId))
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", dynamics);
        result.put("total", dynamicPage.getTotalElements());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("hasNext", dynamicPage.hasNext());
        
        return result;
    }
    
    /**
     * 发布动态
     */
    @Transactional
    public Dynamic publishDynamic(Long userId, String content, List<String> imageUrls) {
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(userId);
        dynamic.setContent(content);
        
        // 将图片URL列表转换为JSON字符串存储
        if (imageUrls != null && !imageUrls.isEmpty()) {
            try {
                dynamic.setImageUrls(objectMapper.writeValueAsString(imageUrls));
            } catch (Exception e) {
                throw new RuntimeException("图片URL序列化失败", e);
            }
        }
        
        return dynamicRepository.save(dynamic);
    }
    
    /**
     * 点赞/取消点赞动态
     */
    @Transactional
    public Map<String, Object> toggleLike(Long dynamicId, Long userId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalse(dynamicId);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        
        boolean isLiked = dynamicLikeRepository.existsByDynamicIdAndUserId(dynamicId, userId);
        
        if (isLiked) {
            // 取消点赞
            dynamicLikeRepository.deleteByDynamicIdAndUserId(dynamicId, userId);
            dynamic.setLikeCount(Math.max(0, dynamic.getLikeCount() - 1));
        } else {
            // 点赞
            DynamicLike like = new DynamicLike();
            like.setDynamicId(dynamicId);
            like.setUserId(userId);
            dynamicLikeRepository.save(like);
            dynamic.setLikeCount(dynamic.getLikeCount() + 1);
        }
        
        dynamicRepository.save(dynamic);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", !isLiked);
        result.put("likeCount", dynamic.getLikeCount());
        
        return result;
    }
    
    /**
     * 删除动态
     */
    @Transactional
    public void deleteDynamic(Long dynamicId, Long userId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalse(dynamicId);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        
        if (!dynamic.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此动态");
        }
        
        dynamic.setIsDeleted(true);
        dynamicRepository.save(dynamic);
    }
    
    /**
     * 获取动态详情
     */
    public Dynamic getDynamicDetail(Long dynamicId, Long currentUserId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalse(dynamicId);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        
        return enrichDynamicInfo(dynamic, currentUserId);
    }
    
    /**
     * 丰富动态信息（添加用户信息、点赞状态等）
     */
    private Dynamic enrichDynamicInfo(Dynamic dynamic, Long currentUserId) {
        // 获取用户信息
        User user = userRepository.findById(dynamic.getUserId()).orElse(null);
        if (user != null) {
            dynamic.setUsername(user.getUsername());
            dynamic.setUserAvatar(user.getProfilePhoto());
        }
        
        // 检查当前用户是否已点赞
        if (currentUserId != null) {
            boolean isLiked = dynamicLikeRepository.existsByDynamicIdAndUserId(dynamic.getId(), currentUserId);
            dynamic.setIsLiked(isLiked);
        }
        
        // 解析图片URL列表
        if (dynamic.getImageUrls() != null && !dynamic.getImageUrls().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(dynamic.getImageUrls(), new TypeReference<List<String>>() {});
                dynamic.setImages(images);
            } catch (Exception e) {
                // 解析失败时设置为空列表
                dynamic.setImages(List.of());
            }
        }
        
        return dynamic;
    }
} 