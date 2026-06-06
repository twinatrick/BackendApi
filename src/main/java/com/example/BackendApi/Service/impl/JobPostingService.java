package com.example.BackendApi.Service.impl;

import com.example.BackendApi.Crawler.impl.CompositeJobCrawler;
import com.example.BackendApi.DataAccess.ICompanyDataAccess;
import com.example.BackendApi.DataAccess.IJobPostingDataAccess;
import com.example.BackendApi.Dto.Vo.CreateJobPostingRequest;
import com.example.BackendApi.Dto.Vo.JobPostingVo;
import com.example.BackendApi.Entity.Company;
import com.example.BackendApi.Entity.JobPosting;
import com.example.BackendApi.Mapper.JobPostingMapper;
import com.example.BackendApi.Service.IGeminiService;
import com.example.BackendApi.Service.IJobPostingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingService implements IJobPostingService {

    private final IJobPostingDataAccess jobPostingDataAccess;
    private final ICompanyDataAccess companyDataAccess;
    private final JobPostingMapper jobPostingMapper;
    private final CompositeJobCrawler jobCrawler;
    private final IGeminiService geminiService;

    @Override
    @Transactional
    @CacheEvict(value = "jobPostings", allEntries = true)
    public JobPostingVo createJobPosting(CreateJobPostingRequest request) {
        Company company = companyDataAccess.findById(UUID.fromString(request.getCompanyId()))
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        JobPosting jobPosting = new JobPosting();
        jobPosting.setCompany(company);
        jobPosting.setTitle(request.getTitle());
        jobPosting.setUrl(request.getUrl());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setRequirements(request.getRequirements());
        jobPosting.setResponsibilities(request.getResponsibilities());
        jobPosting.setSalaryRange(request.getSalaryRange());
        jobPosting.setPostedDate(request.getPostedDate());
        jobPosting = jobPostingDataAccess.save(jobPosting);
        return jobPostingMapper.toVo(jobPosting);
    }

    @Override
    @Cacheable(value = "jobPostings", unless = "#result == null || #result.isEmpty()")
    public List<JobPostingVo> getAllJobPostings() {
        return jobPostingDataAccess.findAll().stream()
                .map(jobPostingMapper::toVo)
                .toList();
    }

    @Override
    @Cacheable(value = "jobPostings", key = "#id", unless = "#result == null")
    public JobPostingVo getJobPostingById(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        JobPosting jobPosting = jobPostingDataAccess.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));
        return jobPostingMapper.toVo(jobPosting);
    }

    @Override
    @Transactional
    @CacheEvict(value = "jobPostings", allEntries = true)
    public JobPostingVo updateJobPosting(JobPostingVo jobPostingVo) {
        if (jobPostingVo.getId() == null || jobPostingVo.getId().isBlank()) {
            throw new IllegalArgumentException("ID must not be null");
        }
        JobPosting jobPosting = jobPostingDataAccess.findById(UUID.fromString(jobPostingVo.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));

        if (jobPostingVo.getCompanyId() != null) {
            Company company = companyDataAccess.findById(UUID.fromString(jobPostingVo.getCompanyId()))
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            jobPosting.setCompany(company);
        }
        if (jobPostingVo.getTitle() != null) {
            jobPosting.setTitle(jobPostingVo.getTitle());
        }
        if (jobPostingVo.getUrl() != null) {
            jobPosting.setUrl(jobPostingVo.getUrl());
        }
        if (jobPostingVo.getDescription() != null) {
            jobPosting.setDescription(jobPostingVo.getDescription());
        }
        if (jobPostingVo.getRequirements() != null) {
            jobPosting.setRequirements(jobPostingVo.getRequirements());
        }
        if (jobPostingVo.getResponsibilities() != null) {
            jobPosting.setResponsibilities(jobPostingVo.getResponsibilities());
        }
        if (jobPostingVo.getSalaryRange() != null) {
            jobPosting.setSalaryRange(jobPostingVo.getSalaryRange());
        }
        if (jobPostingVo.getPostedDate() != null) {
            jobPosting.setPostedDate(jobPostingVo.getPostedDate());
        }
        if (jobPostingVo.getGeminiAnalysis() != null) {
            jobPosting.setGeminiAnalysis(jobPostingVo.getGeminiAnalysis());
        }

        jobPosting = jobPostingDataAccess.save(jobPosting);
        return jobPostingMapper.toVo(jobPosting);
    }

    @Override
    @Transactional
    @CacheEvict(value = "jobPostings", allEntries = true)
    public void deleteJobPosting(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        if (!jobPostingDataAccess.existsById(uuid)) {
            throw new IllegalArgumentException("Job posting not found");
        }
        jobPostingDataAccess.deleteById(uuid);
    }

    @Override
    @Cacheable(value = "jobPostings", key = "'bycompany:' + #companyId", unless = "#result == null || #result.isEmpty()")
    public List<JobPostingVo> getJobPostingsByCompanyId(String companyId) {
        UUID uuid = mapUuid(companyId);
        if (uuid == null) {
            throw new IllegalArgumentException("Company ID must not be null");
        }
        return jobPostingDataAccess.findByCompanyId(uuid).stream()
                .map(jobPostingMapper::toVo)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobPostings", allEntries = true),
        @CacheEvict(value = "companies", allEntries = true)
    })
    public List<JobPostingVo> scrapeAndAnalyzeJobs(String companyId) {
        UUID uuid = mapUuid(companyId);
        if (uuid == null) {
            throw new IllegalArgumentException("Company ID must not be null");
        }

        Company company = companyDataAccess.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        log.info("Starting job scraping for company: {} ({})", company.getName(), company.getWebsite());

        // Step 1: Crawl the company website
        String htmlContent = jobCrawler.crawl(company.getWebsite());

        // Step 2: Send to Gemini for analysis
        List<Map<String, String>> analyzedJobs = geminiService.analyzeJobPostings(company.getName(), htmlContent);

        log.info("Gemini analysis returned {} job postings for company: {}", analyzedJobs.size(), company.getName());

        // Step 3: Save analyzed jobs to database
        List<JobPostingVo> savedJobs = new ArrayList<>();
        for (Map<String, String> jobData : analyzedJobs) {
            try {
                JobPosting jobPosting = new JobPosting();
                jobPosting.setCompany(company);
                jobPosting.setTitle(jobData.getOrDefault("title", "Unknown Title"));
                jobPosting.setUrl(jobData.getOrDefault("url", ""));
                jobPosting.setDescription(jobData.getOrDefault("description", ""));
                jobPosting.setRequirements(jobData.getOrDefault("requirements", ""));
                jobPosting.setResponsibilities(jobData.getOrDefault("responsibilities", ""));
                jobPosting.setSalaryRange(jobData.getOrDefault("salaryRange", ""));

                jobPosting = jobPostingDataAccess.save(jobPosting);
                savedJobs.add(jobPostingMapper.toVo(jobPosting));
            } catch (Exception e) {
                log.error("Failed to save job posting for company: {}", company.getName(), e);
            }
        }

        log.info("Successfully saved {} job postings for company: {}", savedJobs.size(), company.getName());
        return savedJobs;
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
