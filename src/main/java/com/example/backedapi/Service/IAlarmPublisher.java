package com.example.backedapi.Service;

import com.example.backedapi.model.dto.AlarmMessage;

import java.util.List;

public interface IAlarmPublisher {
    void publish(List<AlarmMessage> messages);
}
