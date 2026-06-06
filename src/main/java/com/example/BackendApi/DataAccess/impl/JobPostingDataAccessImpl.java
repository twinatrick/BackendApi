package com.example.BackendApi.DataAccess.impl;

import com.example.BackendApi.DataAccess.IJobPostingDataAccess;
import com.example.BackendApi.Entity.JobPosting;
import com.example.BackendApi.Repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobPostingDataAccessImpl implements IJobPostingDataAccess {

    private final JobPostingRepository jobPostingRepository;

    @Override
    public JobPosting save(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    @Override
    public List<JobPosting> findAll() {
        return jobPostingRepository.findAll();
    }

    @Override
    public Optional<JobPosting> findById(UUID id) {
        return jobPostingRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jobPostingRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jobPostingRepository.deleteById(id);
    }

    @Override
    public List<JobPosting> findByCompanyId(UUID companyId) {
        return jobPostingRepository.findAll().stream()
                .filter(jp -> jp.getCompany().getId().equals(companyId))
                .toList();
    }
}
