package com.example.BackendApi.DataAccess;

import com.example.BackendApi.Dto.Vo.Search.JobPostingSearchQuery;
import com.example.BackendApi.Entity.JobPosting;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IJobPostingDataAccess {

    JobPosting save(JobPosting jobPosting);

    List<JobPosting> findAll();

    Optional<JobPosting> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    List<JobPosting> findByCompanyId(UUID companyId);

    Page<JobPosting> searchJobPostings(JobPostingSearchQuery query);
}
