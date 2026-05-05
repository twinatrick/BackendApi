package com.example.backendApi.mapper;

import com.example.backendApi.Dto.Vo.AlertCheckLimitVo;
import com.example.backendApi.Entity.AlertCheckLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlertCheckLimitMapper {
    AlertCheckLimitVo toVo(AlertCheckLimit alertCheckLimit);

    AlertCheckLimit toEntity(AlertCheckLimitVo alertCheckLimitVo);
}
