package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.CompanyVo;
import com.example.BackendApi.Entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", expression = "java(company.getId() == null ? null : company.getId().toString())")
    CompanyVo toVo(Company company);

    @Mapping(target = "id", expression = "java(companyVo.getId() == null || companyVo.getId().isBlank() ? null : java.util.UUID.fromString(companyVo.getId()))")
    Company toEntity(CompanyVo companyVo);
}
