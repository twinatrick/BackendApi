package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.AlertCheckLimitVo;
import com.example.BackendApi.Entity.AlertCheckLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlertCheckLimitMapper {
    AlertCheckLimitVo toVo(AlertCheckLimit alertCheckLimit);

    AlertCheckLimit toEntity(AlertCheckLimitVo alertCheckLimitVo);
}
