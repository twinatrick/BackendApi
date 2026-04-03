package com.example.backedapi.dataaccess;

import com.example.backedapi.model.db.Role;
import com.example.backedapi.model.db.User;
import com.example.backedapi.model.db.UserRole;

import java.util.List;

/**
 * Data access interface for UserRole entity operations.
 * Abstracts UserRoleRepository operations for service layer.
 * Note: UserRole operations are primarily handled via RoleService,
 * this interface is minimal for UserService needs.
 */
public interface IUserRoleDataAccess {

    /**
     * Delete all user-role associations for given users and roles.
     *
     * @param users list of users
     * @param roles list of roles
     */
    void deleteAllByUserInAndRoleIn(List<User> users, List<Role> roles);

    /**
     * Save all user-role associations.
     *
     * @param userRoles list of user-role associations to save
     * @return list of saved user-role associations
     */
    List<UserRole> saveAll(List<UserRole> userRoles);
}
