package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserStatistics;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Admin
 * 我们实现推荐逻辑，包括获取当前用户、计算相似度、排序并返回前N个推荐用户。
 */
@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    public Map<String, Object> getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserid());
        result.put("username", user.getUsername());
        result.put("gender", convertGender(user.getGender()));
        result.put("location", user.getLocation());
        result.put("profilePhoto", user.getProfilePhoto());
        result.put("age", user.getAge());
        result.put("occupation", user.getOccupation());
        result.put("bio", user.getBio());
        result.put("height", user.getHeight());
        
        // 添加星座信息
        if (user.getBirthDate() != null) {
            result.put("constellation", getConstellation(user.getBirthDate()));
        } else {
            result.put("constellation", "未知");
        }

        // 获取用户统计信息
        UserStatistics stats = userStatisticsRepository.findByUserId(user.getUserid());
        if (stats != null) {
            result.put("statistics", stats);
        }

        return result;
    }

    private String convertGender(Integer gender) {
        if (gender == null) return "未设置";
        return gender == 1 ? "男" : "女";
    }

    private String getConstellation(LocalDateTime birthDate) {
        if (birthDate == null) return "未知";
        
        int month = birthDate.getMonthValue();
        int day = birthDate.getDayOfMonth();
        
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";
        
        return "未知";
    }

    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getUserid())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户信息
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getGender() != null) existingUser.setGender(user.getGender());
        if (user.getLocation() != null) existingUser.setLocation(user.getLocation());
        if (user.getOccupation() != null) existingUser.setOccupation(user.getOccupation());
        if (user.getHeight() != null) existingUser.setHeight(user.getHeight());
        if (user.getProfilePhoto() != null) existingUser.setProfilePhoto(user.getProfilePhoto());
        if (user.getBio() != null) existingUser.setBio(user.getBio());
        if (user.getAge() != null) existingUser.setAge(user.getAge());

        return userRepository.save(existingUser);
    }

    public User getCurrentUser(String token) {
        // 从token中获取用户ID
        String userId = extractUserIdFromToken(token);
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String extractUserIdFromToken(String token) {
        // 移除 "Bearer " 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 实现从token中解析用户ID的逻辑
        // 这里暂时返回一个测试用的用户ID
        return "1";
    }
}
