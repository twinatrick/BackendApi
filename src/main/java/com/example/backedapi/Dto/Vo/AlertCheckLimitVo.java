package com.example.backedapi.Dto.Vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;


@Getter
@Setter
public class AlertCheckLimitVo implements Serializable {

    private UUID id;
    private String tableName;
    private String columnName;

    private double limitValue;

    public AlertCheckLimitVo(UUID id, String tableName, String columnName, double limitValue) {
        this.id = id;
        this.tableName = tableName;
        this.columnName = columnName;
        this.limitValue = limitValue;
    }
}
