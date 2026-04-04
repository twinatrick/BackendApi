package com.example.backedapi.Repository;

import com.example.backedapi.Enity.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FunctionRepository extends JpaRepository<Function, UUID> {


    @Query("select f from Function f where f.parent IN ?1")
    List<Function> findAllByGrandParentId(List<String> grandParentId);

    @Query("select f from Function f where f.parent IN ?1")
    List<Function> getParent(List<String> parent);

    Function findFunctionByName(String name);

    @Query("select f from Function f where f.name = ?1 and f.parent = ?2")
    List<Function> findFunctionByNameAndParent(String name, String parent);
}
