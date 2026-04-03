package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.AlertCheckLimitRepository;
import com.example.backedapi.dataaccess.IAlertCheckLimitDataAccess;
import com.example.backedapi.model.db.AlertCheckLimit;
import lombok.RequiredArgsConstructor;
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
}
