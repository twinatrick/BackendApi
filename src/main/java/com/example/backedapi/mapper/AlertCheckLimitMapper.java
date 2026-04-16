package com.example.backedapi.mapper;

import com.example.backedapi.Dto.Vo.AlertCheckLimitVo;
import com.example.backedapi.Enity.AlertCheckLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlertCheckLimitMapper {
    AlertCheckLimitVo toVo(AlertCheckLimit alertCheckLimit);

    AlertCheckLimit toEntity(AlertCheckLimitVo alertCheckLimitVo);
}
