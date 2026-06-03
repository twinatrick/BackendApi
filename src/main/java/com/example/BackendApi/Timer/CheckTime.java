package com.example.BackendApi.Timer;

import com.example.BackendApi.Service.ICheckApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CheckTime {

    private final ICheckApiService checkApiService;

    @Scheduled(cron = "0 30 * * * *")
    public void runCheckApi() {
        try {
            Date now = new Date();
            checkApiService.getAquarkApiData();
            long executeTime = System.currentTimeMillis() - now.getTime();
            System.out.printf("CheckTime.runCheckApi: %s\n", executeTime / 1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
