package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class HomeService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<Map<String, Object>> getRecommends() {
        List<User> users = userRepository.findRandomUsers(10);
        return users.stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewcomers() {
        List<User> users = userRepository.findNewcomers(10);
        return users.stream()
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }

    public List<User> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(PageRequest.of(page, size))
                .getContent();
        }
        return userRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .filter(user -> 
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword.toLowerCase())) ||
                    (user.getLocation() != null && user.getLocation().toLowerCase().contains(keyword.toLowerCase())) ||
                    (user.getOccupation() != null && user.getOccupation().toLowerCase().contains(keyword.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    public List<User> filterUsers(UserFilterDTO filterDTO) {
        return userRepository.findByAgeBetweenAndGenderAndLocation(
            filterDTO.getAgeRange().get(0),
            filterDTO.getAgeRange().get(1),
            "男".equals(filterDTO.getGender()) ? 1 : 2,
            filterDTO.getRegion()
        );
    }

    // 将User实体转换为Map，处理photos字段
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("userid", user.getUserid());
        result.put("username", user.getUsername());
        result.put("profilePhoto", user.getProfilePhoto());
        result.put("avatar", user.getProfilePhoto()); // 前端可能使用avatar字段
        result.put("gender", user.getGender());
        result.put("location", user.getLocation());
        result.put("occupation", user.getOccupation());
        result.put("bio", user.getBio());
        result.put("height", user.getHeight());
        
        // 计算年龄
        if (user.getBirthDate() != null) {
            int age = Period.between(user.getBirthDate().toLocalDate(), java.time.LocalDate.now()).getYears();
            result.put("age", age);
        }
        
        // 处理生活照片 - 将JSON字符串解析为数组
        List<String> photosList = parsePhotosJson(user.getPhotos());
        result.put("photos", photosList);
        result.put("lifePhotos", photosList); // 兼容前端可能使用的其他字段名
        
        return result;
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