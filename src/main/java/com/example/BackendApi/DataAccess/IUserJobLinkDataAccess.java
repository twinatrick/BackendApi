package com.example.BackendApi.DataAccess;

import com.example.BackendApi.Entity.UserJobLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserJobLinkDataAccess {

    UserJobLink save(UserJobLink userJobLink);

    List<UserJobLink> findAll();

    Optional<UserJobLink> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    List<UserJobLink> findByUserId(UUID userId);

    List<UserJobLink> findByJobPostingId(UUID jobPostingId);
}
