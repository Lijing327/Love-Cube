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

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        logger.info("测试接口被调用");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "测试成功");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/test-log")
    public String logTest() {
        System.out.println(">>> test-log 被访问");
        return "ok";
    }
    @GetMapping("/list")
    public ResponseEntity<?> getMatchUsers(
            @RequestParam(required = true) Long userId,
            @RequestParam(required = false, defaultValue = "18") Integer minAge,
            @RequestParam(required = false, defaultValue = "35") Integer maxAge,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) String location) {
        try {
            logger.info("开始获取匹配列表 - userId: {}, minAge: {}, maxAge: {}, gender: {}, location: {}", 
                userId, minAge, maxAge, gender, location);
            
            List<User> matches = matchService.findMatches(userId, minAge, maxAge, gender, location);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", matches);
            
            logger.info("成功获取匹配列表 - 匹配数量: {}", matches.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取匹配列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取匹配列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
