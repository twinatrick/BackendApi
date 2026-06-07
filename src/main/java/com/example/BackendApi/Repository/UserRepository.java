package com.example.BackendApi.Repository;

import com.example.BackendApi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u")
    List<UUID> findAllIds();
//
//    User findByPhone(String phone);
//
//    User findByEmailAndPassword(String email, String password);
//
//    User findByPhoneAndPassword(String phone, String password);
//
//    User findByEmailOrPhone(String email, String phone);

}
