package com.example.backedapi.Service;

import com.example.backedapi.Dto.Vo.AlertCheckLimitVo;

import java.util.List;

public interface IAlertCheckLimitService {
    AlertCheckLimitVo getLimit(String tableName, String column);

    AlertCheckLimitVo insertLimit(String tableName, String column, double limitValue);

    AlertCheckLimitVo updateLimit(AlertCheckLimitVo alertCheckLimitVo);

    List<AlertCheckLimitVo> getLimit();

    void deleteLimit(AlertCheckLimitVo alertCheckLimitVo);

    AlertCheckLimitVo addLimit(AlertCheckLimitVo alertCheckLimitVo);
}
