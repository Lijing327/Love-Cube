package com.lovecube.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 创建上传目录
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        String avatarPath = uploadPath + "avatar/";
        
        // 确保目录存在
        new File(uploadPath).mkdirs();
        new File(avatarPath).mkdirs();
        
        // 配置静态资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
                
        registry.addResourceHandler("/admin/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
                
        System.out.println("✅ 静态资源目录配置成功！上传目录：" + uploadPath);
    }
}
