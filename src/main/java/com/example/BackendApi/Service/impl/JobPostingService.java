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

import java.time.LocalDate;
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

        if (company.getWebsites() == null || company.getWebsites().isEmpty()) {
            log.warn("Company {} has no websites to scrape", company.getName());
            return List.of();
        }

        log.info("Starting job scraping for company: {} ({} websites)", company.getName(), company.getWebsites().size());

        List<JobPostingVo> allSavedJobs = new ArrayList<>();

        for (var companyWebsite : company.getWebsites()) {
            String url = companyWebsite.getUrl();
            log.info("Crawling website: {}", url);

            String htmlContent;
            try {
                htmlContent = jobCrawler.crawl(url);
            } catch (Exception e) {
                log.warn("Failed to crawl URL {}: {}", url, e.getMessage());
                continue;
            }

            List<Map<String, String>> analyzedJobs = geminiService.analyzeJobPostings(company.getName(), htmlContent);
            log.info("Gemini returned {} jobs from URL: {}", analyzedJobs.size(), url);

            List<JobPosting> existingJobs = jobPostingDataAccess.findByCompanyId(uuid);

            for (Map<String, String> jobData : analyzedJobs) {
                try {
                    String title = jobData.getOrDefault("title", "Unknown Title");
                    String salaryRange = jobData.getOrDefault("salaryRange", "");

                    JobPosting matched = findMatch(existingJobs, title, salaryRange);
                    if (matched != null) {
                        boolean updated = updateIfChanged(matched, jobData);
                        if (updated) {
                            jobPostingDataAccess.save(matched);
                            allSavedJobs.add(jobPostingMapper.toVo(matched));
                            log.info("Updated job: {} for company: {}", title, company.getName());
                        }
                        continue;
                    }

                    JobPosting jobPosting = new JobPosting();
                    jobPosting.setCompany(company);
                    jobPosting.setTitle(title);
                    jobPosting.setUrl(jobData.getOrDefault("url", ""));
                    jobPosting.setDescription(jobData.getOrDefault("description", ""));
                    jobPosting.setRequirements(jobData.getOrDefault("requirements", ""));
                    jobPosting.setResponsibilities(jobData.getOrDefault("responsibilities", ""));
                    jobPosting.setSalaryRange(salaryRange);

                    jobPosting = jobPostingDataAccess.save(jobPosting);
                    allSavedJobs.add(jobPostingMapper.toVo(jobPosting));
                    log.info("Created new job: {} for company: {}", title, company.getName());
                } catch (Exception e) {
                    log.error("Failed to process job posting for company: {}", company.getName(), e);
                }
            }
        }

        company.setLastScrapedAt(LocalDate.now());
        companyDataAccess.save(company);

        log.info("Completed scraping for company: {}, total saved/updated: {}", company.getName(), allSavedJobs.size());
        return allSavedJobs;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobPostings", allEntries = true),
        @CacheEvict(value = "companies", allEntries = true)
    })
    public void scrapeAndAnalyzeAllCompanies() {
        List<Company> allCompanies = companyDataAccess.findAll();
        LocalDate today = LocalDate.now();

        for (Company company : allCompanies) {
            if (today.equals(company.getLastScrapedAt())) {
                log.info("Skip company {}: already scraped today", company.getName());
                continue;
            }
            try {
                scrapeAndAnalyzeJobs(company.getId().toString());
            } catch (Exception e) {
                log.error("Failed to scrape company: {}", company.getName(), e);
            }
        }
    }

    private JobPosting findMatch(List<JobPosting> existingJobs, String title, String salaryRange) {
        if (title == null || title.isBlank()) {
            return null;
        }
        for (JobPosting jp : existingJobs) {
            boolean titleMatch = title.equalsIgnoreCase(jp.getTitle());
            boolean salaryMatch = (salaryRange == null || salaryRange.isBlank())
                    || (jp.getSalaryRange() != null && jp.getSalaryRange().equals(salaryRange));
            if (titleMatch && salaryMatch) {
                return jp;
            }
        }
        return null;
    }

    private boolean updateIfChanged(JobPosting existing, Map<String, String> newData) {
        boolean changed = false;
        String newUrl = newData.getOrDefault("url", "");
        if (!newUrl.equals(existing.getUrl() != null ? existing.getUrl() : "")) {
            existing.setUrl(newUrl);
            changed = true;
        }
        String newDesc = newData.getOrDefault("description", "");
        if (!newDesc.equals(existing.getDescription() != null ? existing.getDescription() : "")) {
            existing.setDescription(newDesc);
            changed = true;
        }
        String newReq = newData.getOrDefault("requirements", "");
        if (!newReq.equals(existing.getRequirements() != null ? existing.getRequirements() : "")) {
            existing.setRequirements(newReq);
            changed = true;
        }
        String newResp = newData.getOrDefault("responsibilities", "");
        if (!newResp.equals(existing.getResponsibilities() != null ? existing.getResponsibilities() : "")) {
            existing.setResponsibilities(newResp);
            changed = true;
        }
        String newSalary = newData.getOrDefault("salaryRange", "");
        if (!newSalary.equals(existing.getSalaryRange() != null ? existing.getSalaryRange() : "")) {
            existing.setSalaryRange(newSalary);
            changed = true;
        }
        return changed;
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
