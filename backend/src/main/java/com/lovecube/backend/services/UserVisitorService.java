package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserVisitor;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserVisitorRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.entity.UserInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserVisitorService {
    
    @Autowired
    private UserVisitorRepository visitorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInteractionRepository interactionRepository;
    
    /**
     * 记录访客访问
     */
    public UserVisitor recordVisit(Long visitorUserId, Long visitedUserId, 
                                 UserVisitor.VisitType visitType, 
                                 UserVisitor.VisitSource visitSource, 
                                 String ipAddress, String deviceInfo) {
        // 不记录自己访问自己
        if (visitorUserId.equals(visitedUserId)) {
            return null;
        }
        
        // 检查是否为首次访问
        boolean isFirstVisit = !visitorRepository.existsByVisitorUserIdAndVisitedUserId(visitorUserId, visitedUserId);
        
        UserVisitor visitor = new UserVisitor();
        visitor.setVisitorUserId(visitorUserId);
        visitor.setVisitedUserId(visitedUserId);
        visitor.setVisitType(visitType);
        visitor.setVisitSource(visitSource);
        visitor.setIsNewVisitor(isFirstVisit);
        visitor.setIpAddress(ipAddress);
        visitor.setDeviceInfo(deviceInfo);
        
        return visitorRepository.save(visitor);
    }
    
    /**
     * 更新访问时长
     */
    public void updateVisitDuration(Long visitorId, Integer durationSeconds) {
        visitorRepository.findById(visitorId).ifPresent(visitor -> {
            visitor.setDurationSeconds(durationSeconds);
            visitorRepository.save(visitor);
        });
    }
    
    /**
     * 标记所有访客记录为已读
     */
    public void markAllVisitorsAsRead(Long userId) {
        visitorRepository.markAllAsRead(userId);
    }
    
    /**
     * 获取用户的访客列表
     */
    public List<Map<String, Object>> getVisitorList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UserVisitor> visitors = visitorRepository.findUniqueVisitorsByVisitedUserId(userId);
        
        // 手动分页
        int start = page * size;
        int end = Math.min(start + size, visitors.size());
        if (start >= visitors.size()) {
            return List.of();
        }
        
        List<UserVisitor> pagedVisitors = visitors.subList(start, end);
        return pagedVisitors.stream().map(this::convertToVisitorDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取今日访客数量
     */
    public Long getTodayVisitorCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return visitorRepository.countTodayVisitors(userId, startOfDay);
    }
    
    /**
     * 获取总访客数量
     */
    public Long getTotalVisitorCount(Long userId) {
        return visitorRepository.countByVisitedUserId(userId);
    }
    
    /**
     * 获取新访客数量
     */
    public Long getNewVisitorCount(Long userId) {
        return visitorRepository.countByVisitedUserIdAndIsNewVisitorTrue(userId);
    }
    
    /**
     * 获取用户的访问次数
     */
    public Long getUserVisitCount(Long visitorUserId, Long visitedUserId) {
        return visitorRepository.countByVisitorUserIdAndVisitedUserId(visitorUserId, visitedUserId);
    }
    
    /**
     * 获取平均访问时长
     */
    public Double getAverageVisitDuration(Long userId) {
        return visitorRepository.getAverageVisitDuration(userId);
    }
    
    /**
     * 清理过期的访客记录（超过30天）
     */
    public void cleanupOldVisitorRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        visitorRepository.deleteOldVisitorRecords(cutoffDate);
    }
    
    /**
     * 获取访客统计信息
     */
    public Map<String, Object> getVisitorStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("todayVisitors", getTodayVisitorCount(userId));
        stats.put("totalVisitors", getTotalVisitorCount(userId));
        stats.put("newVisitors", getNewVisitorCount(userId));
        stats.put("averageDuration", getAverageVisitDuration(userId));
        
        return stats;
    }
    
    /**
     * 检查两个用户是否互相关注/互相点赞
     */
    private boolean checkMutualInteraction(Long userId1, Long userId2, UserInteraction.InteractionType type) {
        boolean user1ToUser2 = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            userId1, userId2, type);
        boolean user2ToUser1 = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            userId2, userId1, type);
        
        return user1ToUser2 && user2ToUser1;
    }
    
    /**
     * 转换为前端需要的DTO格式
     */
    private Map<String, Object> convertToVisitorDTO(UserVisitor visitor) {
        Map<String, Object> dto = new HashMap<>();
        
        // 获取访客用户信息
        User visitorUser = userRepository.findById(visitor.getVisitorUserId())
                .orElse(null);
        
        dto.put("id", visitor.getId());
        dto.put("visitTime", visitor.getCreatedAt());
        dto.put("visitType", visitor.getVisitType().name().toLowerCase());
        dto.put("visitSource", visitor.getVisitSource() != null ? visitor.getVisitSource().name().toLowerCase() : null);
        dto.put("isNewVisitor", visitor.getIsNewVisitor());
        
        if (visitorUser != null) {
            dto.put("userId", visitorUser.getUserid());
            dto.put("nickname", visitorUser.getUsername() != null ? visitorUser.getUsername() : "匿名用户");
            dto.put("avatar", visitorUser.getProfilePhoto() != null ? visitorUser.getProfilePhoto() : "/images/default-avatar.png");
            dto.put("age", visitorUser.getAge());
            dto.put("location", visitorUser.getLocation());
            
            // 生成用户标签
            dto.put("tags", generateUserTags(visitorUser));
        }
        
        // 获取访问次数
        Long visitCount = getUserVisitCount(visitor.getVisitorUserId(), visitor.getVisitedUserId());
        dto.put("visitCount", visitCount);
        
        // 检查是否互相关注（匹配）
        boolean isMatch = checkMutualInteraction(visitor.getVisitorUserId(), visitor.getVisitedUserId(), 
                                               UserInteraction.InteractionType.FOLLOW) ||
                         checkMutualInteraction(visitor.getVisitorUserId(), visitor.getVisitedUserId(), 
                                               UserInteraction.InteractionType.LIKE);
        dto.put("isMatch", isMatch);
        
        // 访问时长信息
        if (visitor.getDurationSeconds() != null) {
            dto.put("duration", formatDuration(visitor.getDurationSeconds()));
        }
        
        return dto;
    }
    
    /**
     * 生成用户标签
     */
    private List<String> generateUserTags(User user) {
        List<String> tags = new java.util.ArrayList<>();
        
        if (user.getAge() != null) {
            if (user.getAge() <= 25) {
                tags.add("年轻");
            } else if (user.getAge() <= 35) {
                tags.add("成熟");
            }
        }
        
        if (user.getLocation() != null) {
            tags.add(user.getLocation());
        }
        
        if (user.getOccupation() != null) {
            tags.add(user.getOccupation());
        }
        
        // 根据注册时间添加标签
        if (user.getCreatedAt() != null) {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            if (user.getCreatedAt().isAfter(oneWeekAgo)) {
                tags.add("新用户");
            }
        }
        
        return tags;
    }
    
    /**
     * 格式化访问时长
     */
    private String formatDuration(Integer durationSeconds) {
        if (durationSeconds < 60) {
            return durationSeconds + "秒";
        } else if (durationSeconds < 3600) {
            return (durationSeconds / 60) + "分钟";
        } else {
            return (durationSeconds / 3600) + "小时";
        }
    }
} 