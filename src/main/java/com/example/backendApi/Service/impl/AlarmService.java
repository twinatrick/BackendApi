package com.example.backendApi.Service.impl;

import com.example.backendApi.Service.IAlarmPublisher;
import com.example.backendApi.Service.IAlarmService;
import com.example.backendApi.Dto.dto.AlarmMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmService implements IAlarmService {

    @Autowired
    private IAlarmPublisher alarmPublisher;

    // 可以使用消息隊列、異步執行器等來處理

    @Override
    public void processAlarm(List<AlarmMessage> alarmMessage) {
        try {
            alarmPublisher.publish(alarmMessage);
        } catch (Exception e) {
            alarmMessage.get(0).setMessage("告警消息發送失敗：" + e.getMessage());
        }

    }

    private void saveAlarm(AlarmMessage alarmMessage) {
        // 實作：保存至資料庫
//        System.out.println("保存告警：" + alarmMessage.getMessage());
    }

    private void logAlarm(AlarmMessage alarmMessage) {
        // 實作：記錄到日誌系統
        System.out.println("記錄告警：" + alarmMessage.getMessage());
    }
}
