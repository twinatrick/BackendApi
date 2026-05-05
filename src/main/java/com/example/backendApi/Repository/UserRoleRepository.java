package com.example.backendApi.Repository;

import com.example.backendApi.Enity.Role;
import com.example.backendApi.Enity.User;
import com.example.backendApi.Enity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
//    @Modifying
//    @Query("delete from UserRole ur where ur.user IN ?1 and ur.role IN ?2")
//    void deleteByUserAndRole(List<UUID> userKey, List<UUID> roleKey);
//    @Modifying
//    @Query("delete from UserRole ur where ur.user IN ?1 and ur.role IN ?2")
//
    void deleteAllByUserInAndRoleIn(List<User> userKey, List<Role> roleKey);
}
