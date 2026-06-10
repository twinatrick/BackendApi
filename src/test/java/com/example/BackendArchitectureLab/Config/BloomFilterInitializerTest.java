package com.example.BackendArchitectureLab.Config;

import com.example.BackendArchitectureLab.Repository.*;
import com.example.BackendArchitectureLab.Service.IBloomFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BloomFilterInitializerTest {

    @Mock
    private IBloomFilterService bloomFilterService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private FunctionRepository functionRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    private BloomFilterInitializer initializer;

    private final List<UUID> sampleIds = List.of(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
    );

    @BeforeEach
    void setUp() {
        when(userRepository.findAllIds()).thenReturn(sampleIds);
        when(companyRepository.findAllIds()).thenReturn(sampleIds);
        when(skillRepository.findAllIds()).thenReturn(sampleIds);
        when(roleRepository.findAllIds()).thenReturn(sampleIds);
        when(functionRepository.findAllIds()).thenReturn(sampleIds);
        when(jobPostingRepository.findAllIds()).thenReturn(sampleIds);

        initializer = new BloomFilterInitializer(
                bloomFilterService, userRepository, companyRepository,
                skillRepository, roleRepository, functionRepository,
                jobPostingRepository
        );
    }

    @Test
    void run_AllReposHaveData_PopulatesSixFilters() {
        initializer.run(null);

        verify(bloomFilterService).addAll("users", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("companies", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("skills", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("roles", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("functions", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("jobPostings", sampleIds.stream().map(UUID::toString).toList());
        verifyNoMoreInteractions(bloomFilterService);
    }

    @Test
    void run_AllReposEmpty_DoesNotCallAddAll() {
        when(userRepository.findAllIds()).thenReturn(List.of());
        when(companyRepository.findAllIds()).thenReturn(List.of());
        when(skillRepository.findAllIds()).thenReturn(List.of());
        when(roleRepository.findAllIds()).thenReturn(List.of());
        when(functionRepository.findAllIds()).thenReturn(List.of());
        when(jobPostingRepository.findAllIds()).thenReturn(List.of());

        initializer.run(null);

        verify(bloomFilterService, never()).addAll(anyString(), anyList());
    }

    @Test
    void run_SomeReposEmpty_PopulatesOnlyNonEmpty() {
        when(companyRepository.findAllIds()).thenReturn(List.of());
        when(functionRepository.findAllIds()).thenReturn(List.of());

        initializer.run(null);

        verify(bloomFilterService).addAll("users", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("skills", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("roles", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("jobPostings", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService, never()).addAll(eq("companies"), anyList());
        verify(bloomFilterService, never()).addAll(eq("functions"), anyList());
    }

    @Test
    void run_NullIds_SkipsGracefully() {
        when(userRepository.findAllIds()).thenReturn(null);
        when(skillRepository.findAllIds()).thenReturn(null);
        when(roleRepository.findAllIds()).thenReturn(null);

        initializer.run(null);

        verify(bloomFilterService).addAll("companies", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("functions", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService).addAll("jobPostings", sampleIds.stream().map(UUID::toString).toList());
        verify(bloomFilterService, never()).addAll(eq("users"), anyList());
        verify(bloomFilterService, never()).addAll(eq("skills"), anyList());
        verify(bloomFilterService, never()).addAll(eq("roles"), anyList());
    }

    @Test
    void run_CalledTwice_DoesNotDuplicate() {
        initializer.run(null);
        initializer.run(null);

        verify(bloomFilterService, times(2)).addAll("users", sampleIds.stream().map(UUID::toString).toList());
    }
}
