package com.example.BackendApi.Config;

import com.example.BackendApi.Service.IInitAndCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.init.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class InitRunner {

    private final IInitAndCheckService initAndCheckService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        initAndCheckService.initAndCheck();
    }
}
