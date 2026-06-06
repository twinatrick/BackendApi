package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.CompanyVo;
import com.example.BackendApi.Dto.Vo.CreateCompanyRequest;
import com.example.BackendApi.Dto.Vo.UpdateCompanyRequest;

import java.util.List;

public interface ICompanyService {

    CompanyVo createCompany(CreateCompanyRequest request);

    List<CompanyVo> getAllCompanies();

    CompanyVo getCompanyById(String id);

    CompanyVo updateCompany(UpdateCompanyRequest request);

    void deleteCompany(String id);
}
