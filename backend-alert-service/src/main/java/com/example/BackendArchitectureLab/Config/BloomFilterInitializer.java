package com.example.BackendArchitectureLab.Config;

import com.example.BackendArchitectureLab.Service.IBloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.init.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class BloomFilterInitializer implements ApplicationRunner {

    @Autowired
    private IBloomFilterService bloomFilterService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bloom filter initialization removed - each service initializes its own");
    }
}
