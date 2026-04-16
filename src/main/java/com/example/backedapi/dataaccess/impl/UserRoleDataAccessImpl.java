package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.UserRoleRepository;
import com.example.backedapi.dataaccess.IUserRoleDataAccess;
import com.example.backedapi.Enity.Role;
import com.example.backedapi.Enity.User;
import com.example.backedapi.Enity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of IUserRoleDataAccess.
 * Delegates to Spring Data JPA UserRoleRepository.
 */
@Component
@RequiredArgsConstructor
public class UserRoleDataAccessImpl implements IUserRoleDataAccess {

    private final UserRoleRepository userRoleRepository;

    @Override
    public void deleteAllByUserInAndRoleIn(List<User> users, List<Role> roles) {
        userRoleRepository.deleteAllByUserInAndRoleIn(users, roles);
    }

    @Override
    public List<UserRole> saveAll(List<UserRole> userRoles) {
        return userRoleRepository.saveAll(userRoles);
    }
}
