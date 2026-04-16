package com.example.backedapi.Service;

import com.example.backedapi.Dto.Vo.FunctionVo;

import java.util.List;

public interface IFunctionService {
    FunctionVo addFunction(FunctionVo functionVo);

    List<FunctionVo> getFunction();

    void updateFunction(FunctionVo functionVo);

    void deleteFunction(FunctionVo functionVo);

    void deleteFunction(List<FunctionVo> function);

    void saveFunction(List<FunctionVo> function);

    List<FunctionVo> saveFunctionNewChild(List<FunctionVo> function);

    FunctionVo getFunctionById(String id);

    FunctionVo getFunctionByName(String name);

    FunctionVo getFunctionByNameAndParent(String name, String parent);
}
