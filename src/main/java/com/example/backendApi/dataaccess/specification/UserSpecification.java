package com.example.backendApi.dataaccess.specification;

import com.example.backendApi.Dto.Vo.dto.search.UserSearchQuery;
import com.example.backendApi.Entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * User 查詢規格建構器
 */
public class UserSpecification {
    
    /**
     * 根據 UserSearchQuery 建立查詢規格
     */
    public static Specification<User> buildSpecification(UserSearchQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 名稱模糊查詢
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + query.getName().toLowerCase() + "%"
                ));
            }
            
            // 電子郵件模糊查詢
            if (query.getEmail() != null && !query.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + query.getEmail().toLowerCase() + "%"
                ));
            }
            
            // 電話號碼模糊查詢
            if (query.getPhone() != null && !query.getPhone().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("phone"),
                    "%" + query.getPhone() + "%"
                ));
            }
            
            // 停用狀態精確查詢
            if (query.getDisabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("disabled"), query.getDisabled()));
            }
            
            // 創建者精確查詢
            if (query.getCreatedBy() != null && !query.getCreatedBy().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), query.getCreatedBy()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
