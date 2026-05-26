package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 10_000)
    private String description;

    /** UUID thể loại cha; chuỗi rỗng để xóa parent. */
    private String parentCategoryId;
}
