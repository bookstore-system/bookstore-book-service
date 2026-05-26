package com.notfound.bookservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
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
public class AuthorRequest {

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(min = 2, max = 200, message = "Tên tác giả phải có độ dài từ 2 đến 200 ký tự")
    private String name;

    @Size(max = 5000, message = "Tiểu sử không được vượt quá 5000 ký tự")
    private String biography;

    @PastOrPresent(message = "Ngày sinh không được là ngày trong tương lai")
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Quốc tịch không được vượt quá 100 ký tự")
    private String nationality;
}
