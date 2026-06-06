package com.example.BackendApi.Controller;

import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationOk;
import com.example.BackendApi.Dto.Vo.CompanyVo;
import com.example.BackendApi.Dto.Vo.CreateCompanyRequest;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.UpdateCompanyRequest;
import com.example.BackendApi.Service.ICompanyService;
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
@RequestMapping("/company")
@ApiControllerTag(name = "Company", description = "Backend API endpoints - Company management")
public class CompanyController {

    private final ICompanyService companyService;

    public CompanyController(ICompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "新增公司", description = "建立一間新的公司。")
    public ResponseType<CompanyVo> addCompany(@RequestBody CreateCompanyRequest request) {
        return ResponseType.Success(companyService.createCompany(request), "公司新增成功");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "取得所有公司", description = "返回所有公司列表。")
    public ResponseType<List<CompanyVo>> getAllCompanies() {
        return ResponseType.Success(companyService.getAllCompanies(), "公司列表查詢成功");
    }

    @GetMapping("/get/{id}")
    @ApiOperationOk(summary = "取得公司詳情", description = "根據 ID 取得公司資訊。")
    public ResponseType<CompanyVo> getCompanyById(@PathVariable String id) {
        return ResponseType.Success(companyService.getCompanyById(id), "公司查詢成功");
    }

    @PutMapping("/update")
    @ApiOperationBadRequest(summary = "更新公司", description = "更新公司資訊。")
    public ResponseType<CompanyVo> updateCompany(@RequestBody UpdateCompanyRequest request) {
        return ResponseType.Success(companyService.updateCompany(request), "公司更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperationBadRequest(summary = "刪除公司", description = "根據 ID 刪除公司。")
    public ResponseType<String> deleteCompany(@PathVariable String id) {
        companyService.deleteCompany(id);
        return ResponseType.Success("公司刪除成功");
    }
}
