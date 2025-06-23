package com.lovecube.backend.controllers;

import com.lovecube.backend.services.WeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wechat")
public class LoginController {

    @Autowired
    private WeChatService weChatService;

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String code)
    {
        Map<String, Object> response = weChatService.login(code);
        return ResponseEntity.ok(response);
    }

}
