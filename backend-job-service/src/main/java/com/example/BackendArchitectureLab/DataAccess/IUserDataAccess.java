package com.example.BackendArchitectureLab.DataAccess;

import com.example.BackendArchitectureLab.Dto.Vo.Search.UserSearchQuery;
import com.example.BackendArchitectureLab.Entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserDataAccess {

    void save(User user);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID key);

    boolean existsById(UUID key);

    List<User> findAllById(List<UUID> keys);
    
    Page<User> searchUsers(UserSearchQuery query);
}
