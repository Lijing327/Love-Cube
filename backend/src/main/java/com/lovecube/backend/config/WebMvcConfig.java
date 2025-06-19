package com.lovecube.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // ✅ 推荐使用 allowedOriginPatterns，支持通配符
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
        System.out.println("✅ 跨域配置已启用");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        String avatarPath = uploadPath + "avatar/";
        String photosPath = uploadPath + "photos/";

        new File(uploadPath).mkdirs();
        new File(avatarPath).mkdirs();
        new File(photosPath).mkdirs();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        registry.addResourceHandler("/admin/uploads/**")
                .addResourceLocations("file:" + uploadPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        System.out.println("✅ 静态资源目录配置成功！上传目录：" + uploadPath);
        System.out.println("✅ 照片目录：" + photosPath);
    }
}
