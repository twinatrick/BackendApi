package com.example.backedapi.Service;

import com.example.backedapi.model.dto.AlarmMessage;

import java.util.List;

public interface IAlarmService {
    void processAlarm(List<AlarmMessage> alarmMessage);
}
