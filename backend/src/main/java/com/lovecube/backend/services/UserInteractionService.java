package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserInteraction;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
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
public class UserInteractionService {
    
    @Autowired
    private UserInteractionRepository interactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建互动记录
     */
    public UserInteraction createInteraction(Long fromUserId, Long toUserId, 
                                           UserInteraction.InteractionType type,
                                           UserInteraction.TargetType targetType, 
                                           Long targetId, String content) {
        // 检查是否已经存在相同的互动
        if (targetId != null) {
            boolean exists = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                fromUserId, toUserId, type, targetId);
            if (exists) {
                throw new RuntimeException("已经存在相同的互动记录");
            }
        } else {
            // 对于关注这种没有targetId的互动
            boolean exists = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
                fromUserId, toUserId, type);
            if (exists) {
                throw new RuntimeException("已经关注该用户");
            }
        }
        
        UserInteraction interaction = new UserInteraction();
        interaction.setFromUserId(fromUserId);
        interaction.setToUserId(toUserId);
        interaction.setInteractionType(type);
        interaction.setTargetType(targetType);
        interaction.setTargetId(targetId);
        interaction.setContent(content);
        
        return interactionRepository.save(interaction);
    }
    
    /**
     * 取消互动（如取消点赞、取消关注）
     */
    public void removeInteraction(Long fromUserId, Long toUserId, 
                                UserInteraction.InteractionType type, Long targetId) {
        if (targetId != null) {
            interactionRepository.deleteByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                fromUserId, toUserId, type, targetId);
        } else {
            interactionRepository.deleteByFromUserIdAndToUserIdAndInteractionType(
                fromUserId, toUserId, type);
        }
    }
    
    /**
     * 获取用户收到的互动列表
     */
    public List<Map<String, Object>> getInteractionList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UserInteraction> interactions = interactionRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return interactions.stream().map(this::convertToInteractionDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取未读互动数量
     */
    public Long getUnreadInteractionCount(Long userId) {
        return interactionRepository.countByToUserIdAndIsReadFalse(userId);
    }
    
    /**
     * 标记所有互动为已读
     */
    public void markAllInteractionsAsRead(Long userId) {
        interactionRepository.markAllAsRead(userId);
    }
    
    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long fromUserId, Long toUserId) {
        return interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW);
    }
    
    /**
     * 检查是否已点赞
     */
    public boolean isLiked(Long fromUserId, Long toUserId, Long targetId) {
        return interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
            fromUserId, toUserId, UserInteraction.InteractionType.LIKE, targetId);
    }
    
    /**
     * 获取用户的关注数
     */
    public Long getFollowingCount(Long userId) {
        return (long) interactionRepository.findFollowingList(userId).size();
    }
    
    /**
     * 获取用户的粉丝数
     */
    public Long getFollowersCount(Long userId) {
        return (long) interactionRepository.findFollowersList(userId).size();
    }
    
    /**
     * 获取用户收到的点赞数
     */
    public Long getLikesCount(Long userId) {
        return interactionRepository.countByToUserIdAndInteractionType(
            userId, UserInteraction.InteractionType.LIKE);
    }
    
    /**
     * 获取今日收到的互动数量
     */
    public Long getTodayInteractionCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return interactionRepository.countTodayInteractions(userId, startOfDay);
    }
    
    /**
     * 关注用户
     */
    public UserInteraction followUser(Long fromUserId, Long toUserId) {
        return createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW,
                               UserInteraction.TargetType.USER, null, null);
    }
    
    /**
     * 取消关注
     */
    public void unfollowUser(Long fromUserId, Long toUserId) {
        removeInteraction(fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW, null);
    }
    
    /**
     * 点赞用户资料
     */
    public UserInteraction likeUser(Long fromUserId, Long toUserId) {
        return createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.LIKE,
                               UserInteraction.TargetType.PROFILE, toUserId, null);
    }
    
    /**
     * 取消点赞
     */
    public void unlikeUser(Long fromUserId, Long toUserId) {
        removeInteraction(fromUserId, toUserId, UserInteraction.InteractionType.LIKE, toUserId);
    }
    
    /**
     * 超级点赞
     */
    public UserInteraction superLikeUser(Long fromUserId, Long toUserId) {
        return createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.SUPER_LIKE,
                               UserInteraction.TargetType.PROFILE, toUserId, null);
    }
    
    /**
     * 发送礼物
     */
    public UserInteraction sendGift(Long fromUserId, Long toUserId, Long giftId, Integer giftCount, String message) {
        UserInteraction interaction = createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.GIFT,
                                                      UserInteraction.TargetType.USER, null, message);
        interaction.setGiftId(giftId);
        interaction.setGiftCount(giftCount);
        return interactionRepository.save(interaction);
    }
    
    /**
     * 转换为前端需要的DTO格式
     */
    private Map<String, Object> convertToInteractionDTO(UserInteraction interaction) {
        Map<String, Object> dto = new HashMap<>();
        
        // 获取发起互动的用户信息
        User fromUser = userRepository.findById(interaction.getFromUserId())
                .orElse(null);
        
        dto.put("id", interaction.getId());
        dto.put("type", interaction.getInteractionType().name().toLowerCase());
        dto.put("action", getActionText(interaction.getInteractionType()));
        dto.put("time", interaction.getCreatedAt());
        dto.put("isRead", interaction.getIsRead());
        
        if (fromUser != null) {
            dto.put("userId", fromUser.getUserid());
            dto.put("nickname", fromUser.getUsername() != null ? fromUser.getUsername() : "匿名用户");
            dto.put("avatar", fromUser.getProfilePhoto() != null ? fromUser.getProfilePhoto() : "/images/default-avatar.png");
        }
        
        // 根据互动类型添加特定信息
        switch (interaction.getInteractionType()) {
            case LIKE:
                dto.put("target", "赞了你");
                break;
            case SUPER_LIKE:
                dto.put("target", "超级喜欢你");
                break;
            case FOLLOW:
                dto.put("target", "关注了你");
                break;
            case GIFT:
                dto.put("target", "给你送了礼物");
                if (interaction.getContent() != null) {
                    dto.put("message", interaction.getContent());
                }
                if (interaction.getGiftCount() != null) {
                    dto.put("giftCount", interaction.getGiftCount());
                }
                break;
            case COMMENT:
                dto.put("target", "评论了你");
                if (interaction.getContent() != null) {
                    dto.put("content", interaction.getContent());
                }
                break;
        }
        
        return dto;
    }
    
    /**
     * 获取互动类型的文本描述
     */
    private String getActionText(UserInteraction.InteractionType type) {
        switch (type) {
            case LIKE: return "点赞了你";
            case SUPER_LIKE: return "超级喜欢你";
            case FOLLOW: return "关注了你";
            case GIFT: return "送了礼物";
            case COMMENT: return "评论了你";
            default: return "与你互动";
        }
    }
} 