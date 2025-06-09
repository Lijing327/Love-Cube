package com.lovecube.backend.controllers;

import com.lovecube.backend.dto.UserSimilarityDTO;
import com.lovecube.backend.exception.UserNotFoundException;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);

        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        Integer gender = user.getGender();
        String genderStr;
        result.put("nickname", user.getUsername());
        if (gender == null) {
            genderStr = "未知";
        } else if (gender == 1) {
            genderStr = "男";
        } else if (gender == 2) {
            genderStr = "女";
        } else {
            genderStr = "其他";
        }
        result.put("gender", genderStr);

        result.put("location", user.getLocation());
        result.put("profilePhoto", user.getProfile_photo());

        // 使用 java.util.Date 计算年龄
        result.put("age", user.getAge());

        return ResponseEntity.ok(result);
    }

    // Date -> 年龄
    private int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserInfo(@RequestHeader("Authorization") String authHeader,
                                                   @RequestBody Map<String, Object> updates) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);

        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        // 更新字段（需根据前端传的字段名而定）
        if (updates.containsKey("nickname")) {
            user.setUsername((String) updates.get("nickname"));
        }
        if (updates.containsKey("gender")) {
            String genderStr = (String) updates.get("gender");
            if ("男".equals(genderStr)) {
                user.setGender(1);
            } else if ("女".equals(genderStr)) {
                user.setGender(2);
            } else {
                user.setGender(3);
            }
        }
        if (updates.containsKey("age")) {
            Object ageVal = updates.get("age");
            if (ageVal instanceof Integer) {
                user.setAge((Integer) ageVal);
            } else if (ageVal instanceof String) {
                user.setAge(Integer.parseInt((String) ageVal));
            }
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("profilePhoto")) {
            user.setProfile_photo((String) updates.get("profilePhoto"));
        }

        userRepository.save(user);
        return ResponseEntity.ok("资料更新成功");
    }




}
