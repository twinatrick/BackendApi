package com.example.backedapi.config;

import com.example.backedapi.Service.IInitAndCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitRunner {

    private final IInitAndCheckService initAndCheckService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        initAndCheckService.initAndCheck();
    }
}
