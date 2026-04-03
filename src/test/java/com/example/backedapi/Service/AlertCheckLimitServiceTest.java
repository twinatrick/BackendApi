package com.example.backedapi.Service;

import com.example.backedapi.Service.impl.AlertCheckLimitService;
import com.example.backedapi.dataaccess.IAlertCheckLimitDataAccess;
import com.example.backedapi.mapper.AlertCheckLimitMapper;
import com.example.backedapi.model.Vo.AlertCheckLimitVo;
import com.example.backedapi.model.db.AlertCheckLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlertCheckLimitService.
 * Uses Mockito to mock DataAccess dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AlertCheckLimitServiceTest {

    @Mock
    private IAlertCheckLimitDataAccess alertCheckLimitDataAccess;

    @Mock
    private AlertCheckLimitMapper alertCheckLimitMapper;

    @InjectMocks
    private AlertCheckLimitService alertCheckLimitService;

    private AlertCheckLimit existingLimit;

    @BeforeEach
    void setUp() {
        existingLimit = new AlertCheckLimit();
        existingLimit.setId(UUID.randomUUID());
        existingLimit.setTableName("aquark_data");
        existingLimit.setColumnName("rain_d");
        existingLimit.setLimitValue(10.5);

        when(alertCheckLimitMapper.toVo(any(AlertCheckLimit.class))).thenAnswer(invocation -> {
            AlertCheckLimit limit = invocation.getArgument(0);
            AlertCheckLimitVo vo = new AlertCheckLimitVo(limit.getId(), limit.getTableName(), limit.getColumnName(), limit.getLimitValue());
            return vo;
        });
        when(alertCheckLimitMapper.toEntity(any(AlertCheckLimitVo.class))).thenAnswer(invocation -> {
            AlertCheckLimitVo vo = invocation.getArgument(0);
            AlertCheckLimit limit = new AlertCheckLimit();
            limit.setId(vo.getId());
            limit.setTableName(vo.getTableName());
            limit.setColumnName(vo.getColumnName());
            limit.setLimitValue(vo.getLimitValue());
            return limit;
        });
    }

    @Test
    void testGetLimit_NotFound() {
        when(alertCheckLimitDataAccess.findByTableNameAndColumnName("aquark_data", "rain_d"))
                .thenReturn(List.of());

        AlertCheckLimitVo result = alertCheckLimitService.getLimit("aquark_data", "rain_d");

        assertNull(result);
        verify(alertCheckLimitDataAccess, times(1))
                .findByTableNameAndColumnName("aquark_data", "rain_d");
    }

    @Test
    void testGetLimit_Found() {
        AlertCheckLimit other = new AlertCheckLimit();
        other.setId(UUID.randomUUID());
        when(alertCheckLimitDataAccess.findByTableNameAndColumnName("aquark_data", "rain_d"))
                .thenReturn(List.of(existingLimit, other));

        AlertCheckLimitVo result = alertCheckLimitService.getLimit("aquark_data", "rain_d");

        assertNotNull(result);
        assertEquals(existingLimit.getId(), result.getId());
    }

    @Test
    void testInsertLimit_NewLimit() {
        when(alertCheckLimitDataAccess.findByTableNameAndColumnName("aquark_data", "rain_d"))
                .thenReturn(List.of());
        when(alertCheckLimitDataAccess.save(any(AlertCheckLimit.class))).thenAnswer(invocation -> {
            AlertCheckLimit saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        AlertCheckLimitVo result = alertCheckLimitService.insertLimit("aquark_data", "rain_d", 12.3);

        assertNotNull(result);
        assertEquals("aquark_data", result.getTableName());
        assertEquals("rain_d", result.getColumnName());
        assertEquals(12.3, result.getLimitValue());
        verify(alertCheckLimitDataAccess, times(1)).save(any(AlertCheckLimit.class));
    }

    @Test
    void testInsertLimit_ExistingLimit() {
        when(alertCheckLimitDataAccess.findByTableNameAndColumnName("aquark_data", "rain_d"))
                .thenReturn(List.of(existingLimit));
        when(alertCheckLimitDataAccess.save(any(AlertCheckLimit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertCheckLimitVo result = alertCheckLimitService.insertLimit("aquark_data", "rain_d", 99.9);

        ArgumentCaptor<AlertCheckLimit> captor = ArgumentCaptor.forClass(AlertCheckLimit.class);
        verify(alertCheckLimitDataAccess).save(captor.capture());

        AlertCheckLimit saved = captor.getValue();
        assertEquals(existingLimit.getId(), saved.getId());
        assertEquals(99.9, saved.getLimitValue());
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void testUpdate() {
        when(alertCheckLimitDataAccess.save(any(AlertCheckLimit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertCheckLimitVo updateVo = new AlertCheckLimitVo(existingLimit.getId(), existingLimit.getTableName(), existingLimit.getColumnName(), 22.2);
        AlertCheckLimitVo result = alertCheckLimitService.updateLimit(updateVo);

        assertEquals(22.2, result.getLimitValue());
        verify(alertCheckLimitDataAccess, times(1)).save(any(AlertCheckLimit.class));
    }

    @Test
    void testGetLimit_All() {
        when(alertCheckLimitDataAccess.findAll()).thenReturn(List.of(existingLimit));

        List<AlertCheckLimitVo> result = alertCheckLimitService.getLimit();

        assertEquals(1, result.size());
        assertEquals(existingLimit.getId(), result.getFirst().getId());
    }

    @Test
    void testDeleteLimit() {
        AlertCheckLimitVo vo = new AlertCheckLimitVo(existingLimit.getId(), existingLimit.getTableName(), existingLimit.getColumnName(), existingLimit.getLimitValue());
        alertCheckLimitService.deleteLimit(vo);

        verify(alertCheckLimitDataAccess, times(1)).delete(any(AlertCheckLimit.class));
    }
}
