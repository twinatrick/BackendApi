package com.example.BackendApi.DataAccess.impl;

import com.example.BackendApi.DataAccess.ICompanyDataAccess;
import com.example.BackendApi.Entity.Company;
import com.example.BackendApi.Repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ICompanyDataAccess.
 * Delegates to Spring Data JPA CompanyRepository.
 */
@Component
@RequiredArgsConstructor
public class CompanyDataAccessImpl implements ICompanyDataAccess {

    private final CompanyRepository companyRepository;

    @Override
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return companyRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        companyRepository.deleteById(id);
    }
}
