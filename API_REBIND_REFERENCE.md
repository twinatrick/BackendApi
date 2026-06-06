# API Rebind 完整參考文件

## 概述
本文件說明所有管理者綁定 API 的使用方式，採用完整覆蓋 (Complete Replacement) 語意。

---

## 🔑 權限需求

### UserController APIs
- **權限**: `System`, `ProjectManagement`, `Edit`
- **適用範圍**: 使用者相關的綁定操作

### AdminBindingController APIs  
- **權限**: `System`, `ProjectManagement`, `EditAll`
- **適用範圍**: 管理者統一管理的綁定操作

---

## 📍 API 端點總覽

| Method | Route | Description | Controller |
|--------|-------|-------------|------------|
| POST | `/users/{userId}/roles/rebind` | 使用者角色完整覆蓋 | UserController |
| POST | `/admin/bindings/user-project/rebind` | 使用者專案完整覆蓋 | AdminBindingController |
| POST | `/admin/bindings/user-skill/rebind` | 使用者技能完整覆蓋 | AdminBindingController |
| POST | `/admin/bindings/project-skill/rebind` | 專案技能需求完整覆蓋 | AdminBindingController |
| POST | `/admin/bindings/project-members-skills/rebind` | 專案成員技能完整覆蓋 | AdminBindingController |

---

## 📋 API 詳細說明

### 1. 使用者角色完整覆蓋
**Endpoint**: `POST /users/{userId}/roles/rebind`  
**Controller**: `UserController`

#### Request Body
```json
{
  "userId": "uuid",
  "roleIds": ["roleId1", "roleId2"]
}
```

#### 語意
- `roleIds = []`: 清空該使用者的所有角色
- `roleIds = null`: 拋出異常
- 完整覆蓋：刪除不在清單中的角色，保留清單中的角色

#### Response
```json
{
  "success": true,
  "data": "User roles rebound successfully",
  "message": null
}
```

#### 取代的舊 API
- ❌ `POST /role/userBindRole` (已標記 @Deprecated)

---

### 2. 使用者專案完整覆蓋
**Endpoint**: `POST /admin/bindings/user-project/rebind`  
**Controller**: `AdminBindingController`

#### Request Body
```json
{
  "userId": "uuid",
  "projectIds": ["projectId1", "projectId2"]
}
```

#### 語意
- `projectIds = []`: 清空該使用者的所有專案
- `projectIds = null`: 視為空陣列
- 完整覆蓋：刪除不在清單中的專案，保留清單中的專案

#### Response
```json
{
  "success": true,
  "data": "User projects rebound successfully",
  "message": null
}
```

---

### 3. 使用者技能完整覆蓋
**Endpoint**: `POST /admin/bindings/user-skill/rebind`  
**Controller**: `AdminBindingController`

#### Request Body
```json
{
  "userId": "uuid",
  "bindings": [
    {
      "skillId": "uuid-skill-1",
      "skillLevelId": "uuid-level-1"
    },
    {
      "skillId": "uuid-skill-2",
      "skillLevelId": "uuid-level-2"
    }
  ]
}
```

#### 語意
- `bindings = []`: 清空該使用者的所有技能
- `bindings = null`: 視為空陣列
- **Diff 策略**: 只更新技能等級有變化的綁定（新增、刪除、等級變更）

#### Response
```json
{
  "success": true,
  "data": "User skills rebound successfully",
  "message": null
}
```

---

### 4. 專案技能需求完整覆蓋
**Endpoint**: `POST /admin/bindings/project-skill/rebind`  
**Controller**: `AdminBindingController`

#### Request Body
```json
{
  "projectId": "uuid",
  "bindings": [
    {
      "skillId": "uuid-skill-1",
      "skillLevelId": "uuid-level-1"
    },
    {
      "skillId": "uuid-skill-2",
      "skillLevelId": "uuid-level-2"
    }
  ]
}
```

#### 語意
- `bindings = []`: 清空該專案的所有技能需求
- `bindings = null`: 視為空陣列
- **Diff 策略**: 只更新技能等級有變化的綁定（新增、刪除、等級變更）

#### Response
```json
{
  "success": true,
  "data": "Project skills rebound successfully",
  "message": null
}
```

---

### 5. 專案成員技能完整覆蓋 ⭐ NEW
**Endpoint**: `POST /admin/bindings/project-members-skills/rebind`  
**Controller**: `AdminBindingController`

#### Request Body
```json
{
  "projectId": "uuid-project",
  "members": [
    {
      "userId": "uuid-user-1",
      "skills": [
        {
          "skillId": "uuid-skill-java",
          "skillLevelId": "uuid-level-senior"
        },
        {
          "skillId": "uuid-skill-python",
          "skillLevelId": "uuid-level-intermediate"
        }
      ]
    },
    {
      "userId": "uuid-user-2",
      "skills": []
    }
  ]
}
```

#### 語意
- `members = []`: 清空該專案**所有成員**的技能
- `members = null`: 視為空陣列
- `members[i].skills = []`: 清空**該成員**在此專案的所有技能
- `members[i].skills = null`: 視為空陣列
- **前置驗證**: 所有 `userId` 必須已是專案成員（`user_project` 存在），否則拋出 `IllegalArgumentException`
- **Diff 策略**: 只更新技能等級有變化的綁定（新增、刪除、等級變更）

#### Response
```json
{
  "success": true,
  "data": "Project member skills rebound successfully",
  "message": null
}
```

