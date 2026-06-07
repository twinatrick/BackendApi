package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.CompanyVo;
import com.example.BackendApi.Entity.Company;
import com.example.BackendApi.Entity.CompanyWebsite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", expression = "java(company.getId() == null ? null : company.getId().toString())")
    @Mapping(target = "websites", source = "websites", qualifiedByName = "toUrlList")
    CompanyVo toVo(Company company);

    @Mapping(target = "id", expression = "java(companyVo.getId() == null || companyVo.getId().isBlank() ? null : java.util.UUID.fromString(companyVo.getId()))")
    @Mapping(target = "websites", ignore = true)
    Company toEntity(CompanyVo companyVo);

    @Named("toUrlList")
    default List<String> toUrlList(List<CompanyWebsite> websites) {
        if (websites == null) {
            return Collections.emptyList();
        }
        return websites.stream()
                .map(CompanyWebsite::getUrl)
                .toList();
    }
}
