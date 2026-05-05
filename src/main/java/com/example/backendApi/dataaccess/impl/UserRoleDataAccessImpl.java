package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.UserRoleRepository;
import com.example.backendApi.dataaccess.IUserRoleDataAccess;
import com.example.backendApi.Enity.Role;
import com.example.backendApi.Enity.User;
import com.example.backendApi.Enity.UserRole;
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
