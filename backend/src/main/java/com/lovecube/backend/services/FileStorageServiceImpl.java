package com.lovecube.backend.services;

import com.lovecube.backend.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    @Autowired
    private AppProperties appProperties;
    
    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        if (appProperties.getOss().isEnabled()) {
            // TODO: 实现OSS上传（需要先添加OSS依赖）
            return uploadToLocal(file, "uploads/avatar/");
        } else {
            return uploadToLocal(file, "uploads/avatar/");
        }
    }
    
    @Override
    public String uploadPhoto(MultipartFile file) throws IOException {
        if (appProperties.getOss().isEnabled()) {
            // TODO: 实现OSS上传（需要先添加OSS依赖）
            return uploadToLocal(file, "uploads/photos/");
        } else {
            return uploadToLocal(file, "uploads/photos/");
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        if (appProperties.getOss().isEnabled()) {
            // TODO: 实现OSS删除
            return deleteFromLocal(fileUrl);
        } else {
            return deleteFromLocal(fileUrl);
        }
    }
    
    /**
     * 上传到本地文件系统
     */
    private String uploadToLocal(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + ext;
        
        String savePath = System.getProperty("user.dir") + "/" + folder;
        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();
        
        File dest = new File(dir, filename);
        file.transferTo(dest);
        
        return appProperties.getBaseUrl() + "/" + folder + filename;
    }
    
    /**
     * 从本地删除文件
     */
    private boolean deleteFromLocal(String fileUrl) {
        try {
            String baseUrl = appProperties.getBaseUrl();
            if (fileUrl.startsWith(baseUrl)) {
                String relativePath = fileUrl.substring(baseUrl.length());
                String filePath = System.getProperty("user.dir") + relativePath;
                File file = new File(filePath);
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("从本地删除文件失败: " + e.getMessage());
            return false;
        }
    }
} 