package com.example.BackendApi.Service.impl;

import com.example.BackendApi.Dto.Vo.Common.AlarmMessage;
import com.example.BackendApi.Service.IKafkaConsumerService;
import com.example.BackendApi.WebSocket.AlarmWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService implements IKafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "socketSend", containerFactory = "alarmMessageKafkaListenerContainerFactory")
    @Override
    public void listen(List<AlarmMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            LOGGER.warn("Received empty alarm message list");
            return;
        }
        System.out.println("outSize:" + messages.size());
        messages.forEach(AlarmWebSocket::broadcast);
    }

}
