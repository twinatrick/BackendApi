package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IRoleFunctionDataAccess;
import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.db.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FunctionService.
 * Uses Mockito to mock IFunctionDataAccess and IRoleFunctionDataAccess dependencies.
 */
@ExtendWith(MockitoExtension.class)
class FunctionServiceTest {

    @Mock
    private IFunctionDataAccess functionDataAccess;

    @Mock
    private IRoleFunctionDataAccess roleFunctionDataAccess;

    @InjectMocks
    private FunctionService functionService;

    private Function testFunction;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testFunction = new Function();
        testFunction.setId(testId);
        testFunction.setName("Test Function");
        testFunction.setParent("parent-id");
        testFunction.setSort("1");
        testFunction.setType(1);
    }

    @Test
    void testAddFunction_Success() {
        Function newFunction = new Function();
        newFunction.setName("New Function");

        when(functionDataAccess.exists(any(Example.class))).thenReturn(false);
        when(functionDataAccess.save(newFunction)).thenReturn(newFunction);

        Function result = functionService.addFunction(newFunction);

        assertNotNull(result);
        assertEquals("New Function", result.getName());
        verify(functionDataAccess).exists(any(Example.class));
        verify(functionDataAccess).save(newFunction);
    }

    @Test
    void testAddFunction_IdNotNull_ThrowsException() {
        Function functionWithId = new Function();
        functionWithId.setId(testId);
        functionWithId.setName("Test");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.addFunction(functionWithId);
        });

        assertEquals("Key must be null", exception.getMessage());
        verify(functionDataAccess, never()).save(any());
    }

    @Test
    void testAddFunction_NameNull_ThrowsException() {
        Function functionWithoutName = new Function();
        functionWithoutName.setName(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.addFunction(functionWithoutName);
        });

        assertEquals("Name must not be null", exception.getMessage());
        verify(functionDataAccess, never()).save(any());
    }

    @Test
    void testAddFunction_NameAlreadyExists_ThrowsException() {
        Function newFunction = new Function();
        newFunction.setName("Existing Function");

        when(functionDataAccess.exists(any(Example.class))).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.addFunction(newFunction);
        });

        assertEquals("Name already exists", exception.getMessage());
        verify(functionDataAccess, never()).save(any());
    }

    @Test
    void testGetFunction() {
        List<Function> functions = Arrays.asList(testFunction, new Function());
        when(functionDataAccess.findAll()).thenReturn(functions);

        List<Function> result = functionService.getFunction();

        assertEquals(2, result.size());
        verify(functionDataAccess).findAll();
    }

    @Test
    void testUpdateFunction_Success() {
        when(functionDataAccess.save(testFunction)).thenReturn(testFunction);

        functionService.updateFunction(testFunction);

        verify(functionDataAccess).save(testFunction);
    }

    @Test
    void testUpdateFunction_IdNull_ThrowsException() {
        Function functionWithoutId = new Function();
        functionWithoutId.setName("Test");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.updateFunction(functionWithoutId);
        });

        assertEquals("Key must not be null", exception.getMessage());
        verify(functionDataAccess, never()).save(any());
    }

    @Test
    void testUpdateFunction_NameNull_ThrowsException() {
        Function functionWithoutName = new Function();
        functionWithoutName.setId(testId);
        functionWithoutName.setName(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.updateFunction(functionWithoutName);
        });

        assertEquals("Name must not be null", exception.getMessage());
        verify(functionDataAccess, never()).save(any());
    }

    @Test
    void testDeleteFunction_Success() {
        doNothing().when(roleFunctionDataAccess).deleteByFunction(testId);
        doNothing().when(functionDataAccess).delete(testFunction);

        functionService.deleteFunction(testFunction);

        verify(roleFunctionDataAccess).deleteByFunction(testId);
        verify(functionDataAccess).delete(testFunction);
    }

    @Test
    void testDeleteFunction_IdNull_ThrowsException() {
        Function functionWithoutId = new Function();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            functionService.deleteFunction(functionWithoutId);
        });

        assertEquals("Key must not be null", exception.getMessage());
        verify(roleFunctionDataAccess, never()).deleteByFunction(any());
        verify(functionDataAccess, never()).delete(any());
    }

    @Test
    void testDeleteFunction_WithList_Success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        FunctionVo vo1 = new FunctionVo();
        vo1.setId(id1.toString());
        FunctionVo vo2 = new FunctionVo();
        vo2.setId(id2.toString());
        
        List<FunctionVo> functionVos = Arrays.asList(vo1, vo2);

        Function f1 = new Function();
        f1.setId(id1);
        Function f2 = new Function();
        f2.setId(id2);
        List<Function> functions = Arrays.asList(f1, f2);

        when(functionDataAccess.findAllById(anyList())).thenReturn(functions);
        doNothing().when(roleFunctionDataAccess).deleteAllByFunctionIn(functions);
        doNothing().when(functionDataAccess).deleteAll(functions);

        functionService.deleteFunction(functionVos);

        verify(functionDataAccess).findAllById(anyList());
        verify(roleFunctionDataAccess).deleteAllByFunctionIn(functions);
        verify(functionDataAccess).deleteAll(functions);
    }

    @Test
    void testDeleteFunction_WithEmptyList_DoesNothing() {
        List<FunctionVo> emptyList = Collections.emptyList();

        functionService.deleteFunction(emptyList);

        verify(functionDataAccess, never()).findAllById(anyList());
        verify(roleFunctionDataAccess, never()).deleteAllByFunctionIn(anyList());
        verify(functionDataAccess, never()).deleteAll(anyList());
    }

    @Test
    void testSaveFunction_Success() {
        FunctionVo vo1 = new FunctionVo();
        vo1.setId(testId.toString());
        vo1.setName("Function 1");
        vo1.setParent("parent-1");
        vo1.setSort("1");
        vo1.setType(1);

        List<FunctionVo> functionVos = Collections.singletonList(vo1);

        when(functionDataAccess.saveAll(anyList())).thenReturn(Collections.emptyList());

        functionService.saveFunction(functionVos);

        ArgumentCaptor<List<Function>> captor = ArgumentCaptor.forClass(List.class);
        verify(functionDataAccess).saveAll(captor.capture());

        List<Function> savedFunctions = captor.getValue();
        assertEquals(1, savedFunctions.size());
        assertEquals("Function 1", savedFunctions.get(0).getName());
        assertEquals("parent-1", savedFunctions.get(0).getParent());
    }

    @Test
    void testSaveFunction_WithEmptyList_DoesNothing() {
        List<FunctionVo> emptyList = Collections.emptyList();

        functionService.saveFunction(emptyList);

        verify(functionDataAccess, never()).saveAll(anyList());
    }

    @Test
    void testSaveFunctionNewChild_Success() {
        FunctionVo vo1 = new FunctionVo();
        vo1.setName("Child Function");
        vo1.setParentName("Parent Function");
        vo1.setGrandParentId("grand-parent-id");
        vo1.setSort("1");

        List<FunctionVo> functionVos = Collections.singletonList(vo1);

        Function parentFunction = new Function();
        parentFunction.setId(UUID.randomUUID());
        parentFunction.setName("Parent Function");
        parentFunction.setParent("grand-parent-id");
        parentFunction.setType(2);

        List<Function> parentFunctions = Collections.singletonList(parentFunction);
        Sort sort = Sort.by(Sort.Direction.ASC, "sort");

        when(functionDataAccess.findAllByGrandParentId(anyList())).thenReturn(parentFunctions);
        when(functionDataAccess.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(functionDataAccess.findAll(sort)).thenReturn(Collections.emptyList());

        List<Function> result = functionService.saveFunctionNewChild(functionVos);

        assertNotNull(result);
        verify(functionDataAccess).findAllByGrandParentId(anyList());
        verify(functionDataAccess).saveAll(anyList());
        verify(functionDataAccess).findAll(sort);
    }

    @Test
    void testSaveFunctionNewChild_WithEmptyList_ReturnsAllFunctions() {
        List<FunctionVo> emptyList = Collections.emptyList();
        Sort sort = Sort.by(Sort.Direction.ASC, "sort");
        List<Function> allFunctions = Arrays.asList(testFunction, new Function());

        when(functionDataAccess.findAll(sort)).thenReturn(allFunctions);

        List<Function> result = functionService.saveFunctionNewChild(emptyList);

        assertEquals(2, result.size());
        verify(functionDataAccess).findAll(sort);
        verify(functionDataAccess, never()).findAllByGrandParentId(anyList());
        verify(functionDataAccess, never()).saveAll(anyList());
    }

    @Test
    void testSaveFunctionNewChild_EmptyGrandParentId_StillFetchesParents() {
        FunctionVo vo1 = new FunctionVo();
        vo1.setName("Child Function");
        vo1.setParentName("Parent Function");
        vo1.setGrandParentId("");
        vo1.setSort("1");

        List<FunctionVo> functionVos = Collections.singletonList(vo1);
        Sort sort = Sort.by(Sort.Direction.ASC, "sort");

        when(functionDataAccess.findAllByGrandParentId(anyList())).thenReturn(Collections.emptyList());
        when(functionDataAccess.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(functionDataAccess.findAll(sort)).thenReturn(Collections.emptyList());

        functionService.saveFunctionNewChild(functionVos);

        verify(functionDataAccess).findAllByGrandParentId(anyList());
        verify(functionDataAccess).saveAll(anyList());
    }

    @Test
    void testGetFunctionByName() {
        when(functionDataAccess.findFunctionByName("Test Function")).thenReturn(testFunction);

        Function result = functionService.getFunctionByName("Test Function");

        assertNotNull(result);
        assertEquals("Test Function", result.getName());
        verify(functionDataAccess).findFunctionByName("Test Function");
    }

    @Test
    void testGetFunctionByNameAndParent_Found() {
        List<Function> functions = Collections.singletonList(testFunction);
        when(functionDataAccess.findFunctionByNameAndParent("Test Function", "parent-id"))
                .thenReturn(functions);

        Function result = functionService.getFunctionByNameAndParent("Test Function", "parent-id");

        assertNotNull(result);
        assertEquals("Test Function", result.getName());
        verify(functionDataAccess).findFunctionByNameAndParent("Test Function", "parent-id");
    }

    @Test
    void testGetFunctionByNameAndParent_NotFound() {
        when(functionDataAccess.findFunctionByNameAndParent("Non-existent", "parent-id"))
                .thenReturn(Collections.emptyList());

        Function result = functionService.getFunctionByNameAndParent("Non-existent", "parent-id");

        assertNull(result);
        verify(functionDataAccess).findFunctionByNameAndParent("Non-existent", "parent-id");
    }
}
