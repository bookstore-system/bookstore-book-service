package com.notfound.bookservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.notfound.bookservice.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty or null");
            }
            return uploadImageBytes(file.getBytes(), folder);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> uploadImageBytes(byte[] bytes, String folder) {
        try {
            if (bytes == null || bytes.length == 0) {
                throw new IllegalArgumentException("Image bytes are empty");
            }
            String targetFolder =
                    (folder == null || folder.isBlank()) ? "bookstore/books" : folder.trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "folder", targetFolder,
                            "resource_type", "image",
                            "overwrite", true,
                            "invalidate", true));
            log.info("Image uploaded to {}: {}", targetFolder, uploadResult.get("url"));
            return uploadResult;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (files == null) {
            return results;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                results.add(uploadImage(file, folder));
            }
        }
        return results;
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank()) {
                return false;
            }
            String publicId = extractPublicIdFromUrl(imageUrl);
            @SuppressWarnings("unchecked")
            Map<String, Object> result =
                    (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            log.warn("Failed to delete image {}: {}", imageUrl, e.getMessage());
            return false;
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (!imageUrl.startsWith("http")) {
            return imageUrl;
        }
        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2) {
            return imageUrl;
        }
        String[] pathParts = parts[1].split("/");
        StringBuilder publicId = new StringBuilder();
        for (int i = 1; i < pathParts.length; i++) {
            if (i > 1) {
                publicId.append("/");
            }
            publicId.append(pathParts[i]);
        }
        return publicId.toString().replaceAll("\\.[^.]+$", "");
    }
}
