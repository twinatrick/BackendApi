package com.example.backendApi.dataaccess.specification;

import com.example.backendApi.Dto.dto.search.ProjectSearchQuery;
import com.example.backendApi.Entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Project 查詢規格建構器
 */
public class ProjectSpecification {
    
    /**
     * 根據 ProjectSearchQuery 建立查詢規格
     */
    public static Specification<Project> buildSpecification(ProjectSearchQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 名稱模糊查詢
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + query.getName().toLowerCase() + "%"
                ));
            }
            
            // 描述模糊查詢
            if (query.getDescription() != null && !query.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + query.getDescription().toLowerCase() + "%"
                ));
            }
            
            // 創建者精確查詢
            if (query.getCreatedBy() != null && !query.getCreatedBy().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), query.getCreatedBy()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 建立當前使用者專案查詢規格（需要透過 UserProject 關聯查詢）
     */
    public static Specification<Project> buildCurrentUserSpecification(String currentUserId, ProjectSearchQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 加入使用者專案關聯條件
            if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.join("userProjects").get("user").get("id").as(String.class), 
                    currentUserId
                ));
            }
            
            // 名稱模糊查詢
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + query.getName().toLowerCase() + "%"
                ));
            }
            
            // 描述模糊查詢
            if (query.getDescription() != null && !query.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + query.getDescription().toLowerCase() + "%"
                ));
            }
            
            // 創建者精確查詢
            if (query.getCreatedBy() != null && !query.getCreatedBy().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), query.getCreatedBy()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
