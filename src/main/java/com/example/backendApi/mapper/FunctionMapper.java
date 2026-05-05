package com.example.backendApi.mapper;

import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Enity.Function;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface FunctionMapper {
    @Mapping(target = "id", expression = "java(mapId(function.getId()))")
    FunctionVo toVo(Function function);

    @Mapping(target = "id", expression = "java(mapUuid(functionVo.getId()))")
    Function toEntity(FunctionVo functionVo);

    default String mapId(UUID id) {
        return id == null ? null : id.toString();
    }

    default UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }

    @AfterMapping
    default void fillDerived(Function function, @MappingTarget FunctionVo vo) {
        if (function == null) {
            return;
        }
        vo.setParentName(function.getParent());
        vo.setGrandParentId("");
        vo.setDisabled(false);
        vo.setEdit(false);
        vo.setNewAdd(false);
        vo.setNewName(function.getName());
        vo.setDelete(false);
    }
}
