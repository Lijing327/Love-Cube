package com.lovecube.backend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file) throws IOException;
    
    /**
     * 上传生活照片
     */
    String uploadPhoto(MultipartFile file) throws IOException;
    
    /**
     * 删除文件
     */
    boolean deleteFile(String fileUrl);
} 