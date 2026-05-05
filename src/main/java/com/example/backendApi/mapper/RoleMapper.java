package com.example.backendApi.mapper;

import com.example.backendApi.Dto.Vo.RoleOutVo;
import com.example.backendApi.Enity.Role;
import com.example.backendApi.Enity.RoleFunction;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {FunctionMapper.class})
public interface RoleMapper {
    RoleOutVo toVo(Role role);

    Role toEntity(RoleOutVo roleOutVo);

    @AfterMapping
    default void fillFunctionIds(Role role, @MappingTarget RoleOutVo vo) {
        if (role == null) {
            return;
        }
        List<String> ids = role.getRoleFunctions().stream()
                .map(RoleFunction::getFunction)
                .map(function -> function.getId())
                .map(UUID::toString)
                .toList();
        vo.setFunctionIds(ids);
    }
}
