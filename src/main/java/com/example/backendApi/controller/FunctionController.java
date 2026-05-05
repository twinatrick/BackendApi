package com.example.backendApi.controller;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.FunctionSearchQuery;
import com.example.backendApi.Service.IFunctionService;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import com.example.backendApi.annotation.openapi.ApiOperationOk;
import com.example.backendApi.Dto.Vo.FunctionTransVo;
import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Dto.Vo.ResponseType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/function")
@ApiControllerTag(name = "Functions", description = "Backend API endpoints - Function management")
public class FunctionController {
    private final IFunctionService functionService;

    public FunctionController(IFunctionService functionService) {
        this.functionService = functionService;
    }

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add function", description = "Creates a new function entry.")
    public ResponseType<?> addFunction(@RequestBody FunctionVo function) {
        functionService.addFunction(function);
        return ResponseType.Success("Function added successfully");
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update function", description = "Updates an existing function.")
    public ResponseType<String> updateFunction(@RequestBody FunctionVo function) {
        functionService.updateFunction(function);
        return ResponseType.Success("Function updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete function", description = "Deletes a function.")
    public ResponseType<String> deleteFunction(@RequestBody FunctionVo function) {
        functionService.deleteFunction(function);
        return ResponseType.Success("Function deleted successfully");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "Get functions", description = "Returns all functions.")
    public ResponseType<List<FunctionVo>> getFunction() {
        return ResponseType.Success(functionService.getFunction(), "Functions fetched successfully");
    }

    @PostMapping("/saveAllFunction")
    @ApiOperationBadRequest(summary = "Save function changes", description = "Applies function deletions and saves new or updated functions.")
    public ResponseType<?> saveAllFunction(@RequestBody FunctionTransVo function) {
        functionService.deleteFunction(function.getDeleteFunction());
        functionService.saveFunction(function.getSaveMainFunction());
        functionService.saveFunctionNewChild(function.getSaveFunctionNewChild());
        return ResponseType.Success("Functions saved successfully");
    }
    
    @PostMapping("/search")
    @ApiOperationOk(summary = "Search functions with pagination", description = "搜尋功能並回傳分頁結果，支援多種查詢條件與排序")
    public ResponseType<PageResult<FunctionVo>> searchFunctions(@Valid @RequestBody FunctionSearchQuery query) {
        PageResult<FunctionVo> result = functionService.searchFunctions(query);
        return ResponseType.Success(result, "Functions fetched successfully");
    }
}
