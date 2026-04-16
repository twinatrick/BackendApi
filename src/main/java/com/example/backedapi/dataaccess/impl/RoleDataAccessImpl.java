package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.RoleRepository;
import com.example.backedapi.dataaccess.IRoleDataAccess;
import com.example.backedapi.Enity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of IRoleDataAccess.
 * Delegates to Spring Data JPA RoleRepository.
 */
@Component
@RequiredArgsConstructor
public class RoleDataAccessImpl implements IRoleDataAccess {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> findRoleByIdIn(List<UUID> ids) {
        return roleRepository.findRoleByIdIn(ids);
    }

    @Override
    public boolean exists(Example<Role> example) {
        return roleRepository.exists(example);
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> findById(UUID key) {
        return roleRepository.findById(key);
    }

    @Override
    public void delete(Role role) {
        roleRepository.delete(role);
    }

    @Override
    public List<Role> findAllById(List<UUID> keys) {
        return roleRepository.findAllById(keys);
    }

    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }
}
