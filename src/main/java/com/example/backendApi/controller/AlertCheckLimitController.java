package com.example.backendApi.controller;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.AlertCheckLimitSearchQuery;
import com.example.backendApi.Service.IAlertCheckLimitService;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import com.example.backendApi.annotation.openapi.ApiOperationOk;
import com.example.backendApi.Dto.Vo.AlertCheckLimitVo;
import com.example.backendApi.Dto.Vo.ResponseType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/backend/alertCheckLimit")
@ApiControllerTag(name = "Alert Limits", description = "Backend API endpoints - Alert threshold management")
public class AlertCheckLimitController {
    private final IAlertCheckLimitService alertCheckLimitService;
    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add alert limit", description = "Creates or updates an alert limit for a table column.")
    public ResponseType<AlertCheckLimitVo> addLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        return ResponseType.Success(alertCheckLimitService.addLimit(alertCheckLimitVo), "Limit added successfully");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "Get alert limits", description = "Returns all alert limits.")
    public ResponseType<List<AlertCheckLimitVo>> getLimit() {
        return ResponseType.Success(alertCheckLimitService.getLimit(), "Limits fetched successfully");
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update alert limit", description = "Updates an existing alert limit.")
    public ResponseType<AlertCheckLimitVo> updateLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        return ResponseType.Success(alertCheckLimitService.updateLimit(alertCheckLimitVo), "Limit updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete alert limit", description = "Deletes an alert limit.")
    public ResponseType<String> deleteLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        alertCheckLimitService.deleteLimit(alertCheckLimitVo);
        return ResponseType.Success("Limit deleted successfully");
    }
    
    @PostMapping("/search")
    @ApiOperationBadRequest(summary = "搜尋告警檢查限制", description = "支援分頁與條件查詢的告警檢查限制搜尋")
    @Operation(summary = "搜尋告警檢查限制（分頁）", description = "支援 tableName、columnName、limitValue 範圍、createdBy 查詢條件，預設按 createdTime 降序排序")
    public ResponseType<PageResult<AlertCheckLimitVo>> searchAlertCheckLimits(@RequestBody AlertCheckLimitSearchQuery query) {
        return ResponseType.Success(alertCheckLimitService.searchAlertCheckLimits(query), "告警檢查限制查詢成功");
    }
}
