package com.example.BackendArchitectureLab.Util;

import com.example.BackendArchitectureLab.Exception.AppException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排序欄位驗證工具類
 */
public class SortFieldValidator {

    /**
     * 驗證排序欄位和方向（使用 Set）
     *
     * @param sortBy        排序欄位
     * @param sortDir       排序方向
     * @param allowedFields 允許的排序欄位集合
     * @throws AppException 如果欄位或方向不合法
     */
    public static void validate(String sortBy, String sortDir, Set<String> allowedFields) {
        if (!allowedFields.contains(sortBy)) {
            throw new AppException("排序欄位錯誤",
                    String.format("非法的排序欄位: %s。允許的欄位: %s",
                            sortBy, String.join(", ", allowedFields)), 400);
        }
        
        validateSortDirection(sortDir);
    }

    /**
     * 驗證排序欄位是否合法
     *
     * @param sortBy        排序欄位
     * @param allowedFields 允許的排序欄位
     * @throws AppException 如果欄位不合法
     */
    public static void validateSortField(String sortBy, String... allowedFields) {
        Set<String> allowedSet = Arrays.stream(allowedFields)
                .collect(Collectors.toSet());

        if (!allowedSet.contains(sortBy)) {
            throw new AppException("排序欄位錯誤",
                    String.format("非法的排序欄位: %s。允許的欄位: %s",
                            sortBy, String.join(", ", allowedFields)), 400);
        }
    }

    /**
     * 驗證排序方向是否合法
     *
     * @param sortDir 排序方向
     * @throws AppException 如果方向不合法
     */
    public static void validateSortDirection(String sortDir) {
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new AppException("排序欄位錯誤",
                    String.format("非法的排序方向: %s。允許的值: asc, desc", sortDir), 400);
        }
    }
}
