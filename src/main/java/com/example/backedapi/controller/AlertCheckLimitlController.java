package com.example.backedapi.controller;

import com.example.backedapi.Service.AlertCheckLimitService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.Vo.AlertCheckLimitVo;
import com.example.backedapi.model.Vo.ResponseType;
import com.example.backedapi.model.db.AlertCheckLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/backend/alertCheckLimit")
@ApiControllerTag(name = "Alert Limits", description = "Backend API endpoints - Alert threshold management")
public class AlertCheckLimitlController {
    private final AlertCheckLimitService alertCheckLimitService;


    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add alert limit", description = "Creates or updates an alert limit for a table column.")
    public ResponseType<AlertCheckLimitVo> addLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        try {
            AlertCheckLimit  data=   alertCheckLimitService.insertLimit(alertCheckLimitVo.getTableName(),alertCheckLimitVo.getColumnName(),alertCheckLimitVo.getLimitValue());
            return new ResponseType<>( 0,data.toVo());
        }catch (Exception e){
            return new ResponseType<>( -1,null,"Error adding limit");
        }
    }
    @GetMapping("/get")
    @ApiOperationOk(summary = "Get alert limits", description = "Returns all alert limits.")
    public ResponseType<List<AlertCheckLimitVo>> getLimit() {
        return new ResponseType<>( 0,alertCheckLimitService.getLimit().stream().map(AlertCheckLimit::toVo).toList());
    }
    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update alert limit", description = "Updates an existing alert limit.")
    public ResponseType<AlertCheckLimitVo> updateLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        try {
            return new ResponseType<>( 0,alertCheckLimitService.update(alertCheckLimitVo.toDb()).toVo());
        }catch (Exception e){
            return new ResponseType<>( -1,null,"Error updating limit");
        }
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete alert limit", description = "Deletes an alert limit.")
    public ResponseType<String> deleteLimit(@RequestBody AlertCheckLimitVo alertCheckLimitVo) {
        try {
            alertCheckLimitService.deleteLimit(alertCheckLimitVo.toDb());
        }catch (Exception e){
            return new ResponseType<>( -1,"Error deleting limit");
        }

        return new ResponseType<>( 0,"limit deleted successfully");
    }
}
