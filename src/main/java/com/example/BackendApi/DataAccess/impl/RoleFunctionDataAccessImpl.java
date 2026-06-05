package com.example.BackendApi.DataAccess.impl;

import com.example.BackendApi.Repository.RoleFunctionRepository;
import com.example.BackendApi.DataAccess.IRoleFunctionDataAccess;
import com.example.BackendApi.Entity.Function;
import com.example.BackendApi.Entity.Role;
import com.example.BackendApi.Entity.RoleFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of IRoleFunctionDataAccess.
 * Delegates to Spring Data JPA RoleFunctionRepository.
 */
@Component
@RequiredArgsConstructor
public class RoleFunctionDataAccessImpl implements IRoleFunctionDataAccess {

    private final RoleFunctionRepository roleFunctionRepository;

    @Override
    public List<RoleFunction> findAll(Example<RoleFunction> example) {
        return roleFunctionRepository.findAll(example);
    }

    @Override
    public void deleteAll(List<RoleFunction> roleFunctions) {
        roleFunctionRepository.deleteAll(roleFunctions);
    }

    @Override
    public void deleteByFunctionAndRole(List<Function> functions, List<Role> roles) {
        roleFunctionRepository.deleteByFunctionAndRole(functions, roles);
    }

    @Override
    public List<RoleFunction> saveAll(List<RoleFunction> roleFunctions) {
        return roleFunctionRepository.saveAll(roleFunctions);
    }

    @Override
    public void deleteByFunction(UUID functionId) {
        roleFunctionRepository.deleteByFunction(functionId);
    }

    @Override
    public void deleteByRoleKey(UUID roleId) {
        roleFunctionRepository.deleteByRoleKey(roleId);
    }

    @Override
    public void deleteAllByFunctionIn(Collection<Function> functions) {
        roleFunctionRepository.deleteAllByFunctionIn(functions);
    }
}
