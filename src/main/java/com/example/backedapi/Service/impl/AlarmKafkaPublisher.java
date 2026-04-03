package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IAlarmPublisher;
import com.example.backedapi.model.dto.AlarmMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AlarmKafkaPublisher implements IAlarmPublisher {

    private static final String TOPIC = "socketSend";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private String toJsonString(List<AlarmMessage> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


    @Override
    public void publish(List<AlarmMessage> message)  {
        System.out.println("Sending message: {}"+ message.size());
            kafkaTemplate.send(TOPIC, Objects.requireNonNull(toJsonString(message)));

    }
}
