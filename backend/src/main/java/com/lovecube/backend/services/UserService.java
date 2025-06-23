package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.UserStatistics;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserStatisticsRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        // 暂时注释掉height字段相关代码
        // result.put("height", user.getHeight());
        
        // 处理生活照片
        List<String> photosList = parsePhotosJson(user.getPhotos());
        result.put("photos", photosList);
        
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
        // 暂时注释掉height字段相关代码
        // if (user.getHeight() != null) existingUser.setHeight(user.getHeight());
        if (user.getProfilePhoto() != null) existingUser.setProfilePhoto(user.getProfilePhoto());
        if (user.getBio() != null) existingUser.setBio(user.getBio());
        if (user.getAge() != null) existingUser.setAge(user.getAge());

        return userRepository.save(existingUser);
    }

    public User getCurrentUser(String token) {
        String openid = JwtUtil.getOpenIdFromToken(token.replace("Bearer ", ""));
        return userRepository.findByOpenid(openid);
    }

    // 解析照片JSON字符串
    private List<String> parsePhotosJson(String photosJson) {
        if (photosJson == null || photosJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("解析照片JSON失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
