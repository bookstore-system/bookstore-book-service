package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadAvatarRequest {
    /** URL ảnh hoặc chuỗi base64 (data:image/...;base64,...). */
    @NotBlank
    private String image;
}
