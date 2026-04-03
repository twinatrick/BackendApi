package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IFunctionService;
import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IRoleFunctionDataAccess;
import com.example.backedapi.mapper.FunctionMapper;
import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.db.Function;
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
public class FunctionService implements IFunctionService {
    private final IFunctionDataAccess functionDataAccess;
    private final IRoleFunctionDataAccess roleFunctionDataAccess;
    private final FunctionMapper functionMapper;

    @Override
    public FunctionVo addFunction(FunctionVo functionVo) {
        Function function = functionMapper.toEntity(functionVo);
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

        return functionMapper.toVo(functionDataAccess.save(function));
    }

    @Override
    public List<FunctionVo> getFunction() {
        return functionDataAccess.findAll().stream().map(functionMapper::toVo).toList();
    }

    @Override
    public void updateFunction(FunctionVo functionVo) {
        Function function = functionMapper.toEntity(functionVo);
        if (function.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (function.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        functionDataAccess.save(function);

    }

    @Override
    public void deleteFunction(FunctionVo functionVo) {
        Function function = functionMapper.toEntity(functionVo);
        if (function.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        roleFunctionDataAccess.deleteByFunction(function.getId());
        functionDataAccess.delete(function);

    }
    @Transactional
    @Override
    public void deleteFunction(List<FunctionVo> function) {
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
    @Override
    public void saveFunction(List<FunctionVo> function) {
        if (function.isEmpty()) {
            return;
        }
        List<Function> f = function.stream()
                .map(functionMapper::toEntity)
                .collect(Collectors.toList());

        functionDataAccess.saveAll(f);
    }
    @Transactional
    @Override
    public List<FunctionVo> saveFunctionNewChild(List<FunctionVo> function) {
        Date date= new Date();
        Sort sort = Sort.by(Sort.Direction.ASC, "sort");
        if (function.isEmpty()) {
            return functionDataAccess.findAll(sort).stream().map(functionMapper::toVo).toList();
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
            Function temp = functionMapper.toEntity(functionVo);
            temp.setType(3);
            saveFunction.add(temp);
        }

        if (!saveFunction.isEmpty()) {
            functionDataAccess.saveAll(saveFunction);
        }
        System.out.println("GrandParentId.size=" + GrandParentId.size() + "\n");
        System.out.println("saveFunctionNewChildTime=" + (( new Date().getTime() - date.getTime())/1000) + "\n");
        return functionDataAccess.findAll(sort).stream().map(functionMapper::toVo).toList();
    }
    @Override
    public FunctionVo getFunctionById(String id) {
        UUID uuid = mapUuid(id);
        if (uuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Function function = functionDataAccess.findById(uuid).orElseThrow(
                () -> new IllegalArgumentException("Function not found")
        );
        return functionMapper.toVo(function);
    }
        @Override
        public FunctionVo getFunctionByName(String name) {
            Function function = functionDataAccess.findFunctionByName(name);
            return function == null ? null : functionMapper.toVo(function);
        }

        @Override
        public FunctionVo getFunctionByNameAndParent(String name, String parent) {
            List<Function> functionList = functionDataAccess.findFunctionByNameAndParent(name, parent);
            if (functionList.isEmpty()) {
                return null;
            }
            return functionMapper.toVo(functionList.getFirst());
        }

        private UUID mapUuid(String id) {
            return id == null || id.isBlank() ? null : UUID.fromString(id);
        }
    }
