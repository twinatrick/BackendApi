package com.example.backedapi.Service;

import com.example.backedapi.Dto.dto.AlarmMessage;

import java.util.List;

public interface IKafkaConsumerService {
    void listen(List<AlarmMessage> messages);
}
