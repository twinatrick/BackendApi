package com.example.BackendApi.DataAccess.impl;

import com.example.BackendApi.Dto.Vo.Search.AlertCheckLimitSearchQuery;
import com.example.BackendApi.Repository.AlertCheckLimitRepository;
import com.example.BackendApi.DataAccess.IAlertCheckLimitDataAccess;
import com.example.BackendApi.DataAccess.specification.AlertCheckLimitSpecification;
import com.example.BackendApi.Entity.AlertCheckLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of IAlertCheckLimitDataAccess.
 * Delegates to Spring Data JPA AlertCheckLimitRepository.
 */
@Component
@RequiredArgsConstructor
public class AlertCheckLimitDataAccessImpl implements IAlertCheckLimitDataAccess {

    private final AlertCheckLimitRepository alertCheckLimitRepository;

    @Override
    public List<AlertCheckLimit> findAll() {
        return alertCheckLimitRepository.findAll();
    }

    @Override
    public List<AlertCheckLimit> findByTableNameAndColumnName(String tableName, String columnName) {
        return alertCheckLimitRepository.findAlertCheckLimitByTableNameAndColumnName(tableName, columnName);
    }

    @Override
    public AlertCheckLimit save(AlertCheckLimit alertCheckLimit) {
        return alertCheckLimitRepository.save(alertCheckLimit);
    }

    @Override
    public void delete(AlertCheckLimit alertCheckLimit) {
        alertCheckLimitRepository.delete(alertCheckLimit);
    }
    
    @Override
    public Page<AlertCheckLimit> searchAlertCheckLimits(AlertCheckLimitSearchQuery query) {
        Sort sort = Sort.by(
            "asc".equalsIgnoreCase(query.getSortDir()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            query.getSortBy()
        );
        
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), sort);
        
        return alertCheckLimitRepository.findAll(
            AlertCheckLimitSpecification.buildSpecification(query),
            pageRequest
        );
    }
}
