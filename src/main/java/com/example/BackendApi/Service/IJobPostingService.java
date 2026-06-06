package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.CreateJobPostingRequest;
import com.example.BackendApi.Dto.Vo.JobPostingVo;

import java.util.List;

public interface IJobPostingService {

    JobPostingVo createJobPosting(CreateJobPostingRequest request);

    List<JobPostingVo> getAllJobPostings();

    JobPostingVo getJobPostingById(String id);

    JobPostingVo updateJobPosting(JobPostingVo jobPostingVo);

    void deleteJobPosting(String id);

    List<JobPostingVo> getJobPostingsByCompanyId(String companyId);

    List<JobPostingVo> scrapeAndAnalyzeJobs(String companyId);

    void scrapeAndAnalyzeAllCompanies();
}
