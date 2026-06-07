package com.example.BackendApi.Controller;

import com.example.BackendApi.Annotation.RequirePermission;
import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationOk;
import com.example.BackendApi.Dto.Vo.Common.PageResult;
import com.example.BackendApi.Dto.Vo.CreateJobPostingRequest;
import com.example.BackendApi.Dto.Vo.JobPostingVo;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.Search.JobPostingSearchQuery;
import com.example.BackendApi.Service.IJobPostingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/job-posting")
@ApiControllerTag(name = "Job Posting", description = "Backend API endpoints - Job posting management")
public class JobPostingController {

    private final IJobPostingService jobPostingService;

    public JobPostingController(IJobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @PostMapping("/add")
    @RequirePermission({"System", "JobPosting", "Edit"})
    @ApiOperationBadRequest(summary = "新增職缺", description = "手動新增一筆職缺。")
    public ResponseType<JobPostingVo> addJobPosting(@Valid @RequestBody CreateJobPostingRequest request) {
        return ResponseType.Success(jobPostingService.createJobPosting(request), "職缺新增成功");
    }

    @GetMapping("/get")
    @RequirePermission({"System", "JobPosting", "View"})
    @ApiOperationOk(summary = "取得所有職缺", description = "返回所有職缺列表。")
    public ResponseType<List<JobPostingVo>> getAllJobPostings() {
        return ResponseType.Success(jobPostingService.getAllJobPostings(), "職缺列表查詢成功");
    }

    @GetMapping("/get/{id}")
    @RequirePermission({"System", "JobPosting", "View"})
    @ApiOperationOk(summary = "取得職缺詳情", description = "根據 ID 取得職缺資訊。")
    public ResponseType<JobPostingVo> getJobPostingById(@PathVariable String id) {
        return ResponseType.Success(jobPostingService.getJobPostingById(id), "職缺查詢成功");
    }

    @GetMapping("/company/{companyId}")
    @RequirePermission({"System", "JobPosting", "View"})
    @ApiOperationOk(summary = "取得公司職缺", description = "根據公司 ID 取得該公司所有職缺。")
    public ResponseType<List<JobPostingVo>> getJobPostingsByCompanyId(@PathVariable String companyId) {
        return ResponseType.Success(jobPostingService.getJobPostingsByCompanyId(companyId), "公司職缺查詢成功");
    }

    @PutMapping("/update")
    @RequirePermission({"System", "JobPosting", "Edit"})
    @ApiOperationBadRequest(summary = "更新職缺", description = "更新職缺資訊。")
    public ResponseType<JobPostingVo> updateJobPosting(@RequestBody JobPostingVo jobPostingVo) {
        return ResponseType.Success(jobPostingService.updateJobPosting(jobPostingVo), "職缺更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission({"System", "JobPosting", "Edit"})
    @ApiOperationBadRequest(summary = "刪除職缺", description = "根據 ID 刪除職缺。")
    public ResponseType<String> deleteJobPosting(@PathVariable String id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseType.Success("職缺刪除成功");
    }

    @PostMapping("/scrape/{companyId}")
    @RequirePermission({"System", "JobPosting", "Scrape"})
    @ApiOperationBadRequest(summary = "爬取並分析職缺", description = "根據公司 ID 爬取該公司網站上的職缺並使用 Gemini 分析。")
    public ResponseType<List<JobPostingVo>> scrapeJobs(@PathVariable String companyId) {
        return ResponseType.Success(jobPostingService.scrapeAndAnalyzeJobs(companyId), "職缺爬取與分析完成");
    }

    @PostMapping("/search")
    @RequirePermission({"System", "JobPosting", "View"})
    @ApiOperationBadRequest(summary = "分頁搜尋職缺", description = "根據條件分頁搜尋職缺。")
    public ResponseType<PageResult<JobPostingVo>> searchJobPostings(@RequestBody JobPostingSearchQuery query) {
        return ResponseType.Success(jobPostingService.searchJobPostings(query), "職缺搜尋成功");
    }
}
