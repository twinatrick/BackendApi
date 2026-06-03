package com.example.BackendApi.Repository;

import com.example.BackendApi.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    List<Project> findByName(String name);

    @Modifying
    @Query("delete from Project p where p.id = :projectId")
    void deleteByIdHard(@Param("projectId") UUID projectId);
}
