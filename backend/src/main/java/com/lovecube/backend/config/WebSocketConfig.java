package com.lovecube.backend.config;


import com.lovecube.backend.websockets.ChatWebSocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public ChatWebSocket chatWebSocket() {
        return new ChatWebSocket(); // 手动注册
    }
}
