package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IRoleDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.Vo.UserVo;
import com.example.backedapi.model.db.Function;
import com.example.backedapi.model.db.Role;
import com.example.backedapi.model.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests service layer business logic with mocked data access dependencies.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserDataAccess userDataAccess;

    @Mock
    private IRoleDataAccess roleDataAccess;

    @Mock
    private IFunctionDataAccess functionDataAccess;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private Function testFunction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setDisabled(false);

        testRole = new Role();
        testRole.setKey(UUID.randomUUID());
        testRole.setName("ROLE_USER");

        testFunction = new Function();
        testFunction.setId(UUID.randomUUID());
        testFunction.setName("TestFunction");
        testFunction.setParent("");
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser() {
        // Arrange
        doNothing().when(userDataAccess).save(any(User.class));

        // Act
        userService.createUser(testUser);

        // Assert
        verify(userDataAccess, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should return all users")
    void testGetUser() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userDataAccess.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getUser();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
        verify(userDataAccess, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return users by email")
    void testGetUserByEmail() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userDataAccess.findByEmail("test@example.com")).thenReturn(users);

        // Act
        List<User> result = userService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
        verify(userDataAccess, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should return only one user by email")
    void testGetOnlyUserByEmail() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userDataAccess.findByEmail("test@example.com")).thenReturn(users);

        // Act
        User result = userService.getOnlyUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userDataAccess, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // Arrange
        doNothing().when(userDataAccess).save(any(User.class));

        // Act
        userService.saveUser(testUser);

        // Assert
        verify(userDataAccess, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should save new user with role successfully")
    void testSaveUserWithRole_NewUser() {
        // Arrange
        UserVo userVo = new UserVo();
        userVo.setEmail("newuser@example.com");
        userVo.setPassword("plainPassword");
        userVo.setDisabled(false);
        userVo.setRoleArr(List.of(testRole.getKey().toString()));

        List<Role> roles = List.of(testRole);
        when(roleDataAccess.findRoleByKeyIn(anyList())).thenReturn(roles);
        doNothing().when(userDataAccess).save(any(User.class));
        doNothing().when(roleService).userBindRole(any(User.class), anyList());

        // Act
        userService.saveUserWithRole(userVo);

        // Assert
        verify(userDataAccess, times(1)).save(any(User.class));
        verify(roleDataAccess, times(1)).findRoleByKeyIn(anyList());
        verify(roleService, times(1)).userBindRole(any(User.class), eq(roles));
    }

    @Test
    @DisplayName("Should update existing user with role successfully")
    void testSaveUserWithRole_ExistingUser() {
        // Arrange
        UserVo userVo = new UserVo();
        userVo.setKey("existing-key");
        userVo.setEmail("test@example.com");
        userVo.setPassword("newPassword");
        userVo.setRoleArr(List.of(testRole.getKey().toString()));

        List<User> existingUsers = List.of(testUser);
        List<Role> roles = List.of(testRole);

        when(userDataAccess.findByEmail("test@example.com")).thenReturn(existingUsers);
        when(roleDataAccess.findRoleByKeyIn(anyList())).thenReturn(roles);
        doNothing().when(userDataAccess).save(any(User.class));
        doNothing().when(roleService).userUnbindAllRole(any(User.class));
        doNothing().when(roleService).userBindRole(any(User.class), anyList());

        // Act
        userService.saveUserWithRole(userVo);

        // Assert
        verify(userDataAccess, times(1)).findByEmail("test@example.com");
        verify(userDataAccess, times(1)).save(testUser);
        verify(roleDataAccess, times(1)).findRoleByKeyIn(anyList());
        verify(roleService, times(1)).userUnbindAllRole(testUser);
        verify(roleService, times(1)).userBindRole(testUser, roles);
    }

    @Test
    @DisplayName("Should get all parent functions successfully")
    void testGetAllParent() {
        // Arrange
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID grandParentId = UUID.randomUUID();

        Function childFunction = new Function();
        childFunction.setId(childId);
        childFunction.setParent(parentId.toString());

        Function parentFunction = new Function();
        parentFunction.setId(parentId);
        parentFunction.setParent(grandParentId.toString());

        Function grandParentFunction = new Function();
        grandParentFunction.setId(grandParentId);
        grandParentFunction.setParent("");

        // Mock first call - get child functions
        when(functionDataAccess.findAllById(List.of(childId)))
                .thenReturn(List.of(childFunction));

        // Mock second call - get parent functions
        when(functionDataAccess.findAllById(List.of(parentId)))
                .thenReturn(List.of(parentFunction));

        // Mock third call - get grandparent functions
        when(functionDataAccess.findAllById(argThat(list ->
                list.contains(parentId) && list.contains(grandParentId)
        ))).thenReturn(List.of(grandParentFunction));

        // Act
        List<FunctionVo> result = userService.getAllParent(List.of(childId.toString()));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(functionDataAccess, times(3)).findAllById(anyList());
    }

    @Test
    @DisplayName("Should handle empty child list in getAllParent")
    void testGetAllParent_EmptyList() {
        // Arrange
        when(functionDataAccess.findAllById(anyList())).thenReturn(new ArrayList<>());

        // Act
        List<FunctionVo> result = userService.getAllParent(new ArrayList<>());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(functionDataAccess, atLeastOnce()).findAllById(anyList());
    }

    @Test
    @DisplayName("Should handle functions without parents in getAllParent")
    void testGetAllParent_NoParents() {
        // Arrange
        UUID childId = UUID.randomUUID();
        Function childFunction = new Function();
        childFunction.setId(childId);
        childFunction.setParent(""); // No parent

        when(functionDataAccess.findAllById(List.of(childId)))
                .thenReturn(List.of(childFunction));
        when(functionDataAccess.findAllById(new ArrayList<>()))
                .thenReturn(new ArrayList<>());

        // Act
        List<FunctionVo> result = userService.getAllParent(List.of(childId.toString()));

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(functionDataAccess, times(3)).findAllById(anyList());
    }
}
