package com.lovecube.backend.controllers;

import com.lovecube.backend.config.AppProperties;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/upload")
public class UploadController
{
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String AVATAR_DIR = "uploads/avatar/";
    private final String PHOTOS_DIR = "uploads/photos/";

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + ext;

            String savePath = System.getProperty("user.dir") + "/uploads/avatar/";
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, filename);
            file.transferTo(dest);

            String avatarUrl = appProperties.getBaseUrl() + appProperties.getAvatarPath() + filename;

            Map<String, Object> result = new HashMap<>();
            result.put("url", avatarUrl);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, Object> updates) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }
        
        User user = userRepository.findByOpenid(openid);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (updates.containsKey("nickname")) {
            user.setUsername((String) updates.get("nickname"));
        }
        if (updates.containsKey("gender")) {
            String genderStr = (String) updates.get("gender");
            if ("男".equals(genderStr)) user.setGender(1);
            else if ("女".equals(genderStr)) user.setGender(2);
            else user.setGender(0);
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("age")) {
            Object ageObj = updates.get("age");
            if (ageObj instanceof Integer) {
                user.setAge((Integer) ageObj);
            } else if (ageObj instanceof String) {
                user.setAge(Integer.parseInt((String) ageObj));
            }
        }
        if (updates.containsKey("profilePhoto")) {
            user.setProfilePhoto((String) updates.get("profilePhoto"));
        }

        userRepository.save(user);
        return ResponseEntity.ok("资料更新成功");
    }

    /**
     * 上传生活照片
     */
    @PostMapping("/photos")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 验证文件类型
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传jpg、jpeg、png、gif格式的图片");
            }
            
            String filename = UUID.randomUUID().toString() + ext;
            String savePath = System.getProperty("user.dir") + "/" + PHOTOS_DIR;
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, filename);
            file.transferTo(dest);

            String photoUrl = appProperties.getBaseUrl() + appProperties.getPhotosPath() + filename;

            Map<String, Object> result = new HashMap<>();
            result.put("url", photoUrl);
            result.put("message", "照片上传成功");
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 批量上传生活照片
     */
    @PostMapping("/photos/batch")
    public ResponseEntity<?> uploadPhotos(@RequestParam("files") MultipartFile[] files,
                                         @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("没有选择文件");
        }

        if (files.length > 9) {
            return ResponseEntity.badRequest().body("最多只能上传9张照片");
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                
                if (!isValidImageExtension(ext)) {
                    failedFiles.add(originalFilename + " (不支持的文件类型)");
                    continue;
                }
                
                String filename = UUID.randomUUID().toString() + ext;
                String savePath = System.getProperty("user.dir") + "/" + PHOTOS_DIR;
                File dir = new File(savePath);
                if (!dir.exists()) dir.mkdirs();

                File dest = new File(dir, filename);
                file.transferTo(dest);

                String photoUrl = appProperties.getBaseUrl() + appProperties.getPhotosPath() + filename;
                uploadedUrls.add(photoUrl);

            } catch (IOException e) {
                failedFiles.add(file.getOriginalFilename() + " (上传失败: " + e.getMessage() + ")");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadedUrls", uploadedUrls);
        result.put("uploadedCount", uploadedUrls.size());
        result.put("failedFiles", failedFiles);
        result.put("failedCount", failedFiles.size());
        
        if (uploadedUrls.size() > 0) {
            result.put("message", "成功上传 " + uploadedUrls.size() + " 张照片");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 保存用户生活照片到用户资料
     */
    @PostMapping("/photos/save")
    public ResponseEntity<?> saveUserPhotos(@RequestBody Map<String, Object> request,
                                           @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) request.get("photos");
            
            if (photoUrls == null) {
                return ResponseEntity.badRequest().body("照片列表不能为空");
            }

            if (photoUrls.size() > 9) {
                return ResponseEntity.badRequest().body("最多只能保存9张照片");
            }

            // 验证所有URL都是本服务器的照片
            String baseUrl = appProperties.getBaseUrl() + appProperties.getPhotosPath();
            for (String url : photoUrls) {
                if (!url.startsWith(baseUrl)) {
                    return ResponseEntity.badRequest().body("包含无效的照片URL: " + url);
                }
            }

            String photosJson = objectMapper.writeValueAsString(photoUrls);
            user.setPhotos(photosJson);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "生活照片保存成功");
            result.put("photosCount", photoUrls.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除生活照片
     */
    @DeleteMapping("/photos")
    public ResponseEntity<?> deletePhoto(@RequestParam String photoUrl,
                                        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        try {
            // 获取用户当前的照片列表
            List<String> currentPhotos = parsePhotosJson(user.getPhotos());
            
            if (!currentPhotos.contains(photoUrl)) {
                return ResponseEntity.badRequest().body("照片不存在于用户资料中");
            }

            // 从列表中移除照片
            currentPhotos.remove(photoUrl);
            
            // 更新用户资料
            String photosJson = objectMapper.writeValueAsString(currentPhotos);
            user.setPhotos(photosJson);
            userRepository.save(user);

            // 删除物理文件
            try {
                String baseUrl = appProperties.getBaseUrl() + appProperties.getPhotosPath();
                if (photoUrl.startsWith(baseUrl)) {
                    String filename = photoUrl.substring(baseUrl.length());
                    String filePath = System.getProperty("user.dir") + "/" + PHOTOS_DIR + filename;
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                // 文件删除失败不影响数据库操作
                System.err.println("删除物理文件失败: " + e.getMessage());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "照片删除成功");
            result.put("remainingPhotosCount", currentPhotos.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取用户的生活照片列表
     */
    @GetMapping("/photos")
    public ResponseEntity<?> getUserPhotos(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        List<String> photos = parsePhotosJson(user.getPhotos());
        
        Map<String, Object> result = new HashMap<>();
        result.put("photos", photos);
        result.put("photosCount", photos.size());
        return ResponseEntity.ok(result);
    }

    // 工具方法
    private boolean isValidImageExtension(String extension) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF"};
        return Arrays.asList(validExtensions).contains(extension);
    }

    private List<String> parsePhotosJson(String photosJson) {
        if (photosJson == null || photosJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("解析照片JSON失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
