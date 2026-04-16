package com.example.backedapi.controller;

import com.example.backedapi.Service.IAlertCheckLimitService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.Vo.AlertCheckLimitVo;
import com.example.backedapi.model.Vo.ResponseType;
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
}
