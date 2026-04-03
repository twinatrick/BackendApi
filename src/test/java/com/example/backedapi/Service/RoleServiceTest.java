package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.*;
import com.example.backedapi.model.Vo.RoleOutVo;
import com.example.backedapi.model.db.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService.
 * Tests service layer business logic with mocked data access dependencies.
 * Target: 85%+ coverage
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private IRoleDataAccess roleDataAccess;

    @Mock
    private IRoleFunctionDataAccess roleFunctionDataAccess;

    @Mock
    private IFunctionDataAccess functionDataAccess;

    @Mock
    private IUserDataAccess userDataAccess;

    @Mock
    private IUserRoleDataAccess userRoleDataAccess;

    @Mock
    private User currentUser;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private Function testFunction;
    private User testUser;
    private UUID testRoleKey;
    private UUID testFunctionId;
    private UUID testUserKey;

    @BeforeEach
    void setUp() {
        // Inject mocked currentUser into RoleService using ReflectionTestUtils
        ReflectionTestUtils.setField(roleService, "currentUser", currentUser);
        
        // Mock currentUser behavior with lenient() to avoid UnnecessaryStubbingException
        lenient().when(currentUser.getEmail()).thenReturn("admin@example.com");
        lenient().when(currentUser.getKey()).thenReturn(UUID.randomUUID());

        // Setup test role
        testRoleKey = UUID.randomUUID();
        testRole = new Role();
        testRole.setKey(testRoleKey);
        testRole.setName("ROLE_ADMIN");
        testRole.setDescription("Admin role");

        // Setup test function
        testFunctionId = UUID.randomUUID();
        testFunction = new Function();
        testFunction.setId(testFunctionId);
        testFunction.setName("TestFunction");
        testFunction.setParent("");

        // Setup test user
        testUserKey = UUID.randomUUID();
        testUser = new User();
        testUser.setKey(testUserKey);
        testUser.setEmail("user@example.com");
    }

    // ==================== Group 1: CRUD Operations ====================

    @Test
    @DisplayName("Should add role successfully")
    void testAddRole_Success() {
        // Arrange
        Role newRole = new Role();
        newRole.setName("ROLE_USER");
        
        when(roleDataAccess.exists(any(Example.class))).thenReturn(false);
        when(roleDataAccess.save(any(Role.class))).thenReturn(newRole);

        // Act
        Role result = roleService.addRole(newRole);

        // Assert
        assertNotNull(result);
        verify(roleDataAccess, times(1)).exists(any(Example.class));
        verify(roleDataAccess, times(1)).save(newRole);
    }

    @Test
    @DisplayName("Should throw exception when adding role with non-null key")
    void testAddRole_KeyNotNull() {
        // Arrange
        Role newRole = new Role();
        newRole.setKey(UUID.randomUUID());
        newRole.setName("ROLE_USER");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> roleService.addRole(newRole));
        assertEquals("Key must be null", exception.getMessage());
        verify(roleDataAccess, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when adding role with null name")
    void testAddRole_NameIsNull() {
        // Arrange
        Role newRole = new Role();
        newRole.setName(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> roleService.addRole(newRole));
        assertEquals("Name must not be null", exception.getMessage());
        verify(roleDataAccess, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when adding role with duplicate name")
    void testAddRole_NameAlreadyExists() {
        // Arrange
        Role newRole = new Role();
        newRole.setName("ROLE_USER");
        
        when(roleDataAccess.exists(any(Example.class))).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> roleService.addRole(newRole));
        assertEquals("Name already exists", exception.getMessage());
        verify(roleDataAccess, never()).save(any());
    }

    @Test
    @DisplayName("Should get all roles as RoleOutVo")
    void testGetRole() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        when(roleDataAccess.findAll()).thenReturn(roles);

        // Act
        List<RoleOutVo> result = roleService.getRole();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roleDataAccess, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get all roles as Role entities")
    void testGetRoleRestIn() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        when(roleDataAccess.findAll()).thenReturn(roles);

        // Act
        List<Role> result = roleService.getRoleRestIn();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getName(), result.get(0).getName());
        verify(roleDataAccess, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update role successfully")
    void testUpdateRole() {
        // Arrange
        Role updateRole = new Role();
        updateRole.setKey(testRoleKey);
        updateRole.setName("ROLE_ADMIN");
        updateRole.setDescription("Updated description");
        
        when(roleDataAccess.findById(testRoleKey)).thenReturn(Optional.of(testRole));
        when(roleDataAccess.save(any(Role.class))).thenReturn(testRole);

        // Act
        roleService.updateRole(updateRole);

        // Assert
        verify(roleDataAccess, times(1)).findById(testRoleKey);
        verify(roleDataAccess, times(1)).save(any(Role.class));
        assertEquals("Updated description", testRole.getDescription());
        assertEquals("admin@example.com", testRole.getUpdatedBy());
    }

    @Test
    @DisplayName("Should delete role and its associated functions")
    void testDeleteRole() {
        // Arrange
        Role roleToDelete = new Role();
        roleToDelete.setKey(testRoleKey);
        
        when(roleDataAccess.findById(testRoleKey)).thenReturn(Optional.of(testRole));
        when(roleFunctionDataAccess.findAll(any(Example.class))).thenReturn(new ArrayList<>());
        doNothing().when(roleFunctionDataAccess).deleteAll(anyList());
        doNothing().when(roleDataAccess).delete(any(Role.class));

        // Act
        roleService.deleteRole(roleToDelete);

        // Assert
        verify(roleDataAccess, times(1)).findById(testRoleKey);
        verify(roleFunctionDataAccess, times(1)).findAll(any(Example.class));
        verify(roleDataAccess, times(1)).delete(testRole);
    }

    // ==================== Group 2: Role-Function Binding Operations ====================

    @Test
    @DisplayName("Should bind functions to role")
    void testRoleBindFunction() {
        // Arrange
        List<Function> functions = Arrays.asList(testFunction);
        
        when(roleDataAccess.findById(testRoleKey)).thenReturn(Optional.of(testRole));
        when(functionDataAccess.findAllById(anyList())).thenReturn(functions);
        doNothing().when(roleFunctionDataAccess).deleteByFunctionAndRole(anyList(), anyList());
        when(roleFunctionDataAccess.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        roleService.roleBindFunction(testRole, functions);

        // Assert
        verify(roleDataAccess, times(1)).findById(testRoleKey);
        verify(roleFunctionDataAccess, times(1)).deleteByFunctionAndRole(anyList(), anyList());
        verify(functionDataAccess, times(1)).findAllById(anyList());
        verify(roleFunctionDataAccess, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should bind roles to function")
    void testFunctionBindRole() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        
        doNothing().when(roleFunctionDataAccess).deleteByFunctionAndRole(anyList(), anyList());
        when(roleFunctionDataAccess.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        roleService.functionBindRole(testFunction, roles);

        // Assert
        verify(roleFunctionDataAccess, times(1)).deleteByFunctionAndRole(anyList(), anyList());
        verify(roleFunctionDataAccess, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should unbind functions from role")
    void testRoleUnbindFunction() {
        // Arrange
        List<Function> functions = Arrays.asList(testFunction);
        
        doNothing().when(roleFunctionDataAccess).deleteByFunctionAndRole(anyList(), anyList());

        // Act
        roleService.roleUnbindFunction(testRole, functions);

        // Assert
        verify(roleFunctionDataAccess, times(1)).deleteByFunctionAndRole(functions, List.of(testRole));
    }

    @Test
    @DisplayName("Should unbind roles from function")
    void testFunctionUnbindRole() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        
        doNothing().when(roleFunctionDataAccess).deleteByFunctionAndRole(anyList(), anyList());

        // Act
        roleService.functionUnbindRole(testFunction, roles);

        // Assert
        verify(roleFunctionDataAccess, times(1)).deleteByFunctionAndRole(List.of(testFunction), roles);
    }

    // ==================== Group 3: Role-User Binding Operations ====================

    @Test
    @DisplayName("Should bind users to role")
    void testRoleBindingUser() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        
        doNothing().when(userRoleDataAccess).deleteAllByUserInAndRoleIn(anyList(), anyList());
        when(userRoleDataAccess.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        roleService.roleBindingUser(testRole, users);

        // Assert
        verify(userRoleDataAccess, times(1)).deleteAllByUserInAndRoleIn(users, List.of(testRole));
        verify(userRoleDataAccess, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should bind roles to user")
    void testUserBindRole() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        
        doNothing().when(userRoleDataAccess).deleteAllByUserInAndRoleIn(anyList(), anyList());
        when(userRoleDataAccess.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        roleService.userBindRole(testUser, roles);

        // Assert
        verify(userRoleDataAccess, times(1)).deleteAllByUserInAndRoleIn(List.of(testUser), roles);
        verify(userRoleDataAccess, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should unbind users from role")
    void testRoleUnbindUser() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        
        doNothing().when(userRoleDataAccess).deleteAllByUserInAndRoleIn(anyList(), anyList());

        // Act
        roleService.roleUnbindUser(testRole, users);

        // Assert
        verify(userRoleDataAccess, times(1)).deleteAllByUserInAndRoleIn(users, List.of(testRole));
    }

    @Test
    @DisplayName("Should unbind roles from user")
    void testUserUnbindRole() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        
        doNothing().when(userRoleDataAccess).deleteAllByUserInAndRoleIn(anyList(), anyList());

        // Act
        roleService.userUnbindRole(testUser, roles);

        // Assert
        verify(userRoleDataAccess, times(1)).deleteAllByUserInAndRoleIn(List.of(testUser), roles);
    }

    @Test
    @DisplayName("Should unbind all roles from user")
    void testUserUnbindAllRole() {
        // Arrange
        List<Role> allRoles = Arrays.asList(testRole);
        
        when(roleDataAccess.findAll()).thenReturn(allRoles);
        doNothing().when(userRoleDataAccess).deleteAllByUserInAndRoleIn(anyList(), anyList());

        // Act
        roleService.userUnbindAllRole(testUser);

        // Assert
        verify(roleDataAccess, times(1)).findAll();
        verify(userRoleDataAccess, times(1)).deleteAllByUserInAndRoleIn(List.of(testUser), allRoles);
    }

    // ==================== Group 4: Query Operations ====================

    @Test
    @DisplayName("Should get functions by role")
    void testGetFunctionByRole() {
        // Arrange
        RoleFunction roleFunction = new RoleFunction();
        roleFunction.setRole(testRole);
        roleFunction.setFunction(testFunction);
        
        List<RoleFunction> roleFunctions = Arrays.asList(roleFunction);
        testRole.setRoleFunctions(roleFunctions);
        
        when(roleDataAccess.findById(testRoleKey)).thenReturn(Optional.of(testRole));

        // Act
        List<Function> result = roleService.getFunctionByRole(testRole);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFunction.getName(), result.get(0).getName());
        verify(roleDataAccess, times(1)).findById(testRoleKey);
    }

    @Test
    @DisplayName("Should get roles by function")
    void testGetRoleByFunction() {
        // Arrange
        RoleFunction roleFunction = new RoleFunction();
        roleFunction.setRole(testRole);
        roleFunction.setFunction(testFunction);
        
        List<RoleFunction> roleFunctions = Arrays.asList(roleFunction);
        testFunction.setRoleFunctions(roleFunctions);
        
        when(functionDataAccess.findById(testFunctionId)).thenReturn(Optional.of(testFunction));

        // Act
        List<Role> result = roleService.getRoleByFunction(testFunction);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getName(), result.get(0).getName());
        verify(functionDataAccess, times(1)).findById(testFunctionId);
    }

    @Test
    @DisplayName("Should get users by role")
    void testGetUserByRole() {
        // Arrange
        UserRole userRole = new UserRole();
        userRole.setRole(testRole);
        userRole.setUser(testUser);
        
        List<UserRole> userRoles = Arrays.asList(userRole);
        testRole.setUserRoles(userRoles);
        
        when(roleDataAccess.findById(testRoleKey)).thenReturn(Optional.of(testRole));

        // Act
        List<User> result = roleService.getUserByRole(testRole);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
        verify(roleDataAccess, times(1)).findById(testRoleKey);
    }

    @Test
    @DisplayName("Should get roles by user")
    void testGetRoleByUser() {
        // Arrange
        UserRole userRole = new UserRole();
        userRole.setRole(testRole);
        userRole.setUser(testUser);
        
        List<UserRole> roles = Arrays.asList(userRole);
        testUser.setRoles(roles);
        
        when(userDataAccess.findById(testUserKey)).thenReturn(Optional.of(testUser));

        // Act
        List<Role> result = roleService.getRoleByUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getName(), result.get(0).getName());
        verify(userDataAccess, times(1)).findById(testUserKey);
    }

    @Test
    @DisplayName("Should get role by name")
    void testGetRoleByName() {
        // Arrange
        when(roleDataAccess.findRoleByName("ROLE_ADMIN")).thenReturn(testRole);

        // Act
        Role result = roleService.getRoleByName("ROLE_ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getName());
        verify(roleDataAccess, times(1)).findRoleByName("ROLE_ADMIN");
    }
}
