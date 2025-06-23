package com.lovecube.backend.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.UserStatistics;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserStatisticsRepository;
import com.lovecube.backend.services.UserService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<?> updateUserProfile(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Map<String, Object> profileData) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            User user = userRepository.findByOpenid(openid);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            // 更新用户信息
            if (profileData.containsKey("nickname")) {
                user.setUsername((String) profileData.get("nickname"));
            }
            if (profileData.containsKey("gender")) {
                String gender = (String) profileData.get("gender");
                user.setGender("男".equals(gender) ? 1 : 2);
            }
            if (profileData.containsKey("birthday")) {
                String birthday = (String) profileData.get("birthday");
                if (birthday != null && !birthday.isEmpty()) {
                    try {
                        LocalDateTime birthDate = LocalDateTime.parse(birthday + "T00:00:00");
                        user.setBirthDate(birthDate);
                        // 计算年龄
                        user.setAge(calculateAge(birthDate));
                    } catch (Exception e) {
                        // 忽略日期解析错误
                    }
                }
            }
            if (profileData.containsKey("location")) {
                user.setLocation((String) profileData.get("location"));
            }
            if (profileData.containsKey("occupation")) {
                user.setOccupation((String) profileData.get("occupation"));
            }
            // 暂时注释掉height字段相关代码
            /*
            if (profileData.containsKey("height")) {
                try {
                    Object heightObj = profileData.get("height");
                    if (heightObj instanceof String) {
                        String heightStr = (String) heightObj;
                        if (heightStr.endsWith("cm")) {
                            heightStr = heightStr.replace("cm", "");
                        }
                        user.setHeight(Integer.parseInt(heightStr));
                    } else if (heightObj instanceof Number) {
                        user.setHeight(((Number) heightObj).intValue());
                    }
                } catch (NumberFormatException e) {
                    // 忽略身高解析错误
                }
            }
            */
            if (profileData.containsKey("signature")) {
                user.setBio((String) profileData.get("signature"));
            }
            if (profileData.containsKey("avatar")) {
                user.setProfilePhoto((String) profileData.get("avatar"));
            }

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "资料更新成功"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "更新失败: " + e.getMessage()));
        }
    }

    @GetMapping("/users/current")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getCurrentUser(token));
    }

    /*
    注册
     */
    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 确保 openid 存在
        if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少 openid");
        }

        // 先检查数据库是否已有该 openid
        User existingUser = userRepository.findByOpenid(user.getOpenid());
        if (existingUser != null) {
            return ResponseEntity.badRequest().body("用户已注册，直接进入");
        }

        // 存储新用户
        User savedUser = userRepository.save(user);
        String token = JwtUtil.generateToken(savedUser.getOpenid());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getUserid());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    /*
    用户注册状态
     */
    @GetMapping("/users/checkUserStatus")
    public ResponseEntity<Map<String, Object>> checkUserStatus(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 提取 Token
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);

        if (openid == null) {
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 查询用户是否注册
        boolean isRegistered = userRepository.existsByOpenid(openid);
        response.put("registered", isRegistered);
        return ResponseEntity.ok(response);
    }

    /*
    查询当前用户信息
     */
    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUserInfo(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "未提供或格式错误的 token"));
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);

        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "token 无效"));
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserid());
        result.put("nickname", user.getUsername());
        result.put("gender", convertGender(user.getGender()));
        result.put("location", user.getLocation());
        result.put("profilePhoto", user.getProfilePhoto());
        
        // 格式化生日字段，确保前端能正确获取
        if (user.getBirthDate() != null) {
            String birthdayStr = user.getBirthDate().toLocalDate().toString();
            result.put("birthday", birthdayStr);
            result.put("birthDate", user.getBirthDate()); // 保留原字段以兼容
            result.put("age", calculateAge(user.getBirthDate()));
            result.put("constellation", getConstellation(user.getBirthDate())); // 添加星座计算
        } else {
            result.put("birthday", "");
            result.put("birthDate", null);
            result.put("constellation", "未知");
        }
        
        result.put("occupation", user.getOccupation());
        result.put("signature", user.getBio()); // 前端期望的是 signature 字段
        result.put("bio", user.getBio()); // 保留原字段以兼容
        // 暂时注释掉height字段相关代码
        /*
        result.put("height", user.getHeight());
        */
        
        // 处理生活照片
        List<String> photosList = parsePhotosJson(user.getPhotos());
        result.put("photos", photosList);

        // 获取用户统计信息
        UserStatistics stats = userStatisticsRepository.findByUserId(user.getUserid());
        if (stats == null) {
            stats = new UserStatistics();
            stats.setUserId(user.getUserid());
            stats = userStatisticsRepository.save(stats);
        }

        result.put("statistics", stats);
        result.put("completionRate", calculateCompletionRate(user));

        return ResponseEntity.ok(result);
    }

    private String convertGender(Integer gender) {
        if (gender == null) return null;
        return gender == 1 ? "男" : "女";
    }

    private int calculateCompletionRate(User user) {
        int totalFields = 7;
        int completedFields = 0;

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) completedFields++;
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().trim().isEmpty()) completedFields++;
        if (user.getGender() != null) completedFields++;
        if (user.getBirthDate() != null) completedFields++;
        if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) completedFields++;
        if (user.getOccupation() != null && !user.getOccupation().trim().isEmpty()) completedFields++;
        if (user.getBio() != null && !user.getBio().trim().isEmpty()) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    private int calculateAge(LocalDateTime birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate.toLocalDate(), java.time.LocalDate.now()).getYears();
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
