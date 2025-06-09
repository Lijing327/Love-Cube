package com.lovecube.backend.controllers;

import com.lovecube.backend.services.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/greetings")
public class GreetingController
{

    @Autowired
    private GreetingService greetingService;

    @PostMapping("/greet")
    public ResponseEntity<?> sendGreeting(@RequestParam Long senderId, @RequestParam Long receiverId)
    {
        String message = greetingService.sendGreeting(senderId, receiverId);
        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }
}
