package com.example.BackendApi.Dto.Vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "公司名稱不得為空")
    private String name;

    @NotBlank(message = "公司網址不得為空")
    private String website;

    private String description;
}
