package com.example.backedapi.Service;

import com.example.backedapi.model.dto.AlarmMessage;

import java.util.List;

public interface IKafkaConsumerService {
    void listen(List<AlarmMessage> messages);
}