#### 錯誤處理
```json
{
  "success": false,
  "data": null,
  "message": "User {userId} is not a member of project {projectId}"
}
```

---

## 🗂️ 資料結構

### 新增資料表: `user_project_skill`

| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | UUID | Primary Key | NOT NULL |
| user_id | UUID | 使用者 ID | NOT NULL, FK → user.id |
| project_id | UUID | 專案 ID | NOT NULL, FK → project.id |
| skill_id | UUID | 技能 ID | NOT NULL, FK → skill.id |
| skill_level_id | UUID | 技能等級 ID | NOT NULL, FK → skill_level.id |
| created_by | VARCHAR | 建立者 | - |
| created_time | TIMESTAMP | 建立時間 | - |
| updated_by | VARCHAR | 更新者 | - |
| updated_time | TIMESTAMP | 更新時間 | - |

**唯一約束**: `(user_id, project_id, skill_id)`

### 資料關聯圖
```
User ──┐
       ├── UserProjectSkill ── SkillLevel
Project ┘                   └── Skill
```

---

## 🔄 Diff 策略說明

### 適用 API
- 使用者技能完整覆蓋
- 專案技能需求完整覆蓋
- 專案成員技能完整覆蓋

### 策略邏輯
1. **刪除**: 移除不在目標清單中的綁定
2. **新增**: 建立目標清單中不存在的綁定
3. **更新**: 僅當技能等級變更時才更新（避免無效的 save 操作）
4. **保留**: 技能等級未變更的綁定不進行任何操作

### 優勢
- **效能最佳化**: 只操作變更的資料
- **資料一致性**: `@Transactional` 保證原子性
- **審計追蹤**: `BaseEntity` 自動記錄 `updated_time`

---

## ⚠️ 注意事項

### 前置驗證
1. **專案成員技能 API**:
   - 必須先使用 `/admin/bindings/user-project/rebind` 綁定使用者到專案
   - 未綁定的使用者會拋出異常

### 資料完整性
- 所有 `skillLevelId` 必須對應正確的 `skillId`
- 系統會驗證 `skillLevel.skill.id == skillId`

### 權限控制
- UserController APIs: 需要 `Edit` 權限
- AdminBindingController APIs: 需要 `EditAll` 權限

### 事務管理
- 所有 rebind 方法都使用 `@Transactional`
- 任何驗證失敗或資料錯誤都會觸發 rollback

---

## 📊 使用流程範例

### 情境：建立專案團隊並分配技能

#### Step 1: 建立使用者並分配角色
```http
POST /users/{userId}/roles/rebind
{
  "userId": "user-uuid-1",
  "roleIds": ["developer-role-uuid"]
}
```

#### Step 2: 將使用者加入專案
```http
POST /admin/bindings/user-project/rebind
{
  "userId": "user-uuid-1",
  "projectIds": ["project-uuid-1"]
}
```

#### Step 3: 設定專案需要的技能
```http
POST /admin/bindings/project-skill/rebind
{
  "projectId": "project-uuid-1",
  "bindings": [
    {"skillId": "java-uuid", "skillLevelId": "senior-uuid"},
    {"skillId": "python-uuid", "skillLevelId": "intermediate-uuid"}
  ]
}
```

#### Step 4: 設定使用者的個人技能
```http
POST /admin/bindings/user-skill/rebind
{
  "userId": "user-uuid-1",
  "bindings": [
    {"skillId": "java-uuid", "skillLevelId": "senior-uuid"},
    {"skillId": "python-uuid", "skillLevelId": "intermediate-uuid"}
  ]
}
```

#### Step 5: 設定使用者在專案中的技能
```http
POST /admin/bindings/project-members-skills/rebind
{
  "projectId": "project-uuid-1",
  "members": [
    {
      "userId": "user-uuid-1",
      "skills": [
        {"skillId": "java-uuid", "skillLevelId": "senior-uuid"}
      ]
    }
  ]
}
```

---

## 🔍 測試驗證

### 測試結果
```
Tests run: 377, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 覆蓋率
- **Bundle**: 96 classes analyzed
- **Coverage**: 符合專案要求（80%）

---

## 📚 相關檔案

### Entity
- `UserProjectSkill.java`

### Repository
- `UserProjectSkillRepository.java`

### DataAccess
- `IUserProjectSkillDataAccess.java`
- `UserProjectSkillDataAccessImpl.java`

### Service
- `IUserService.java` → `rebindUserRoles()`
- `IProjectService.java` → `rebindProjectMemberSkills()`
- `UserService.java`
- `ProjectService.java`

### Controller
- `UserController.java` → `/users/{userId}/roles/rebind`
- `AdminBindingController.java` → `/admin/bindings/project-members-skills/rebind`

### DTOs
- `UserRoleRebindRequest.java`
- `ProjectMemberSkillsRebindRequest.java`
- `MemberSkillBindings.java`
- `SkillLevelBindingItem.java` (Existing)

### Utilities
- `SkillLevelBindingMapper.java` (Existing)

---

## 📅 版本記錄

### v1.0.0 (2026-06-05)
- ✅ 新增 `UserProjectSkill` Entity 與資料存取層
- ✅ 新增使用者角色完整覆蓋 API
- ✅ 新增專案成員技能完整覆蓋 API
- ✅ 標記舊 API 為 Deprecated
- ✅ 所有測試通過（377 tests）

---

**文件結束**
