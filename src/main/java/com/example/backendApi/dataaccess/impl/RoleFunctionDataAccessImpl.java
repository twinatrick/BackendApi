package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.RoleFunctionRepository;
import com.example.backendApi.dataaccess.IRoleFunctionDataAccess;
import com.example.backendApi.Enity.Function;
import com.example.backendApi.Enity.Role;
import com.example.backendApi.Enity.RoleFunction;
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
    public void deleteAllByFunctionIn(Collection<Function> functions) {
        roleFunctionRepository.deleteAllByFunctionIn(functions);
    }
}
