package com.example.backedapi.controller;

import com.example.backedapi.Service.IFunctionService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.Dto.Vo.FunctionTransVo;
import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.ResponseType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backend/function")
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
}
