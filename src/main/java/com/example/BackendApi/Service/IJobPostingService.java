package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.JobPostingVo;

import java.util.List;

public interface IJobPostingService {

    JobPostingVo createJobPosting(JobPostingVo jobPostingVo);

    List<JobPostingVo> getAllJobPostings();

    JobPostingVo getJobPostingById(String id);

    JobPostingVo updateJobPosting(JobPostingVo jobPostingVo);

    void deleteJobPosting(String id);

    List<JobPostingVo> getJobPostingsByCompanyId(String companyId);

    List<JobPostingVo> scrapeAndAnalyzeJobs(String companyId);
}
