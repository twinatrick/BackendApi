package com.example.backedapi.controller;

import com.example.backedapi.Service.IAquarkDataService;
import com.example.backedapi.annotation.Ingnore;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.Dto.Vo.ResponseType;
import com.example.backedapi.Dto.Vo.aquarkUse.AquarkDataRaw;
import com.example.backedapi.Dto.Vo.aquarkUse.AverageAquark;
import com.example.backedapi.Dto.Vo.aquarkUse.CriteriaAPIFilter;
import com.example.backedapi.Dto.Vo.aquarkUse.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/backend/aquarkData")
@ApiControllerTag(name = "AquarkData", description = "Backend API endpoints - Aquark data queries")
public class AquarkDataController {
    private final IAquarkDataService aquarkDataService;

    @PostMapping("/getData")
    @ApiOperationBadRequest(summary = "Get aquark data", description = "Returns aquark data filtered by criteria.")
    public ResponseType<List<AquarkDataRaw>> getData(@RequestBody List<CriteriaAPIFilter> fillterList) {


        return new ResponseType<>(aquarkDataService.getAquarkDataWithFilter(fillterList));
    }

    @GetMapping("/getColumnNameList")
    @ApiOperationOk(summary = "Get column names", description = "Returns available aquark data column names.")
    public ResponseType<List<String>> getColumnNameList() {
        return new ResponseType<>(aquarkDataService.getColumnNameList());
    }
    @Ingnore

    @PostMapping("/getAverage")
    @ApiOperationBadRequest(summary = "Get average aquark data", description = "Returns averaged aquark data within a time range.")
    public ResponseType<List<AverageAquark>> getAverage(@RequestBody TimeRange time) {
        return new ResponseType<>(aquarkDataService.getAverageAquark(time.getStart(), time.getEnd()));
    }
}
