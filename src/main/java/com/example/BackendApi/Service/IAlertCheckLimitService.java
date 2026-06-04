package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.AlertCheckLimitVo;
import com.example.BackendApi.Dto.Vo.Search.AlertCheckLimitSearchQuery;
import com.example.BackendApi.Dto.Vo.common.PageResult;

import java.util.List;

public interface IAlertCheckLimitService {
    AlertCheckLimitVo getLimit(String tableName, String column);

    AlertCheckLimitVo insertLimit(String tableName, String column, double limitValue);

    AlertCheckLimitVo updateLimit(AlertCheckLimitVo alertCheckLimitVo);

    List<AlertCheckLimitVo> getLimit();

    void deleteLimit(AlertCheckLimitVo alertCheckLimitVo);

    AlertCheckLimitVo addLimit(AlertCheckLimitVo alertCheckLimitVo);
    
    /**
     * 搜尋告警檢查限制（支援分頁與條件查詢）
     *
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<AlertCheckLimitVo> searchAlertCheckLimits(AlertCheckLimitSearchQuery query);
}
