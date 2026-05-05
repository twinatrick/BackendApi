package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.AlertCheckLimitSearchQuery;
import com.example.backendApi.Dto.Vo.AlertCheckLimitVo;

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
