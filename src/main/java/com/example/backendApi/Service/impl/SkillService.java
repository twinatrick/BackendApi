package com.example.backendApi.Service.impl;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.SkillLevelSearchQuery;
import com.example.backendApi.Dto.Vo.dto.search.SkillSearchQuery;
import com.example.backendApi.Dto.Vo.CurrentUserSkillVo;
import com.example.backendApi.Dto.Vo.PersonalSkillRequest;
import com.example.backendApi.Dto.Vo.SkillLevelVo;
import com.example.backendApi.Dto.Vo.SkillVo;
import com.example.backendApi.Entity.Project;
import com.example.backendApi.Entity.ProjectSkill;
import com.example.backendApi.Entity.Skill;
import com.example.backendApi.Entity.SkillLevel;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserProject;
import com.example.backendApi.Entity.UserSkill;
import com.example.backendApi.Service.ISkillService;
import com.example.backendApi.Util.SortFieldValidator;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.dataaccess.ISkillDataAccess;
import com.example.backendApi.dataaccess.ISkillLevelDataAccess;
import com.example.backendApi.dataaccess.IUserDataAccess;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.dataaccess.IUserSkillDataAccess;
import com.example.backendApi.mapper.SkillMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final ISkillDataAccess skillDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IProjectDataAccess projectDataAccess;
    private final ISkillLevelDataAccess skillLevelDataAccess;
    private final IUserSkillDataAccess userSkillDataAccess;
    private final IUserProjectDataAccess userProjectDataAccess;
    private final IProjectSkillDataAccess projectSkillDataAccess;
    private final SkillMapper skillMapper;
    private final EntityManager entityManager;
    private final User currentUser;

    @Transactional
    @Override
    public SkillVo addSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        Example<Skill> example = Example.of(skill);
        if (skillDataAccess.exists(example)) {
            throw new IllegalArgumentException("Name already exists");
        }

        Skill savedSkill = skillDataAccess.save(skill);
        
        // 處理使用者綁定（如果提供了 userIds）
        if (skillVo.getUserIds() != null && !skillVo.getUserIds().isEmpty()) {
            bindUsersToSkill(savedSkill.getId(), skillVo.getUserIds(), skillVo.getSkillLevelId());
        }
        
        return skillMapper.toVo(savedSkill);
    }

    @Transactional
    @Override
    public void updateSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        skillDataAccess.save(skill);
        
        // 處理使用者重新綁定（如果提供了 userIds）
        if (skillVo.getUserIds() != null) {
            // 先刪除現有綁定
            List<UserSkill> existingBindings = userSkillDataAccess.findBySkillId(skill.getId());
            existingBindings.forEach(userSkill -> 
                userSkillDataAccess.deleteByUserIdAndSkillId(userSkill.getUser().getId(), skill.getId())
            );
            
            // 重新綁定（如果 userIds 不為空）
            if (!skillVo.getUserIds().isEmpty()) {
                bindUsersToSkill(skill.getId(), skillVo.getUserIds(), skillVo.getSkillLevelId());
            }
        }
    }
    
    /**
     * 綁定多個使用者到技能
     * 
     * @param skillId 技能 ID
     * @param userIds 使用者 ID 列表
     * @param skillLevelId 技能等級 ID（可選）
     */
    private void bindUsersToSkill(UUID skillId, List<String> userIds, String skillLevelId) {
        Skill skill = skillDataAccess.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        
        // 取得技能等級（如果提供）
        SkillLevel skillLevel = null;
        if (skillLevelId != null && !skillLevelId.isBlank()) {
            UUID levelId = UUID.fromString(skillLevelId);
            skillLevel = skillLevelDataAccess.findById(levelId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
            
            // 驗證技能等級是否屬於該技能
            if (!skillLevel.getSkill().getId().equals(skillId)) {
                throw new IllegalArgumentException("Skill level does not belong to this skill");
            }
        } else {
            // 如果沒有提供技能等級，使用該技能的第一個等級
            List<SkillLevel> levels = skillLevelDataAccess.findBySkillIdOrderByLevelValueAsc(skillId);
            if (!levels.isEmpty()) {
                skillLevel = levels.get(0);
            }
        }
        
        // 綁定每個使用者
        for (String userIdStr : userIds) {
            UUID userId = UUID.fromString(userIdStr);
            
            // 驗證使用者是否存在
            if (!userDataAccess.existsById(userId)) {
                throw new IllegalArgumentException("User not found: " + userIdStr);
            }
            
            // 檢查是否已存在綁定
            if (!userSkillDataAccess.existsByUserIdAndSkillId(userId, skillId)) {
                // 使用 EntityManager.getReference 避免 CGLIB 代理問題
                User user = entityManager.getReference(User.class, userId);
                
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkill.setSkillLevel(skillLevel);
                userSkillDataAccess.save(userSkill);
            }
        }
    }

    @Override
    public List<SkillVo> getSkill() {
        return skillDataAccess.findAll().stream().map(skillMapper::toVo).toList();
    }

    @Override
    public SkillLevelVo addSkillLevel(SkillLevelVo skillLevelVo) {
        if (skillLevelVo.getId() != null && !skillLevelVo.getId().isBlank()) {
            throw new IllegalArgumentException("Key must be null");
        }
        UUID skillId = mapUuid(skillLevelVo.getSkillId());
        if (skillId == null) {
            throw new IllegalArgumentException("Skill key must not be null");
        }
        validateLevelInput(skillLevelVo);

        Skill skill = skillDataAccess.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        if (skillLevelDataAccess.existsBySkillIdAndLevelValue(skillId, skillLevelVo.getLevelValue())) {
            throw new IllegalArgumentException("Skill level value already exists");
        }


        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setLevelValue(skillLevelVo.getLevelValue());
        skillLevel.setTitle(skillLevelVo.getTitle());
        skillLevel.setDescription(skillLevelVo.getDescription());
        skillLevel.setUserSkills(new ArrayList<>());
        return mapSkillLevelVo(skillLevelDataAccess.save(skillLevel));
    }

    @Override
    public void updateSkillLevel(SkillLevelVo skillLevelVo) {
        UUID skillLevelId = mapUuid(skillLevelVo.getId());
        if (skillLevelId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        validateLevelInput(skillLevelVo);

        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelId)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));

        List<SkillLevel> existingLevels = skillLevelDataAccess.findBySkillIdOrderByLevelValueAsc(skillLevel.getSkill().getId());
        boolean levelValueUsed = existingLevels.stream().anyMatch(item ->
                !item.getId().equals(skillLevel.getId()) && item.getLevelValue().equals(skillLevelVo.getLevelValue())
        );
        if (levelValueUsed) {
            throw new IllegalArgumentException("Skill level value already exists");
        }

        skillLevel.setLevelValue(skillLevelVo.getLevelValue());
        skillLevel.setTitle(skillLevelVo.getTitle());
        skillLevel.setDescription(skillLevelVo.getDescription());
        skillLevelDataAccess.save(skillLevel);
    }

    @Override
    public List<SkillLevelVo> getSkillLevels(String skillId) {
        UUID skillUuid = mapUuid(skillId);
        if (skillUuid == null) {
            throw new IllegalArgumentException("Skill key must not be null");
        }
        skillDataAccess.findById(skillUuid).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        return skillLevelDataAccess.findBySkillIdOrderByLevelValueAsc(skillUuid)
                .stream()
                .map(this::mapSkillLevelVo)
                .toList();
    }

    @Override
    public void deleteSkillLevel(String skillLevelId) {
        UUID skillLevelUuid = mapUuid(skillLevelId);
        if (skillLevelUuid == null) {
            throw new IllegalArgumentException("Skill level key must not be null");
        }
        if (userSkillDataAccess.existsBySkillLevelId(skillLevelUuid)
                || projectSkillDataAccess.existsBySkillLevelId(skillLevelUuid)) {
            throw new IllegalArgumentException("Skill level is already in use");
        }
        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelUuid)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
        skillLevelDataAccess.delete(skillLevel);
    }

    @Override
    public void bindUserSkill(String userId, String skillId, String skillLevelId) {
        UUID userUuid = mapUuid(userId);
        UUID skillUuid = mapUuid(skillId);
        UUID skillLevelUuid = mapUuid(skillLevelId);
        if (userUuid == null || skillUuid == null || skillLevelUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        User user = userDataAccess.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Skill skill = skillDataAccess.findById(skillUuid)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelUuid)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
        validateSkillLevelBelongsToSkill(skillLevel, skill);

        if (userSkillDataAccess.existsByUserIdAndSkillId(userUuid, skillUuid)) {
            throw new IllegalArgumentException("Skill already bind to user");
        }

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setSkillLevel(skillLevel);
        userSkillDataAccess.save(userSkill);
    }

    @Override
    public void bindProjectSkill(String projectId, String skillId, String skillLevelId) {
        UUID projectUuid = mapUuid(projectId);
        UUID skillUuid = mapUuid(skillId);
        UUID skillLevelUuid = mapUuid(skillLevelId);
        if (projectUuid == null || skillUuid == null || skillLevelUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        Project project = projectDataAccess.findById(projectUuid)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Skill skill = skillDataAccess.findById(skillUuid)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelUuid)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
        validateSkillLevelBelongsToSkill(skillLevel, skill);

        if (projectSkillDataAccess.existsByProjectIdAndSkillId(projectUuid, skillUuid)) {
            throw new IllegalArgumentException("Skill already bind to project");
        }

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);
        projectSkill.setSkill(skill);
        projectSkill.setSkillLevel(skillLevel);
        projectSkillDataAccess.save(projectSkill);
    }

    @Transactional
    @Override
    public void deleteSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        skill = skillDataAccess.findById(skill.getId()).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        userSkillDataAccess.deleteBySkillId(skill.getId());
        projectSkillDataAccess.deleteBySkillId(skill.getId());
        skillLevelDataAccess.deleteBySkillId(skill.getId());
        skillDataAccess.delete(skill);
    }
    
    @Override
    public PageResult<SkillVo> searchSkills(SkillSearchQuery query) {
        // 驗證排序欄位
        Set<String> validSortFields = Set.of("id", "name", "description", "createdBy", "updatedBy", "createdTime", "updatedTime");
        SortFieldValidator.validate(query.getSortBy(), query.getSortDir(), validSortFields);
        
        // 執行分頁查詢
        Page<Skill> page = skillDataAccess.searchSkills(query);
        
        // 轉換為 VO
        List<SkillVo> content = page.getContent().stream()
                .map(skillMapper::toVo)
                .collect(Collectors.toList());
        
        return PageResult.of(page, content);
    }
    
    @Override
    public List<CurrentUserSkillVo> getCurrentUserSkills() {
        List<CurrentUserSkillVo> result = new ArrayList<>();
        Set<UUID> seenSkillIds = new HashSet<>();
        
        // 1. 取得 USER 直接綁定的技能
        List<UserSkill> userSkills = userSkillDataAccess.findByUserId(currentUser.getId());
        for (UserSkill us : userSkills) {
            Skill skill = us.getSkill();
            result.add(CurrentUserSkillVo.fromSkillVo(skillMapper.toVo(skill)));
            seenSkillIds.add(skill.getId());
        }
        
        // 2. 取得 PROJECT 專案的技能
        List<UserProject> userProjects = userProjectDataAccess.findByUserId(currentUser.getId());
        for (UserProject up : userProjects) {
            Project project = up.getProject();
            for (ProjectSkill ps : project.getProjectSkills()) {
                Skill skill = ps.getSkill();
                if (!seenSkillIds.contains(skill.getId())) {
                    result.add(CurrentUserSkillVo.fromSkillVoWithProject(
                        skillMapper.toVo(skill),
                        project.getId(),
                        project.getName()
                    ));
                    seenSkillIds.add(skill.getId());
                }
            }
        }
        
        return result;
    }
    
    @Override
    public PageResult<CurrentUserSkillVo> searchCurrentUserSkills(SkillSearchQuery query) {
        // 驗證排序欄位
        Set<String> validSortFields = Set.of("id", "name", "description", "createdBy", "updatedBy", "createdTime", "updatedTime");
        SortFieldValidator.validate(query.getSortBy(), query.getSortDir(), validSortFields);
        
        // 先取得所有當前使用者技能（已合併）
        List<CurrentUserSkillVo> allSkills = getCurrentUserSkills();
        
        // 套用搜尋條件
        List<CurrentUserSkillVo> filteredSkills = allSkills.stream()
                .filter(skill -> matchesQuery(skill, query))
                .collect(Collectors.toList());
        
        // 套用排序
        filteredSkills = applySorting(filteredSkills, query);
        
        // 套用分頁
        int start = query.getPage() * query.getSize();
        int end = Math.min(start + query.getSize(), filteredSkills.size());
        List<CurrentUserSkillVo> pageContent = (start < filteredSkills.size()) 
                ? filteredSkills.subList(start, end) 
                : new ArrayList<>();
        
        // 建立分頁結果
        return PageResult.<CurrentUserSkillVo>builder()
                .content(pageContent)
                .totalElements((long) filteredSkills.size())
                .totalPages((int) Math.ceil((double) filteredSkills.size() / query.getSize()))
                .currentPage(query.getPage())
                .pageSize(query.getSize())
                .hasNext(end < filteredSkills.size())
                .hasPrevious(query.getPage() > 0)
                .isFirst(query.getPage() == 0)
                .isLast(end >= filteredSkills.size())
                .build();
    }
    
    private boolean matchesQuery(CurrentUserSkillVo skill, SkillSearchQuery query) {
        if (query.getName() != null && !query.getName().trim().isEmpty()) {
            if (skill.getName() == null || !skill.getName().toLowerCase().contains(query.getName().toLowerCase())) {
                return false;
            }
        }
        
        if (query.getDescription() != null && !query.getDescription().trim().isEmpty()) {
            if (skill.getDescription() == null || !skill.getDescription().toLowerCase().contains(query.getDescription().toLowerCase())) {
                return false;
            }
        }
        
        if (query.getCreatedBy() != null && !query.getCreatedBy().trim().isEmpty()) {
            if (!query.getCreatedBy().equals(skill.getCreatedBy())) {
                return false;
            }
        }
        
        return true;
    }
    
    private List<CurrentUserSkillVo> applySorting(List<CurrentUserSkillVo> skills, SkillSearchQuery query) {
        boolean ascending = "asc".equalsIgnoreCase(query.getSortDir());
        
        return skills.stream()
                .sorted((s1, s2) -> {
                    int comparison = 0;
                    switch (query.getSortBy()) {
                        case "name":
                            comparison = compareNullable(s1.getName(), s2.getName());
                            break;
                        case "description":
                            comparison = compareNullable(s1.getDescription(), s2.getDescription());
                            break;
                        case "createdBy":
                            comparison = compareNullable(s1.getCreatedBy(), s2.getCreatedBy());
                            break;
                        case "updatedBy":
                            comparison = compareNullable(s1.getUpdatedBy(), s2.getUpdatedBy());
                            break;
                        case "createdTime":
                            comparison = compareNullable(s1.getCreatedTime(), s2.getCreatedTime());
                            break;
                        case "updatedTime":
                            comparison = compareNullable(s1.getUpdatedTime(), s2.getUpdatedTime());
                            break;
                        case "id":
                        default:
                            comparison = compareNullable(s1.getId(), s2.getId());
                            break;
                    }
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<SkillLevelVo> searchSkillLevels(SkillLevelSearchQuery query) {
        // 驗證排序欄位
        Set<String> validSortFields = Set.of("id", "levelValue", "title", "description", "createdBy", "updatedBy", "createdTime", "updatedTime");
        SortFieldValidator.validate(query.getSortBy(), query.getSortDir(), validSortFields);
        
        // 執行分頁查詢
        Page<SkillLevel> page = skillLevelDataAccess.searchSkillLevels(query);
        
        // 轉換為 VO
        List<SkillLevelVo> content = page.getContent().stream()
                .map(this::mapSkillLevelVo)
                .collect(Collectors.toList());
        
        return PageResult.of(page, content);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> int compareNullable(T o1, T o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return o1.compareTo(o2);
    }

    private SkillLevelVo mapSkillLevelVo(SkillLevel skillLevel) {
        SkillLevelVo vo = new SkillLevelVo();
        vo.setId(skillLevel.getId() == null ? null : skillLevel.getId().toString());
        vo.setSkillId(skillLevel.getSkill() == null ? null : skillLevel.getSkill().getId().toString());
        vo.setLevelValue(skillLevel.getLevelValue());
        vo.setTitle(skillLevel.getTitle());
        vo.setDescription(skillLevel.getDescription());
        vo.setCreatedBy(skillLevel.getCreatedBy());
        vo.setUpdatedBy(skillLevel.getUpdatedBy());
        vo.setCreatedTime(skillLevel.getCreatedTime());
        vo.setUpdatedTime(skillLevel.getUpdatedTime());
        return vo;
    }

    private void validateLevelInput(SkillLevelVo skillLevelVo) {
        if (skillLevelVo.getLevelValue() == null || skillLevelVo.getLevelValue() < 1) {
            throw new IllegalArgumentException("Level value must be greater than 0");
        }
        if (skillLevelVo.getTitle() == null || skillLevelVo.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title must not be null");
        }
    }

    private void validateSkillLevelBelongsToSkill(SkillLevel skillLevel, Skill skill) {
        if (!skillLevel.getSkill().getId().equals(skill.getId())) {
            throw new IllegalArgumentException("Skill level does not belong to skill");
        }
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
    
    @Transactional
    @Override
    public SkillVo addPersonalSkill(PersonalSkillRequest request) {
        // 驗證輸入
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null");
        }
        
        // 檢查名稱是否已存在
        Skill existingSkill = new Skill();
        existingSkill.setName(request.getName());
        Example<Skill> example = Example.of(existingSkill);
        if (skillDataAccess.exists(example)) {
            throw new IllegalArgumentException("Name already exists");
        }
        
        // 建立技能
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        Skill savedSkill = skillDataAccess.save(skill);
        
        // 自動綁定當前使用者
        UserSkill userSkill = new UserSkill();
        userSkill.setUser(currentUser);
        userSkill.setSkill(savedSkill);
        
        // 如果提供了技能等級，綁定該等級
        if (request.getSkillLevelId() != null && !request.getSkillLevelId().trim().isEmpty()) {
            UUID skillLevelId = UUID.fromString(request.getSkillLevelId());
            SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
            
            // 驗證技能等級屬於該技能
            if (!skillLevel.getSkill().getId().equals(savedSkill.getId())) {
                throw new IllegalArgumentException("Skill level does not belong to skill");
            }
            
            userSkill.setSkillLevel(skillLevel);
        } else {
            // 如果沒提供等級，使用該技能的第一個等級（如果有的話）
            List<SkillLevel> levels = skillLevelDataAccess.findBySkillIdOrderByLevelValueAsc(savedSkill.getId());
            if (!levels.isEmpty()) {
                userSkill.setSkillLevel(levels.get(0));
            } else {
                throw new IllegalArgumentException("Skill must have at least one level");
            }
        }
        
        userSkillDataAccess.save(userSkill);
        
        return skillMapper.toVo(savedSkill);
    }
    
    @Transactional
    @Override
    public void updatePersonalSkill(UUID skillId, PersonalSkillRequest request) {
        // 驗證輸入
        if (skillId == null) {
            throw new IllegalArgumentException("Skill ID must not be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null");
        }
        
        // 查找技能
        Skill skill = skillDataAccess.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        
        // 驗證是否為擁有者（檢查是否有綁定關係）
        if (!userSkillDataAccess.existsByUserIdAndSkillId(currentUser.getId(), skillId)) {
            throw new IllegalArgumentException("You are not the owner of this skill");
        }

        if (!canEditContent(skill.getCreatedBy(), currentUser.getId())) {
            throw new IllegalArgumentException("Skill assigned by admin is read-only");
        }
        
        // 更新技能資訊
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skillDataAccess.save(skill);
        
        // 如果提供了新的技能等級，更新綁定的等級
        if (request.getSkillLevelId() != null && !request.getSkillLevelId().trim().isEmpty()) {
            UUID skillLevelId = UUID.fromString(request.getSkillLevelId());
            SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
            
            // 驗證技能等級屬於該技能
            if (!skillLevel.getSkill().getId().equals(skillId)) {
                throw new IllegalArgumentException("Skill level does not belong to skill");
            }
            
            // 更新 UserSkill 的等級
            List<UserSkill> userSkills = userSkillDataAccess.findByUserIdAndSkillId(currentUser.getId(), skillId);
            if (!userSkills.isEmpty()) {
                UserSkill userSkill = userSkills.get(0);
                userSkill.setSkillLevel(skillLevel);
                userSkillDataAccess.save(userSkill);
            }
        }
    }

    @Transactional
    @Override
    public void updatePersonalSkillLevel(UUID skillId, UUID skillLevelId) {
        if (skillId == null || skillLevelId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        Skill skill = skillDataAccess.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        if (!userSkillDataAccess.existsByUserIdAndSkillId(currentUser.getId(), skillId)) {
            throw new IllegalArgumentException("Skill is not bind to current user");
        }

        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelId)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
        validateSkillLevelBelongsToSkill(skillLevel, skill);

        List<UserSkill> userSkills = userSkillDataAccess.findByUserIdAndSkillId(currentUser.getId(), skillId);
        if (userSkills.isEmpty()) {
            throw new IllegalArgumentException("Skill is not bind to current user");
        }

        UserSkill userSkill = userSkills.get(0);
        userSkill.setSkillLevel(skillLevel);
        userSkillDataAccess.save(userSkill);
    }
    
    @Transactional
    @Override
    public void deletePersonalSkill(UUID skillId) {
        // 驗證輸入
        if (skillId == null) {
            throw new IllegalArgumentException("Skill ID must not be null");
        }
        
        // 查找技能
        Skill skill = skillDataAccess.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        
        // 驗證是否為擁有者
        if (!userSkillDataAccess.existsByUserIdAndSkillId(currentUser.getId(), skillId)) {
            throw new IllegalArgumentException("You are not the owner of this skill");
        }

        // 刪除當前使用者與技能的綁定
        userSkillDataAccess.deleteByUserIdAndSkillId(currentUser.getId(), skillId);
    }

    private boolean canEditContent(String createdBy, UUID currentUserId) {
        if (createdBy == null || createdBy.isBlank()) {
            return true;
        }
        return createdBy.equals(currentUserId.toString());
    }
}
