package com.lovecube.backend.controllers;

import com.lovecube.backend.config.AppProperties;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController
{
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private UserRepository userRepository;
    
    private  final String AVATAR_DIR = "uploads/avatar/"; // 存储目录

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + ext;

            String savePath = System.getProperty("user.dir") + "/uploads/avatar/";
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, filename);
            file.transferTo(dest);

            String avatarUrl = appProperties.getBaseUrl() + appProperties.getAvatarPath() + filename;

            Map<String, Object> result = new HashMap<>();
            result.put("url", avatarUrl);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authHeader,
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

        if (updates.containsKey("nickname")) {
            user.setUsername((String) updates.get("nickname"));
        }
        if (updates.containsKey("gender")) {
            String genderStr = (String) updates.get("gender");
            if ("男".equals(genderStr)) user.setGender(1);
            else if ("女".equals(genderStr)) user.setGender(2);
            else user.setGender(0);
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("age")) {
            Object ageObj = updates.get("age");
            if (ageObj instanceof Integer) {
                user.setAge((Integer) ageObj);
            } else if (ageObj instanceof String) {
                user.setAge(Integer.parseInt((String) ageObj));
            }
        }
        if (updates.containsKey("profilePhoto")) {
            user.setProfile_photo((String) updates.get("profilePhoto"));
        }

        userRepository.save(user);
        return ResponseEntity.ok("资料更新成功");
    }

}
