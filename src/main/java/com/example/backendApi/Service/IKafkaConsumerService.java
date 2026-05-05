package com.example.backendApi.Service;

import com.example.backendApi.Dto.dto.AlarmMessage;

import java.util.List;

public interface IKafkaConsumerService {
    void listen(List<AlarmMessage> messages);
}
