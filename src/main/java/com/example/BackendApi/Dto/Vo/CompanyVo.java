package com.example.BackendApi.Dto.Vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CompanyVo {
    private String id;
    private String name;
    private String website;
    private String description;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;
}
