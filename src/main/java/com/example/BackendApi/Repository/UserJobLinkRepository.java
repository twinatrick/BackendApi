package com.example.BackendApi.Repository;

import com.example.BackendApi.Entity.UserJobLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserJobLinkRepository extends JpaRepository<UserJobLink, UUID> {
}
