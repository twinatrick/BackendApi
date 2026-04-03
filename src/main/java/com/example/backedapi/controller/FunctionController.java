package com.example.backedapi.controller;

import com.example.backedapi.Service.FunctionService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.db.Function;
import com.example.backedapi.model.Vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backend/function")
@ApiControllerTag(name = "Functions", description = "Backend API endpoints - Function management")
public class FunctionController {
    @Autowired
    private FunctionService functionService;

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add function", description = "Creates a new function entry.")
    public ResponseType<?> addFunction(@RequestBody FunctionVo function) {
        try {
            FunctionVo    f = functionService.addFunction(function.toFunction()).toVo();
            return new ResponseType<>(0, f);
        } catch (Exception e) {
            return new ResponseType<>(-1, null, "Error adding function");
        }

    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update function", description = "Updates an existing function.")

    public ResponseType<String> updateFunction(@RequestBody Function function) {
        try {
            functionService.updateFunction(function);
        } catch (Exception e) {
            return new ResponseType<>(-1, "Error updating function");
        }

        return new ResponseType<>(0, "Function updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete function", description = "Deletes a function.")
    public ResponseType<String> deleteFunction(@RequestBody Function function) {
        try {
            functionService.deleteFunction(function);
        } catch (Exception e) {
            return new ResponseType<>(-1, "Error deleting function");
        }

        return new ResponseType<>(0, "Function deleted successfully");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "Get functions", description = "Returns all functions.")
    public ResponseType<List<FunctionVo>> getFunction() {
        return new ResponseType<>(0, functionService.getFunction().stream().map(Function::toVo).toList());
    }

    @PostMapping("/saveAllFunction")
    @ApiOperationBadRequest(summary = "Save function changes", description = "Applies function deletions and saves new or updated functions.")
    public ResponseType<?> saveAllFunction(@RequestBody FunctionTransVo function) {
        functionService.deleteFunction(function.getDeleteFunction());
        functionService.saveFunction(function.getSaveMainFunction());
        List<FunctionVo> data= functionService.saveFunctionNewChild(function.getSaveFunctionNewChild()).stream().map(Function::toVo).toList();
        ResponseType response = new ResponseType();
        response.setCode(0);
        response.setData(data);
        return response;
    }
}
