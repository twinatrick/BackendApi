package com.example.BackendArchitectureLab.Config;

import com.example.BackendArchitectureLab.Repository.*;
import com.example.BackendArchitectureLab.Service.IBloomFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.init.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class BloomFilterInitializer implements ApplicationRunner {

    private final IBloomFilterService bloomFilterService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final RoleRepository roleRepository;
    private final FunctionRepository functionRepository;
    private final JobPostingRepository jobPostingRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("開始初始化布隆過濾器...");

        populateFromRepository("users", userRepository.findAllIds());
        populateFromRepository("companies", companyRepository.findAllIds());
        populateFromRepository("skills", skillRepository.findAllIds());
        populateFromRepository("roles", roleRepository.findAllIds());
        populateFromRepository("functions", functionRepository.findAllIds());
        populateFromRepository("jobPostings", jobPostingRepository.findAllIds());

        log.info("布隆過濾器初始化完成");
    }

    private void populateFromRepository(String cacheName, List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("布隆過濾器 [bloom:{}] 無資料可填充", cacheName);
            return;
        }
        List<String> idStrings = ids.stream().map(UUID::toString).collect(Collectors.toList());
        bloomFilterService.addAll(cacheName, idStrings);
        log.info("布隆過濾器 [bloom:{}] 已填充 {} 筆資料", cacheName, ids.size());
    }
}
