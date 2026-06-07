package com.example.BackendApi.Service.impl;

import com.example.BackendApi.DataAccess.ICompanyDataAccess;
import com.example.BackendApi.Dto.Vo.CompanyVo;
import com.example.BackendApi.Dto.Vo.CreateCompanyRequest;
import com.example.BackendApi.Dto.Vo.UpdateCompanyRequest;
import com.example.BackendApi.Entity.Company;
import com.example.BackendApi.Mapper.CompanyMapper;
import com.example.BackendApi.Service.ICompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {

    private final ICompanyDataAccess companyDataAccess;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional
    @CacheEvict(value = "companies", allEntries = true)
    public CompanyVo createCompany(CreateCompanyRequest request) {
        Company company = new Company();
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        for (String url : request.getWebsites()) {
            company.addWebsite(url);
        }
        company = companyDataAccess.save(company);
        return companyMapper.toVo(company);
    }

    @Override
    @Cacheable(value = "companies", sync = true)
    public List<CompanyVo> getAllCompanies() {
        return companyDataAccess.findAll().stream()
                .map(companyMapper::toVo)
                .toList();
    }

    @Override
    @Cacheable(value = "companies", key = "#id", sync = true)
    public CompanyVo getCompanyById(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        Company company = companyDataAccess.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        return companyMapper.toVo(company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "companies", allEntries = true)
    public CompanyVo updateCompany(UpdateCompanyRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        Company company = companyDataAccess.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getWebsites() != null) {
            company.getWebsites().clear();
            for (String url : request.getWebsites()) {
                company.addWebsite(url);
            }
        }
        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }
        company = companyDataAccess.save(company);
        return companyMapper.toVo(company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "companies", allEntries = true)
    public void deleteCompany(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        if (!companyDataAccess.existsById(uuid)) {
            throw new IllegalArgumentException("Company not found");
        }
        companyDataAccess.deleteById(uuid);
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
