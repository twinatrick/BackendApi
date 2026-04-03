package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.Service.IRoleService;
import com.example.backedapi.Service.impl.UserService;
import com.example.backedapi.mapper.FunctionMapper;
import com.example.backedapi.mapper.UserMapper;
import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.Vo.UserVo;
import com.example.backedapi.model.db.Function;
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
import static org.mockito.ArgumentMatchers.anyString;
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
    private IFunctionDataAccess functionDataAccess;

    @Mock
    private User currentUser;

    @Mock
    private IRoleService roleService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FunctionMapper functionMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Function testFunction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setDisabled(false);

        testFunction = new Function();
        testFunction.setId(UUID.randomUUID());
        testFunction.setName("TestFunction");
        testFunction.setParent("");

        when(userMapper.toEntity(any(UserVo.class))).thenAnswer(invocation -> {
            UserVo vo = invocation.getArgument(0);
            User user = new User();
            if (vo.getId() != null && !vo.getId().isBlank()) {
                user.setId(UUID.fromString(vo.getId()));
            }
            user.setEmail(vo.getEmail());
            user.setPassword(vo.getPassword());
            user.setDisabled(vo.isDisabled());
            return user;
        });
        when(userMapper.toVo(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            UserVo vo = new UserVo();
            if (user.getId() != null) {
                vo.setId(user.getId().toString());
            }
            vo.setEmail(user.getEmail());
            vo.setPassword(user.getPassword());
            vo.setDisabled(user.isDisabled());
            return vo;
        });
        when(functionMapper.toVo(any(Function.class))).thenAnswer(invocation -> {
            Function function = invocation.getArgument(0);
            FunctionVo vo = new FunctionVo();
            vo.setId(function.getId() == null ? null : function.getId().toString());
            vo.setName(function.getName());
            vo.setParent(function.getParent());
            return vo;
        });
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser() {
        // Arrange
        doNothing().when(userDataAccess).save(any(User.class));
        UserVo userVo = new UserVo();
        userVo.setEmail("test@example.com");
        userVo.setPassword("plainPassword");

        // Act
        UserVo result = userService.createUser(userVo);

        // Assert
        assertNotNull(result);
        verify(userDataAccess, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should return all users")
    void testGetUser() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userDataAccess.findAll()).thenReturn(users);

        // Act
        List<UserVo> result = userService.getUser();

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
        List<UserVo> result = userService.getUserByEmail("test@example.com");

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
        UserVo result = userService.getOnlyUserByEmail("test@example.com");

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
        UserVo userVo = new UserVo();
        userVo.setEmail("test@example.com");
        userVo.setPassword("plainPassword");

        // Act
        userService.saveUser(userVo);

        // Assert
        verify(userDataAccess, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should save new user with role successfully")
    void testSaveUserWithRole_NewUser() {
        // Arrange
        UserVo userVo = new UserVo();
        userVo.setEmail("newuser@example.com");
        userVo.setPassword("plainPassword");
        userVo.setDisabled(false);
        userVo.setRoleArr(List.of(UUID.randomUUID().toString()));

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return null;
        }).when(userDataAccess).save(any(User.class));
        doNothing().when(roleService).userBindRole(anyString(), anyList());

        // Act
        userService.saveUserWithRole(userVo);

        // Assert
        verify(userDataAccess, times(1)).save(any(User.class));
        verify(roleService, times(1)).userBindRole(anyString(), anyList());
    }

    @Test
    @DisplayName("Should update existing user with role successfully")
    void testSaveUserWithRole_ExistingUser() {
        // Arrange
        UserVo userVo = new UserVo();
        userVo.setId("existing-id");
        userVo.setEmail("test@example.com");
        userVo.setPassword("newPassword");
        userVo.setRoleArr(List.of(UUID.randomUUID().toString()));

        List<User> existingUsers = List.of(testUser);
        when(userDataAccess.findByEmail("test@example.com")).thenReturn(existingUsers);
        doNothing().when(userDataAccess).save(any(User.class));
        doNothing().when(roleService).userUnbindAllRole(anyString());
        doNothing().when(roleService).userBindRole(anyString(), anyList());

        // Act
        userService.saveUserWithRole(userVo);

        // Assert
        verify(userDataAccess, times(1)).findByEmail("test@example.com");
        verify(userDataAccess, times(1)).save(testUser);
        verify(roleService, times(1)).userUnbindAllRole(anyString());
        verify(roleService, times(1)).userBindRole(anyString(), anyList());
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
