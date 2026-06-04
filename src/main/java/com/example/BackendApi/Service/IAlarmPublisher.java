package com.example.BackendApi.Service;


import com.example.BackendApi.Dto.Vo.Common.AlarmMessage;

import java.util.List;

public interface IAlarmPublisher {
    void publish(List<AlarmMessage> messages);
}
