package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IAlertCheckLimitService;
import com.example.backedapi.dataaccess.IAlertCheckLimitDataAccess;
import com.example.backedapi.mapper.AlertCheckLimitMapper;
import com.example.backedapi.Dto.Vo.AlertCheckLimitVo;
import com.example.backedapi.Enity.AlertCheckLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertCheckLimitService implements IAlertCheckLimitService {
    private final IAlertCheckLimitDataAccess alertCheckLimitDataAccess;
    private final AlertCheckLimitMapper alertCheckLimitMapper;

    @Override
    @Cacheable(value = "alertCheckLimit",key = "#tableName + '.' + #column" )
    public AlertCheckLimitVo getLimit(String tableName, String column) {
        AlertCheckLimit limit = getLimitEntity(tableName, column);
        return limit == null ? null : alertCheckLimitMapper.toVo(limit);
    }

    @Override
    @CachePut(value = "alertCheckLimit", key = "#tableName + '.' + #column")
    public AlertCheckLimitVo insertLimit(String tableName, String column, double limitValue) {
        AlertCheckLimit alertCheckLimit = new AlertCheckLimit();
        alertCheckLimit.setTableName(tableName);
        alertCheckLimit.setColumnName(column);
        alertCheckLimit.setLimitValue(limitValue);
        List<AlertCheckLimit> limitList = alertCheckLimitDataAccess.findByTableNameAndColumnName(
                alertCheckLimit.getTableName(),
                alertCheckLimit.getColumnName()
        );
        if (limitList.isEmpty()) {
            AlertCheckLimit limit = alertCheckLimitDataAccess.save(alertCheckLimit);
            return alertCheckLimitMapper.toVo(limit);
        }
        AlertCheckLimit limit = limitList.getFirst();
        limit.setLimitValue(limitValue);
        alertCheckLimitDataAccess.save(limit);
        return alertCheckLimitMapper.toVo(limit);
    }

    @Override
    public AlertCheckLimitVo addLimit(AlertCheckLimitVo alertCheckLimitVo) {
        return insertLimit(
                alertCheckLimitVo.getTableName(),
                alertCheckLimitVo.getColumnName(),
                alertCheckLimitVo.getLimitValue()
        );
    }

    @Override
    public AlertCheckLimitVo updateLimit(AlertCheckLimitVo alertCheckLimitVo) {
        AlertCheckLimit updated = updateEntity(alertCheckLimitMapper.toEntity(alertCheckLimitVo));
        return alertCheckLimitMapper.toVo(updated);
    }

    @Override
    public List<AlertCheckLimitVo> getLimit() {
        return alertCheckLimitDataAccess.findAll().stream().map(alertCheckLimitMapper::toVo).toList();
    }

    @Override
    public void deleteLimit(AlertCheckLimitVo alertCheckLimitVo) {
        deleteLimitEntity(alertCheckLimitMapper.toEntity(alertCheckLimitVo));
    }

    @CachePut(value = "alertCheckLimit", key = "#alertCheckLimit.tableName + '.' + #alertCheckLimit.columnName")
    private AlertCheckLimit updateEntity(AlertCheckLimit alertCheckLimit) {
        return alertCheckLimitDataAccess.save(alertCheckLimit);
    }

    @CacheEvict(value = "alertCheckLimit", key = "#alertCheckLimit.tableName + '.' + #alertCheckLimit.columnName")
    private void deleteLimitEntity(AlertCheckLimit alertCheckLimit) {
        alertCheckLimitDataAccess.delete(alertCheckLimit);
    }

    private AlertCheckLimit getLimitEntity(String tableName, String column) {
        List<AlertCheckLimit> limits = alertCheckLimitDataAccess.findByTableNameAndColumnName(tableName, column);
        if (limits.isEmpty()) {
            return null;
        }
        return limits.getFirst();
    }
}
