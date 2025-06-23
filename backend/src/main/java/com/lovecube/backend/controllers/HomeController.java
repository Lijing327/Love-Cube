package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.services.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(homeService.getBanners());
    }

    @GetMapping("/recommends")
    public ResponseEntity<List<Map<String, Object>>> getRecommends() {
        return ResponseEntity.ok(homeService.getRecommends());
    }

    @GetMapping("/newcomers")
    public ResponseEntity<List<Map<String, Object>>> getNewcomers() {
        return ResponseEntity.ok(homeService.getNewcomers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(homeService.searchUsers(keyword, page, size));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<User>> filterUsers(@RequestBody UserFilterDTO filterDTO) {
        return ResponseEntity.ok(homeService.filterUsers(filterDTO));
    }
} 