package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.AlertCheckLimitVo;
import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Dto.Vo.RoleOutVo;
import com.example.backendApi.Service.impl.initAndCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * initAndCheckService 測試類別
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class initAndCheckServiceTest {

    @Mock
    private IRoleService roleService;

    @Mock
    private IAlertCheckLimitService alertCheckLimitService;

    @Mock
    private IFunctionService functionService;

    @InjectMocks
    private initAndCheckService initAndCheckService;

    private RoleOutVo adminRole;
    private RoleOutVo userRole;
    private FunctionVo testFunction;
    private AlertCheckLimitVo testLimit;

    @BeforeEach
    void setUp() {
        // 設置測試用角色
        adminRole = new RoleOutVo();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("admin");

        userRole = new RoleOutVo();
        userRole.setId(UUID.randomUUID());
        userRole.setName("user");

        // 設置測試用功能
        testFunction = new FunctionVo();
        testFunction.setId(UUID.randomUUID().toString());
        testFunction.setName("TestFunction");
        testFunction.setParent("");

        // 設置測試用告警限制
        testLimit = new AlertCheckLimitVo(UUID.randomUUID(), "aquark_data", "rain_d", 10.0);
    }

    @Test
    void testInitAndCheck_應該調用所有初始化方法() {
        // Arrange
        when(roleService.getRole()).thenReturn(Collections.emptyList());
        when(roleService.addRole(any(RoleOutVo.class))).thenReturn(adminRole, userRole);
        when(alertCheckLimitService.getLimit()).thenReturn(Collections.emptyList());
        when(alertCheckLimitService.insertLimit(anyString(), anyString(), anyDouble())).thenReturn(testLimit);
        when(functionService.getFunction()).thenReturn(Collections.emptyList());
        when(functionService.getFunctionByName(anyString())).thenReturn(null);
        when(functionService.getFunctionByNameAndParent(anyString(), anyString())).thenReturn(null);
        
        // Create FunctionVo that will be returned by addFunction
        FunctionVo createdFunction = new FunctionVo();
        createdFunction.setId(UUID.randomUUID().toString());
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(createdFunction);
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);
        doNothing().when(roleService).roleBindFunction(anyString(), anyList());

        // Act
        initAndCheckService.initAndCheck();

        // Assert
        verify(roleService, atLeastOnce()).getRole();
        verify(alertCheckLimitService, atLeastOnce()).getLimit();
        verify(functionService, atLeastOnce()).getFunction();
    }

    @Test
    void testCheckRole_當角色列表為空_應該初始化角色() {
        // Arrange
        when(roleService.getRole()).thenReturn(Collections.emptyList());
        when(roleService.addRole(any(RoleOutVo.class))).thenReturn(adminRole, userRole);

        // Act
        initAndCheckService.checkRole();

        // Assert
        verify(roleService, times(1)).getRole();
        verify(roleService, times(2)).addRole(any(RoleOutVo.class));
    }

    @Test
    void testCheckRole_當角色列表存在_不應該初始化角色() {
        // Arrange
        when(roleService.getRole()).thenReturn(List.of(adminRole, userRole));

        // Act
        initAndCheckService.checkRole();

        // Assert
        verify(roleService, times(1)).getRole();
        verify(roleService, never()).addRole(any(RoleOutVo.class));
    }

    @Test
    void testCheckLimit_當限制列表為空_應該初始化所有限制() {
        // Arrange
        when(alertCheckLimitService.getLimit()).thenReturn(Collections.emptyList());
        when(alertCheckLimitService.insertLimit(anyString(), anyString(), anyDouble())).thenReturn(testLimit);

        // Act
        initAndCheckService.checkLimit();

        // Assert
        verify(alertCheckLimitService, times(1)).getLimit();
        verify(alertCheckLimitService, times(12)).insertLimit(eq("aquark_data"), anyString(), eq(10.0));
    }

    @Test
    void testCheckLimit_當限制列表存在但缺少某些欄位_應該補充缺少的限制() {
        // Arrange
        when(alertCheckLimitService.getLimit()).thenReturn(List.of(testLimit));
        when(alertCheckLimitService.getLimit("aquark_data", "rain_d")).thenReturn(testLimit);
        when(alertCheckLimitService.getLimit(eq("aquark_data"), argThat(s -> !s.equals("rain_d")))).thenReturn(null);
        when(alertCheckLimitService.insertLimit(anyString(), anyString(), anyDouble())).thenReturn(testLimit);

        // Act
        initAndCheckService.checkLimit();

        // Assert
        verify(alertCheckLimitService, times(1)).getLimit();
        verify(alertCheckLimitService, times(12)).getLimit(eq("aquark_data"), anyString());
        verify(alertCheckLimitService, times(11)).insertLimit(eq("aquark_data"), anyString(), eq(10.0));
    }

    @Test
    void testCheckIsExist_當三層功能都存在_應該回傳true() {
        // Arrange
        FunctionVo oneLayer = new FunctionVo();
        oneLayer.setId("id1");
        oneLayer.setName("System");

        FunctionVo twoLayer = new FunctionVo();
        twoLayer.setId("id2");
        twoLayer.setName("User");
        twoLayer.setParent("id1");

        FunctionVo threeLayer = new FunctionVo();
        threeLayer.setId("id3");
        threeLayer.setName("View");
        threeLayer.setParent("id2");

        when(functionService.getFunctionByName("System")).thenReturn(oneLayer);
        when(functionService.getFunctionByNameAndParent("User", "id1")).thenReturn(twoLayer);
        when(functionService.getFunctionByNameAndParent("View", "id2")).thenReturn(threeLayer);

        // Act
        boolean result = initAndCheckService.checkIsExist("System", "User", "View");

        // Assert
        assertTrue(result);
        verify(functionService, times(1)).getFunctionByName("System");
        verify(functionService, times(2)).getFunctionByNameAndParent(anyString(), anyString());
    }

    @Test
    void testCheckIsExist_當第一層不存在_應該回傳false() {
        // Arrange
        when(functionService.getFunctionByName("System")).thenReturn(null);

        // Act
        boolean result = initAndCheckService.checkIsExist("System", "User", "View");

        // Assert
        assertFalse(result);
        verify(functionService, times(1)).getFunctionByName("System");
        verify(functionService, never()).getFunctionByNameAndParent(anyString(), anyString());
    }

    @Test
    void testCheckIsExist_當第二層不存在_應該回傳false() {
        // Arrange
        FunctionVo oneLayer = new FunctionVo();
        oneLayer.setId("id1");
        oneLayer.setName("System");

        when(functionService.getFunctionByName("System")).thenReturn(oneLayer);
        when(functionService.getFunctionByNameAndParent("User", "id1")).thenReturn(null);

        // Act
        boolean result = initAndCheckService.checkIsExist("System", "User", "View");

        // Assert
        assertFalse(result);
        verify(functionService, times(1)).getFunctionByName("System");
        verify(functionService, times(1)).getFunctionByNameAndParent("User", "id1");
    }

    @Test
    void testCheckIsExist_當第三層不存在_應該回傳false() {
        // Arrange
        FunctionVo oneLayer = new FunctionVo();
        oneLayer.setId("id1");
        oneLayer.setName("System");

        FunctionVo twoLayer = new FunctionVo();
        twoLayer.setId("id2");
        twoLayer.setName("User");
        twoLayer.setParent("id1");

        when(functionService.getFunctionByName("System")).thenReturn(oneLayer);
        when(functionService.getFunctionByNameAndParent("User", "id1")).thenReturn(twoLayer);
        when(functionService.getFunctionByNameAndParent("View", "id2")).thenReturn(null);

        // Act
        boolean result = initAndCheckService.checkIsExist("System", "User", "View");

        // Assert
        assertFalse(result);
        verify(functionService, times(1)).getFunctionByName("System");
        verify(functionService, times(2)).getFunctionByNameAndParent(anyString(), anyString());
    }

    @Test
    void testInsertFunctionByList_當列表為空_應該立即返回() {
        // Arrange
        List<String> emptyList = Collections.emptyList();

        // Act
        initAndCheckService.insertFunctionByList(emptyList, "");

        // Assert
        verify(functionService, never()).getFunctionByNameAndParent(anyString(), anyString());
        verify(functionService, never()).addFunction(any(FunctionVo.class));
    }

    @Test
    void testInsertFunctionByList_當功能已存在_應該遞迴處理子層() {
        // Arrange
        List<String> functionList = List.of("System", "User");
        FunctionVo existingFunction = new FunctionVo();
        existingFunction.setId("existing-id");
        existingFunction.setName("System");

        when(functionService.getFunctionByNameAndParent("System", "")).thenReturn(existingFunction);
        when(functionService.getFunctionByNameAndParent("User", "existing-id")).thenReturn(null);
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(testFunction);

        // Act
        initAndCheckService.insertFunctionByList(functionList, "");

        // Assert
        verify(functionService, times(2)).getFunctionByNameAndParent(anyString(), anyString());
        verify(functionService, times(1)).addFunction(any(FunctionVo.class));
    }

    @Test
    void testInsertFunctionByList_當功能不存在_應該建立並遞迴處理子層() {
        // Arrange
        List<String> functionList = List.of("System", "User", "View");
        when(functionService.getFunctionByNameAndParent(anyString(), anyString())).thenReturn(null);

        FunctionVo createdFunction = new FunctionVo();
        createdFunction.setId(UUID.randomUUID().toString());
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(createdFunction);

        // Act
        initAndCheckService.insertFunctionByList(functionList, "");

        // Assert
        verify(functionService, times(3)).getFunctionByNameAndParent(anyString(), anyString());
        verify(functionService, times(3)).addFunction(any(FunctionVo.class));
    }

    @Test
    void testCheckFunctionBindDefaultRole_應該建立功能並綁定到admin角色() {
        // Arrange
        FunctionVo oneLayer = new FunctionVo();
        oneLayer.setId("id1");
        FunctionVo twoLayer = new FunctionVo();
        twoLayer.setId("id2");
        FunctionVo threeLayer = new FunctionVo();
        threeLayer.setId("id3");

        when(functionService.getFunction()).thenReturn(Collections.emptyList(), List.of(oneLayer, twoLayer, threeLayer));
        when(functionService.getFunctionByName(anyString())).thenReturn(null);
        when(functionService.getFunctionByNameAndParent(anyString(), anyString())).thenReturn(null);
        
        // Create FunctionVo that will be returned by addFunction
        FunctionVo createdFunction = new FunctionVo();
        createdFunction.setId(UUID.randomUUID().toString());
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(createdFunction);
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);
        doNothing().when(roleService).roleBindFunction(anyString(), anyList());

        // Act
        initAndCheckService.checkFunctionBindDefaultRole();

        // Assert
        verify(functionService, times(2)).getFunction();
        verify(roleService, times(1)).getRoleByName("admin");
        verify(roleService, times(1)).roleBindFunction(eq(adminRole.getId().toString()), anyList());
    }

    @Test
    void testCheckFunctionBindDefaultRole_當admin角色不存在_不應該綁定功能() {
        // Arrange
        when(functionService.getFunction()).thenReturn(Collections.emptyList());
        when(functionService.getFunctionByName(anyString())).thenReturn(null);
        when(functionService.getFunctionByNameAndParent(anyString(), anyString())).thenReturn(null);
        
        // Create FunctionVo that will be returned by addFunction
        FunctionVo createdFunction = new FunctionVo();
        createdFunction.setId(UUID.randomUUID().toString());
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(createdFunction);
        when(roleService.getRoleByName("admin")).thenReturn(null);

        // Act
        initAndCheckService.checkFunctionBindDefaultRole();

        // Assert
        verify(roleService, times(1)).getRoleByName("admin");
        verify(roleService, never()).roleBindFunction(anyString(), anyList());
    }
}
