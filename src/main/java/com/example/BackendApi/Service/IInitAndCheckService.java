package com.example.BackendApi.Service;

import java.util.List;

public interface IInitAndCheckService {
    void initAndCheck();

    void checkRole();

    void checkLimit();

    boolean checkIsExist(String oneLayer, String twoLayer, String threeLayer);

    void checkFunctionBindDefaultRole();

    void insertFunctionByList(List<String> functionList, String parent);
}
