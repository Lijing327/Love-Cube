package com.lovecube.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl;
    private String avatarPath;
    private String photosPath;
    
    // OSS相关配置
    private Oss oss = new Oss();
    
    @Data
    public static class Oss {
        private String accessKeyId;
        private String accessKeySecret;
        private String endpoint;
        private String bucketName;
        private String cdnDomain; // CDN域名
        private String avatarFolder = "avatars/"; // 头像文件夹
        private String photosFolder = "photos/"; // 生活照文件夹
        private boolean enabled = false; // 是否启用OSS
    }
}
