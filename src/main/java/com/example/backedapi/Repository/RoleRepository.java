package com.example.backedapi.Repository;

import com.example.backedapi.model.db.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    @Query("select r from Role r where r.id in ?1")
    List<Role> findRoleByIdIn(List<UUID> ids);

    Role findRoleByName(String name);
}
