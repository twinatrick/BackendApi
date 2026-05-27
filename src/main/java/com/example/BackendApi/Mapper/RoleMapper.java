package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.RoleOutVo;
import com.example.BackendApi.Entity.BaseEntity;
import com.example.BackendApi.Entity.Role;
import com.example.BackendApi.Entity.RoleFunction;
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
                .map(BaseEntity::getId)
                .map(UUID::toString)
                .toList();
        vo.setFunctionIds(ids);
    }
}
