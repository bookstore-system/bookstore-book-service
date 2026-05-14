package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuthorRequest {

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 255)
    private String name;

    @Size(max = 20_000)
    private String biography;

    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String nationality;
}
