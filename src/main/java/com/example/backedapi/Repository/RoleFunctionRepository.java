package com.example.backedapi.Repository;

import com.example.backedapi.Enity.Function;
import com.example.backedapi.Enity.Role;
import com.example.backedapi.Enity.RoleFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleFunctionRepository extends JpaRepository<RoleFunction, UUID> {
    @Modifying
    @Query("delete from RoleFunction s where s.function.id = ?1")
    void deleteByFunction(UUID key);
    @Modifying
    @Query("delete from RoleFunction s where s.role.id = ?1")
    void deleteByRoleKey(UUID key);
    @Modifying
    @Query("delete from RoleFunction s where s.function in ?1 and s.role in ?2")
    void deleteByFunctionAndRole(List<Function> function, List<Role> role);

    void  deleteAllByFunctionIn(Collection<Function> function);
}
