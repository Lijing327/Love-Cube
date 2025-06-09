package com.lovecube.backend.controllers;

import com.lovecube.backend.dto.UserSimilarityDTO;
import com.lovecube.backend.entity.UserStatistics;
import com.lovecube.backend.exception.UserNotFoundException;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserStatisticsRepository;
import com.lovecube.backend.services.UserService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

import static com.lovecube.backend.utils.JwtUtil.generateToken;

/*
我们创建一个REST端点，处理GET请求，接收用户ID作为参数，并调用 UserService 的方法返回推荐列表。
 */
@RestController
@RequestMapping("/api/users")
public class UserController
{
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    /*
    匹配推荐
     */
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable Long userId, @RequestParam(defaultValue = "5") int limit)
    {
        try {
            List<UserSimilarityDTO> recommendations = userService.getRecommendedUsers(userId, limit);
            return ResponseEntity.ok(recommendations);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /*
    注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user)
    {
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
    @GetMapping("/checkUserStatus")
    public ResponseEntity<Map<String, Object>> checkUserStatus(@RequestHeader("Authorization") String authHeader)
    {
        Map<String, Object> response = new HashMap<>();

        System.out.println("🔍 收到请求，完整 Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("❌ Token 格式错误！");
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 提取 Token
        String token = authHeader.substring(7);
        System.out.println("🔍 解析出的 Token: " + token);

        // 解析 token 获取 openid
        String openid = JwtUtil.getOpenIdFromToken(token);
        System.out.println("🔍 解析出的 openid: " + openid);

        if (openid == null) {
            System.err.println("❌ token 无效！");
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 查询用户是否注册
        boolean isRegistered = userService.isUserRegistered(openid);
        System.out.println("✅ 用户注册状态: " + isRegistered);

        response.put("registered", isRegistered);
        return ResponseEntity.ok(response);
    }

    /*
    查询当前用户信息
     */
    @GetMapping("/me")
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
        result.put("profilePhoto", user.getProfile_photo());
        result.put("age", user.getAge());
        result.put("occupation", user.getOccupation());
        result.put("bio", user.getBio());
        result.put("height", user.getHeight());
        result.put("birthday", user.getBirth_date() != null ? 
            new SimpleDateFormat("yyyy-MM-dd").format(user.getBirth_date()) : null);

        // 获取用户统计信息
        UserStatistics stats = userStatisticsRepository.findByUserId(user.getUserid());
        if (stats == null) {
            stats = new UserStatistics();
            stats.setUserId(user.getUserid());
            stats = userStatisticsRepository.save(stats);
        }

        // 检查是否需要重置每日统计
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (stats.getLastStatisticsReset() == null || stats.getLastStatisticsReset().isBefore(today)) {
            stats.setTodayVisitors(0);
            stats.setNewLikes(0);
            stats.setNewMatches(0);
            stats.setLastStatisticsReset(today);
            stats = userStatisticsRepository.save(stats);
        }

        result.put("statistics", stats);
        result.put("completionRate", calculateCompletionRate(user));

        return ResponseEntity.ok(result);
    }

    private String convertGender(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "其他";
        }
    }

    private int calculateCompletionRate(User user) {
        int totalFields = 8;
        int completedFields = 0;

        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) completedFields++;
        if (user.getProfile_photo() != null && !user.getProfile_photo().trim().isEmpty()) completedFields++;
        if (user.getGender() != null) completedFields++;
        if (user.getAge() != null && user.getAge() > 0) completedFields++;
        if (user.getBirth_date() != null) completedFields++;
        if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) completedFields++;
        if (user.getOccupation() != null && !user.getOccupation().trim().isEmpty()) completedFields++;
        if (user.getBio() != null && !user.getBio().trim().isEmpty()) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "未提供或格式错误的 token"));
        }

        // 这里可以添加token黑名单等逻辑

        return ResponseEntity.ok(Map.of("message", "退出成功"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody Map<String, Object> updates) {
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

        try {
            // 更新用户信息
            if (updates.containsKey("nickname")) {
                user.setUsername((String) updates.get("nickname"));
            }
            if (updates.containsKey("gender")) {
                user.setGender(convertGenderToInt((String) updates.get("gender")));
            }
            if (updates.containsKey("birthday")) {
                try {
                    Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse((String) updates.get("birthday"));
                    user.setBirth_date(birthDate);
                    // 计算年龄
                    Calendar birth = Calendar.getInstance();
                    birth.setTime(birthDate);
                    Calendar today = Calendar.getInstance();
                    int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    user.setAge(age);
                } catch (ParseException e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "生日格式错误"));
                }
            }
            if (updates.containsKey("location")) {
                user.setLocation((String) updates.get("location"));
            }
            if (updates.containsKey("occupation")) {
                user.setOccupation((String) updates.get("occupation"));
            }
            if (updates.containsKey("bio")) {
                user.setBio((String) updates.get("bio"));
            }
            if (updates.containsKey("height")) {
                Object heightVal = updates.get("height");
                if (heightVal instanceof Integer) {
                    user.setHeight((Integer) heightVal);
                } else if (heightVal instanceof String) {
                    user.setHeight(Integer.parseInt((String) heightVal));
                }
            }
            if (updates.containsKey("avatar")) {
                user.setProfile_photo((String) updates.get("avatar"));
            }

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "资料更新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "更新失败: " + e.getMessage()));
        }
    }

    private Integer convertGenderToInt(String gender) {
        switch (gender.trim()) {
            case "男": return 1;
            case "女": return 2;
            default: return 0;
        }
    }
}
