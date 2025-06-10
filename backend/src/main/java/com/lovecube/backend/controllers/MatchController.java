package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匹配控制器
 */
@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MatchController {
    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;

    @GetMapping("/list")
    public ResponseEntity<?> getMatchUsers(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = false, defaultValue = "18") Integer minAge,
            @RequestParam(required = false, defaultValue = "35") Integer maxAge,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String location) {
        
        logger.info("接收到匹配请求 - userId: {}, minAge: {}, maxAge: {}, gender: {}, location: {}", 
                   userId, minAge, maxAge, gender, location);

        Map<String, Object> response = new HashMap<>();
        
        try {
            // 参数验证
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("无效的用户ID");
            }

            if (minAge != null && maxAge != null && minAge > maxAge) {
                throw new IllegalArgumentException("最小年龄不能大于最大年龄");
            }

            // 调用服务层
            List<User> users = matchService.findMatchUsers(userId, minAge, maxAge, gender, location);
            
            // 处理返回数据，移除敏感信息
            List<Map<String, Object>> userDTOs = users.stream()
                .map(user -> {
                    Map<String, Object> userDTO = new HashMap<>();
                    userDTO.put("userid", user.getUserid());
                    userDTO.put("username", user.getUsername());
                    userDTO.put("age", user.getAge());
                    userDTO.put("gender", user.getGender());
                    userDTO.put("location", user.getLocation());
                    userDTO.put("occupation", user.getOccupation());
                    userDTO.put("profilePhoto", user.getProfilePhoto());
                    userDTO.put("bio", user.getBio());
                    return userDTO;
                })
                .collect(java.util.stream.Collectors.toList());
            
            response.put("success", true);
            response.put("data", userDTOs);
            response.put("total", userDTOs.size());
            
            logger.info("匹配成功 - 找到 {} 个匹配用户", userDTOs.size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("匹配请求参数错误: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "参数错误");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("匹配过程发生错误", e);
            response.put("success", false);
            response.put("error", "服务器错误");
            response.put("message", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 用于测试的端点
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        logger.info("收到连接测试请求");
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "匹配服务正常运行");
            response.put("timestamp", System.currentTimeMillis());
            logger.info("连接测试成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("连接测试失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "服务异常：" + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
