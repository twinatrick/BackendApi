package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.dto.AlarmMessage;

import java.util.List;

public interface IAlarmService {
    void processAlarm(List<AlarmMessage> alarmMessage);
}
