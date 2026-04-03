package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IRoleFunctionDataAccess;
import com.example.backedapi.model.db.Function;
import com.example.backedapi.model.Vo.FunctionVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FunctionService {
    private final IFunctionDataAccess functionDataAccess;
    private final IRoleFunctionDataAccess roleFunctionDataAccess;

    public Function addFunction(Function function) {

        if (function.getId() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (function.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        } else {
            Function f = new Function();
            f.setName(function.getName());
            Example<Function> example = Example.of(f);
            if (functionDataAccess.exists(example)) {
                throw new IllegalArgumentException("Name already exists");
            }
        }

        return functionDataAccess.save(function);

    }

    public List<Function> getFunction() {
        return functionDataAccess.findAll();
    }

    public void updateFunction(Function function) {
        if (function.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (function.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        functionDataAccess.save(function);

    }

    public void deleteFunction(Function function) {
        if (function.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        roleFunctionDataAccess.deleteByFunction(function.getId());
        functionDataAccess.delete(function);

    }
    @Transactional
    public void deleteFunction(List<FunctionVo> function) {
        Date date = new Date();
        if (function.isEmpty()) {
            return;
        }
        List<Function> f= functionDataAccess.findAllById(function.stream().map(
                FunctionVo::getId
        ).map(UUID::fromString).collect(Collectors.toList()));
        roleFunctionDataAccess.deleteAllByFunctionIn(f)  ;
        functionDataAccess.deleteAll(f);
    }

@Transactional
    public void saveFunction(List<FunctionVo> function) {
        Date date = new Date();
        if (function.isEmpty()) {
            return;
        }
        List<Function> f = function.stream().map(
                functionVo -> {
                    Function temp = new Function();
                    if (functionVo.getId() != null) {
                        temp.setId(UUID.fromString(functionVo.getId()));
                    }
                    temp.setName(functionVo.getName());
                    temp.setParent(functionVo.getParent());
                    temp.setSort(functionVo.getSort());
                    temp.setType(functionVo.getType());
                    return temp;
                }
        ).collect(Collectors.toList());

        functionDataAccess.saveAll(f);
    }
    @Transactional
    public List<Function> saveFunctionNewChild(List<FunctionVo> function) {
        Date date= new Date();
        Sort sort = Sort.by(Sort.Direction.ASC, "sort");
        if (function.isEmpty()) {
            return functionDataAccess.findAll(sort) ;
        }
        List<String> GrandParentId = function.stream().map(
                FunctionVo::getGrandParentId
        ).collect(Collectors.toList());
        List<Function> saveNext = (GrandParentId.isEmpty()) ? new ArrayList<>() : functionDataAccess.findAllByGrandParentId(GrandParentId);
        List<Function> saveFunction = new ArrayList<>();
        for (FunctionVo functionVo : function) {
            for (Function f : saveNext) {
                if (f.getName().equals(functionVo.getParentName()) && f.getType() == 2 && f.getParent().equals(functionVo.getGrandParentId())) {
                    functionVo.setParent(f.getId().toString());
                    break;
                }
            }
            Function temp = new Function();
            temp.setName(functionVo.getName());
            temp.setParent(functionVo.getParent());
            temp.setSort(functionVo.getSort());
            temp.setType(3);
            saveFunction.add(temp);
        }

        if (!saveFunction.isEmpty()) {
            functionDataAccess.saveAll(saveFunction);
        }
        System.out.println("GrandParentId.size=" + GrandParentId.size() + "\n");
        System.out.println("saveFunctionNewChildTime=" + (( new Date().getTime() - date.getTime())/1000) + "\n");
        return  functionDataAccess.findAll(sort) ;
    }
        public Function getFunctionByName(String name) {
            return functionDataAccess.findFunctionByName(name);
        }

        public Function getFunctionByNameAndParent(String name, String parent) {
            List<Function> functionList = functionDataAccess.findFunctionByNameAndParent(name, parent);
            if (functionList.isEmpty()) {
                return null;
            }
            return functionList.getFirst();
        }
    }
