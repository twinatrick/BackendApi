package com.example.BackendArchitectureLab.Service;

import com.example.BackendArchitectureLab.Dto.Vo.AlertCheckLimitVo;
import com.example.BackendArchitectureLab.Dto.Vo.FunctionVo;
import com.example.BackendArchitectureLab.Dto.Vo.RoleOutVo;
import com.example.BackendArchitectureLab.Dto.Vo.UserVo;
import com.example.BackendArchitectureLab.Service.impl.InitAndCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class InitAndCheckServiceTest {

    @Mock
    private IRoleService roleService;

    @Mock
    private IAlertCheckLimitService alertCheckLimitService;

    @Mock
    private IFunctionService functionService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private InitAndCheckService initAndCheckService;

    private RoleOutVo adminRole;
    private RoleOutVo userRole;
    private FunctionVo testFunction;
    private AlertCheckLimitVo testLimit;
    private UserVo testUser;

    @BeforeEach
    void setUp() {
        // 設置測試用角色
        adminRole = new RoleOutVo();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("admin");

        userRole = new RoleOutVo();
        userRole.setId(UUID.randomUUID());
        userRole.setName("user");

        // 設置測試用使用者
        testUser = new UserVo();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail("admin");
        testUser.setPassword("admin");

        // 設置測試用功能
        testFunction = new FunctionVo();
        testFunction.setId(UUID.randomUUID().toString());
        testFunction.setName("TestFunction");
        testFunction.setParent("");

        // 設置測試用告警限制
        testLimit = new AlertCheckLimitVo(UUID.randomUUID(), "aquark_data", "rain_d", 10.0);
    }

    @Test
    @DisplayName("應該調用所有初始化方法")
    void testInitAndCheck_shouldCallAllInitializationMethods() {
        // Arrange
        when(roleService.getRole()).thenReturn(Collections.emptyList());
        when(roleService.addRole(any(RoleOutVo.class))).thenReturn(adminRole, userRole);
        when(userService.getUserByEmail("admin")).thenReturn(List.of(testUser));
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);
        doNothing().when(roleService).roleBindUser(anyString(), anyList());
        when(alertCheckLimitService.getLimit()).thenReturn(Collections.emptyList());
        when(alertCheckLimitService.insertLimit(anyString(), anyString(), anyDouble())).thenReturn(testLimit);
        when(functionService.getFunction()).thenReturn(Collections.emptyList());
        when(functionService.getFunctionByName(anyString())).thenReturn(null);
        when(functionService.getFunctionByNameAndParent(anyString(), anyString())).thenReturn(null);
        
        // Create FunctionVo that will be returned by addFunction
        FunctionVo createdFunction = new FunctionVo();
        createdFunction.setId(UUID.randomUUID().toString());
        when(functionService.addFunction(any(FunctionVo.class))).thenReturn(createdFunction);
        doNothing().when(roleService).roleBindFunction(anyString(), anyList());

        // Act
        initAndCheckService.initAndCheck();

        // Assert
        verify(roleService, atLeastOnce()).getRole();
        verify(alertCheckLimitService, atLeastOnce()).getLimit();
        verify(functionService, atLeastOnce()).getFunction();
    }

    @Test
    @DisplayName("當角色列表為空時應該初始化角色")
    void testCheckRole_shouldInitializeRoles_whenRoleListIsEmpty() {
        // Arrange
        when(roleService.getRole()).thenReturn(Collections.emptyList());
        when(roleService.addRole(any(RoleOutVo.class))).thenReturn(adminRole, userRole);
        when(userService.getUserByEmail("admin")).thenReturn(List.of(testUser));
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);
        doNothing().when(roleService).roleBindUser(anyString(), anyList());

        // Act
        initAndCheckService.checkRole();

        // Assert
        verify(roleService, times(1)).getRole();
        verify(roleService, times(2)).addRole(any(RoleOutVo.class));
    }

    @Test
    @DisplayName("當角色列表存在時不應該初始化角色")
    void testCheckRole_shouldNotInitializeRoles_whenRoleListExists() {
        // Arrange
        when(roleService.getRole()).thenReturn(List.of(adminRole, userRole));
        when(roleService.getRoleByName("admin")).thenReturn(adminRole);
        when(roleService.getRoleByName("user")).thenReturn(userRole);
        when(userService.getUserByEmail("admin")).thenReturn(List.of(testUser));
        doNothing().when(roleService).roleBindUser(anyString(), anyList());

        // Act
        initAndCheckService.checkRole();

        // Assert
        verify(roleService, times(1)).getRole();
        verify(roleService, never()).addRole(any(RoleOutVo.class));
    }

    @Test
    @DisplayName("當限制列表為空時應該初始化所有限制")
    void testCheckLimit_shouldInitializeAllLimits_whenLimitListIsEmpty() {
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
    @DisplayName("當限制列表存在但缺少某些欄位時應該補充缺少的限制")
    void testCheckLimit_shouldAddMissingLimits_whenLimitListExistsButMissingSomeFields() {
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
    @DisplayName("當三層功能都存在時應該回傳true")
    void testCheckIsExist_shouldReturnTrue_whenAllThreeLayersExist() {
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
    @DisplayName("當第一層不存在時應該回傳false")
    void testCheckIsExist_shouldReturnFalse_whenFirstLayerNotExists() {
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
    @DisplayName("當第二層不存在時應該回傳false")
    void testCheckIsExist_shouldReturnFalse_whenSecondLayerNotExists() {
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
    @DisplayName("當第三層不存在時應該回傳false")
    void testCheckIsExist_shouldReturnFalse_whenThirdLayerNotExists() {
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
    @DisplayName("當列表為空時應該立即返回")
    void testInsertFunctionByList_shouldReturnImmediately_whenListIsEmpty() {
        // Arrange
        List<String> emptyList = Collections.emptyList();

        // Act
        initAndCheckService.insertFunctionByList(emptyList, "");

        // Assert
        verify(functionService, never()).getFunctionByNameAndParent(anyString(), anyString());
        verify(functionService, never()).addFunction(any(FunctionVo.class));
    }

    @Test
    @DisplayName("當功能已存在時應該遞迴處理子層")
    void testInsertFunctionByList_shouldRecursivelyProcessChildren_whenFunctionExists() {
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
    @DisplayName("當功能不存在時應該建立並遞迴處理子層")
    void testInsertFunctionByList_shouldCreateAndRecursivelyProcessChildren_whenFunctionNotExists() {
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
    @DisplayName("應該建立功能並綁定到admin角色")
    void testCheckFunctionBindDefaultRole_shouldCreateFunctionsAndBindToAdminRole() {
        // Arrange
        FunctionVo oneLayer = new FunctionVo();
        oneLayer.setId("id1");
        FunctionVo twoLayer = new FunctionVo();
        twoLayer.setId("id2");
        FunctionVo threeLayer = new FunctionVo();
        threeLayer.setId("id3");

        when(functionService.getFunction()).thenReturn(List.of(oneLayer, twoLayer, threeLayer));
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
        verify(functionService, times(1)).getFunction();
        verify(roleService, times(1)).getRoleByName("admin");
        verify(roleService, times(1)).roleBindFunction(eq(adminRole.getId().toString()), anyList());
    }

    @Test
    @DisplayName("當admin角色不存在時不應該綁定功能")
    void testCheckFunctionBindDefaultRole_shouldNotBindFunctions_whenAdminRoleNotExists() {
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
