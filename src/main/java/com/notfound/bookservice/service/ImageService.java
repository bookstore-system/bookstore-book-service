package com.notfound.bookservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService {

    Map<String, Object> uploadImage(MultipartFile file, String folder);

    Map<String, Object> uploadImageBytes(byte[] bytes, String folder);

    List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder);

    boolean deleteImage(String imageUrl);
}
