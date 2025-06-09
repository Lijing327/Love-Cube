package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 匹配控制器
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController
{
    @Autowired
    private MatchService matchService;

    @GetMapping("/list")
    public ResponseEntity<?> getMatchUsers(@RequestParam Long userId,
                                           @RequestParam(required = false) Integer minAge,
                                           @RequestParam(required = false) Integer maxAge,
                                           @RequestParam(required = false) Integer gender,
                                           @RequestParam(required = false) String location) {
        // 传递给 Service 层
        List<User> users = matchService.findMatchUsers(userId, minAge, maxAge, gender, location);
        return ResponseEntity.ok(users);
    }


}
