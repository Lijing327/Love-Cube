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
        if ("test".equals(code)) {
            Map<String, Object> mock = new HashMap<>();
            mock.put("status", "MOCK_SUCCESS");
            mock.put("message", "接口联调成功！");
            return ResponseEntity.ok(mock);
        }

        Map<String, Object> response = weChatService.login(code);
        return ResponseEntity.ok(response);
    }

}
