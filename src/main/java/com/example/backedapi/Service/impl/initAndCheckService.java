package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IAlertCheckLimitService;
import com.example.backedapi.Service.IFunctionService;
import com.example.backedapi.Service.IInitAndCheckService;
import com.example.backedapi.Service.IRoleService;
import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.AlertCheckLimitVo;
import com.example.backedapi.Dto.Vo.RoleOutVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class initAndCheckService implements IInitAndCheckService {
    @Autowired
    private IRoleService roleService;

    @Autowired
    private IAlertCheckLimitService alertCheckLimitService;

    @Autowired
    private IFunctionService functionService;

    @Override
    public void initAndCheck() {
        checkRole();

        checkLimit();
        checkFunctionBindDefaultRole();
        // 初始化告警設定
        // 1. 從資料庫讀取告警設定
        // 2. 將告警設定放入內存
        // 3. 啟動告警檢查任務
    }

    @Override
    public void checkRole() {
        List<RoleOutVo> roleList = roleService.getRole();
        if (roleList.isEmpty()) {
            // 初始化角色
            RoleOutVo role = new RoleOutVo();
            role.setName("admin");
            roleService.addRole(role);
            role = new RoleOutVo();
            role.setName("user");
            roleService.addRole(role);
        }
    }

    @Override
    public void checkLimit() {
        List<AlertCheckLimitVo> alertCheckLimitList = alertCheckLimitService.getLimit();
        if (alertCheckLimitList.isEmpty()) {
            alertCheckLimitService.insertLimit("aquark_data", "rain_d", 10);
            alertCheckLimitService.insertLimit("aquark_data", "moisture", 10);
            alertCheckLimitService.insertLimit("aquark_data", "temperature", 10);
            alertCheckLimitService.insertLimit("aquark_data", "echo", 10);
            alertCheckLimitService.insertLimit("aquark_data", "water_speed_aquark", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v1", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v2", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v3", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v4", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v5", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v6", 10);
            alertCheckLimitService.insertLimit("aquark_data", "v7", 10);
        }else {
            String[] aquark_data_column = {"rain_d", "moisture", "temperature", "echo", "water_speed_aquark", "v1", "v2", "v3", "v4", "v5", "v6", "v7"};
            Arrays.stream(aquark_data_column).forEach(s -> {
                AlertCheckLimitVo alertCheckLimit = alertCheckLimitService.getLimit("aquark_data", s);
                if (alertCheckLimit == null) {
                    alertCheckLimitService.insertLimit("aquark_data", s, 10);
                }
            });
        }
    }
    @Override
    public boolean checkIsExist(String oneLayer, String twoLayer, String threeLayer) {
        FunctionVo one= functionService.getFunctionByName(oneLayer);
        if (one == null) {
            return false;
        }
        FunctionVo two = functionService.getFunctionByNameAndParent(twoLayer, one.getId());

        if (two == null) {
            return false;
        }
        FunctionVo three = functionService.getFunctionByNameAndParent(threeLayer, two.getId());
        if (three == null) {
            return false;
        }
        return true;
    }

    @Override
    public void checkFunctionBindDefaultRole() {
        List<FunctionVo> functionList = functionService.getFunction();
        List<List<String>> allFunctionList = new ArrayList<>();
        allFunctionList.add(new ArrayList<>( List.of("System", "User", "View")));
        allFunctionList.add(new ArrayList<>( List.of("System", "RolePermission", "View")));
        allFunctionList.add(new ArrayList<>( List.of("DataView", "AquarkData", "View")));
        allFunctionList.add(new ArrayList<>( List.of("DataView", "AquarkDataAvg", "View")));
        allFunctionList.add(new ArrayList<>( List.of("System", "LimitSetting", "View")));


        for (List<String> functionListStr : allFunctionList) {
            if (!checkIsExist(functionListStr.get(0), functionListStr.get(1), functionListStr.get(2))) {
                insertFunctionByList(functionListStr,"");
            }
        }
        functionList = functionService.getFunction();
        RoleOutVo role = roleService.getRoleByName("admin");
        if (role != null) {
            List<String> functionIds = functionList.stream().map(FunctionVo::getId).toList();
            roleService.roleBindFunction(role.getId().toString(), functionIds);
        }
    }
    @Override
    public void insertFunctionByList(List<String> functionList , String parent) {
        if (functionList.isEmpty()) {
            return;
        }
        FunctionVo sameFunction = functionService.getFunctionByNameAndParent(functionList.getFirst(), parent);
        if (sameFunction != null) {
            insertFunctionByList(functionList.subList(1, functionList.size()), sameFunction.getId());
        }else {
            FunctionVo f = new FunctionVo();
            f.setName(functionList.getFirst());
            f.setParent(parent);
            f = functionService.addFunction(f);
            insertFunctionByList(functionList.subList(1, functionList.size()), f.getId());
        }

    }

}
