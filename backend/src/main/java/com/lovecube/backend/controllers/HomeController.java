package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.services.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(homeService.getBanners());
    }

    @GetMapping("/recommends")
    public ResponseEntity<List<UserProfile>> getRecommends() {
        return ResponseEntity.ok(homeService.getRecommends());
    }

    @GetMapping("/newcomers")
    public ResponseEntity<List<UserProfile>> getNewcomers() {
        return ResponseEntity.ok(homeService.getNewcomers());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(homeService.searchUsers(keyword, page, size));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<UserProfile>> filterUsers(@RequestBody UserFilterDTO filterDTO) {
        return ResponseEntity.ok(homeService.filterUsers(filterDTO));
    }
} 