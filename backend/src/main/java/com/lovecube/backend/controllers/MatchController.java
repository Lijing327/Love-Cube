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
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MatchController {
    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;


    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer gender) {
        try {
            logger.info("获取全部用户列表 - gender: {}", gender);

            Long currentUserId = matchService.getCurrentUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("无效的用户Token");
            }

            List<User> users = matchService.getAllUsers(currentUserId, gender);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filterMatches(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> filters) {
        try {
            logger.info("筛选匹配用户 - filters: {}", filters);

            Long currentUserId = matchService.getCurrentUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("无效的用户Token");
            }

            // 解析筛选条件
            Integer minAge = null;
            Integer maxAge = null;
            if (filters.containsKey("ageRange") && filters.get("ageRange") instanceof List) {
                List<Integer> ageRange = (List<Integer>) filters.get("ageRange");
                if (ageRange.size() == 2) {
                    minAge = ageRange.get(0);
                    maxAge = ageRange.get(1);
                }
            }

            Integer gender = filters.get("gender") != null ? 
                Integer.parseInt(filters.get("gender").toString()) : null;
            String location = (String) filters.get("region");

            List<User> filteredUsers = matchService.findMatches(
                currentUserId, minAge, maxAge, gender, location);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", filteredUsers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("筛选用户失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "筛选用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}
