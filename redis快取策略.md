# Redis 快取策略文件

## 一、現有快取基礎設施

### Redis 連線
- **連線方式**: Lettuce ConnectionFactory
- **Host 解析邏輯**: `REDIS_HOST` 環境變數 → `APP_IN_DOCKER` 判斷（docker 內為 `redis`，本機為 `localhost`）
- **預設 port**: `6379`

### 序列化
- **Key 序列化**: `StringRedisSerializer`
- **Value 序列化**: `GenericJackson2JsonRedisSerializer`（支援多態類型）

### 全域預設
- **預設 TTL**: 1 小時（可透過 `REDIS_CACHE_TTL_HOURS` 環境變數調整）
- **快取管理器**: `RedisCacheManager`，支援 `@Cacheable`、`@CachePut`、`@CacheEvict` 註解

---

## 二、快取策略總覽

| 層級 | 資料類型 | TTL 範圍 | 策略說明 |
|------|---------|---------|---------|
| **L1 - 參考資料** | Skill, Role, Function, SkillLevel | 6 ~ 24 小時 | 極少變更，長 TTL，寫時清除全部 |
| **L2 - 業務資料** | Company, JobPosting, Project | 30 分鐘 ~ 6 小時 | 中等變更頻率，單條清除 |
| **L3 - 使用者資料** | UserSkills, UserProjects, UserJobLinks | 10 分鐘 | 個人化資料，短 TTL |
| **L4 - 運算結果** | 平均值聚合, 成員技能關聯 | 30 分鐘 | 計算密集型，節省 CPU |

---

## 三、完整快取配置（RedisConfig）

| 快取名稱空間 | TTL | 資料說明 | 清理時機 |
|-------------|-----|---------|---------|
| `users` | 2 小時 | 使用者基本資訊 + Email 映射 | 修改/刪除使用者時清除 |
| `alertCheckLimit` | 1 小時 | 告警檢查閾值 | 更新/刪除閾值時清除 |
| `aquarkData` | 1 小時 | IoT 水文資料 | 更新資料時清除 |
| `skills` | 24 小時 | 技能定義列表與單項 | 新增/修改/刪除技能時清除全部 |
| `skillLevels` | 24 小時 | 特定技能的等級列表 | 新增/修改/刪除等級時清除 |
| `roles` | 6 小時 | 角色列表、單項角色、角色名稱 | 新增/修改/刪除角色時清除全部 |
| `roleFunctions` | 6 小時 | 角色對應的功能權限 | 角色綁定/解除功能時清除 |
| `functions` | 24 小時 | 功能樹列表、單項功能、功能名稱 | 新增/修改/刪除功能時清除全部 |
| `companies` | 6 小時 | 公司列表與單項公司 | 修改/刪除公司時清除 |
| `jobPostings` | 1 小時 | 職缺列表與單項職缺 | 修改/刪除職缺時清除 |
| `projectSkills` | 30 分鐘 | 專案關聯技能 | 綁定/解除技能時清除 |
| `projectMemberSkills` | 30 分鐘 | 專案成員技能（N+1 高風險） | 成員技能變更時清除 |
| `userProjects` | 10 分鐘 | 當前使用者專案清單 | 專案綁定/解除時清除 |
| `currentUserSkills` | 10 分鐘 | 當前使用者技能（多表合併） | 使用者技能變更時清除 |
| `userJobLinks` | 10 分鐘 | 使用者職缺收藏清單 | 新增/刪除連結時清除 |
| `userRoles` | 10 分鐘 | 使用者角色清單 | 角色綁定/解除時清除 |
| `aquarkDataAvg` | 30 分鐘 | 平均數據（運算密集型） | TTL 到期自動失效 |

---

## 四、各 Service 快取實作細節

### 4.1 UserService（快取名稱: `users`）

#### 現有問題修復
- **`getUserById`**: `@Cacheable(key = "#result.id")` → 改為 `#id`
- **`saveUserWithRole`**: 新增 `@CacheEvict(value = "users", allEntries = true)`

#### 快取方法

```java
// 查詢 - 快取
@Cacheable(value = "users", key = "#email", unless = "#result == null")
UserVo getOnlyUserByEmail(String email);

@Cacheable(value = "users", key = "#id", unless = "#result == null")
UserVo getUserById(String id);

// 寫入 - 更新快取
@Caching(put = {
    @CachePut(value = "users", key = "#result.id", unless = "#result == null"),
    @CachePut(value = "users", key = "#result.email", unless = "#result == null")
})
UserVo createUser(UserVo userVo);

@Caching(put = {
    @CachePut(value = "users", key = "#result.id", unless = "#result == null"),
    @CachePut(value = "users", key = "#result.email", unless = "#result == null")
})
UserVo saveUser(UserVo userVo);

// 寫入 - 清除快取
@CacheEvict(value = "users", allEntries = true)
void saveUserWithRole(UserVo userVo);

@CacheEvict(value = "users", allEntries = true)
void rebindUserRoles(UUID userId, List<String> roleIds);

@CacheEvict(value = "userProjects", allEntries = true)
void rebindUserProjects(UUID userId, List<UUID> projectIds);
```

#### 不建議快取的方法
| 方法 | 原因 |
|------|------|
| `getUser()` / `getAllUsersVo()` | 全表掃描，管理用途，命中率低 |
| `getUserByEmail(String)` | 回傳 `List`，設計不一致 |
| `searchUsers(UserSearchQuery)` | 分頁查詢參數多變 |
| `bindUserProject/rebindUserProjects` | 高頻寫入，需即時一致性 |
| `getAllParent/getCurrentUserInfo` | 內部包含大量關聯，建議快取最終結果 |

---

### 4.2 SkillService（快取名稱: `skills`, `skillLevels`, `currentUserSkills`）

```java
@Cacheable(value = "skills", key = "'all'", unless = "#result.isEmpty()")
List<SkillVo> getSkill();

@Cacheable(value = "skillLevels", key = "#skillId", unless = "#result.isEmpty()")
List<SkillLevelVo> getSkillLevels(String skillId);

@Cacheable(value = "currentUserSkills", key = "#currentUser.id", unless = "#result.isEmpty()")
List<CurrentUserSkillVo> getCurrentUserSkills();

// 寫入操作 - 清除對應快取
@CacheEvict(value = "skills", allEntries = true)
SkillVo addSkill(SkillVo skillVo);

@CacheEvict(value = "skills", allEntries = true)
void updateSkill(SkillVo skillVo);

@CacheEvict(value = "skills", allEntries = true)
void deleteSkill(SkillVo skillVo);

@Caching(evict = {
    @CacheEvict(value = "skillLevels", key = "#skillLevelVo.skillId"),
    @CacheEvict(value = "skills", allEntries = true)
})
SkillLevelVo addSkillLevel(SkillLevelVo skillLevelVo);

@Caching(evict = {
    @CacheEvict(value = "skillLevels", key = "#skillLevelVo.skillId"),
    @CacheEvict(value = "skills", allEntries = true)
})
void updateSkillLevel(SkillLevelVo skillLevelVo);

@Caching(evict = {
    @CacheEvict(value = "skillLevels", allEntries = true),
    @CacheEvict(value = "skills", allEntries = true),
    @CacheEvict(value = "currentUserSkills", allEntries = true)
})
void deleteSkillLevel(String id);

// 個人技能操作 - 清除使用者技能快取
@Caching(evict = {
    @CacheEvict(value = "currentUserSkills", allEntries = true)
})
SkillVo addPersonalSkill(PersonalSkillRequest request);

@Caching(evict = {
    @CacheEvict(value = "currentUserSkills", allEntries = true)
})
void updatePersonalSkill(UUID skillId, PersonalSkillRequest request);

@Caching(evict = {
    @CacheEvict(value = "currentUserSkills", allEntries = true)
})
void updatePersonalSkillLevel(UUID skillId, UUID skillLevelId);

@Caching(evict = {
    @CacheEvict(value = "currentUserSkills", allEntries = true)
})
void deletePersonalSkill(UUID skillId);

@CacheEvict(value = "currentUserSkills", allEntries = true)
void rebindUserSkills(UUID userId, List<UserSkillBindingRequest> bindings);
```

---

### 4.3 RoleService（快取名稱: `roles`, `roleFunctions`, `userRoles`）

```java
@Cacheable(value = "roles", key = "'all'", unless = "#result.isEmpty()")
List<RoleOutVo> getRole();

@Cacheable(value = "roles", key = "#roleId", unless = "#result == null")
RoleOutVo getRoleById(String roleId);

@Cacheable(value = "roleFunctions", key = "#roleId", unless = "#result.isEmpty()")
List<FunctionVo> getFunctionByRole(String roleId);

@Cacheable(value = "roles", key = "#name", unless = "#result == null")
RoleOutVo getRoleByName(String name);

@Cacheable(value = "userRoles", key = "#userId", unless = "#result.isEmpty()")
List<RoleOutVo> getRoleByUser(String userId);

// 寫入操作 - 清除對應快取
@Caching(evict = {
    @CacheEvict(value = "roles", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true)
})
RoleOutVo addRole(RoleOutVo roleOutVo);

@Caching(evict = {
    @CacheEvict(value = "roles", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true)
})
void updateRole(RoleOutVo roleOutVo);

@Caching(evict = {
    @CacheEvict(value = "roles", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true)
})
void deleteRole(RoleOutVo roleOutVo);

@Caching(evict = {
    @CacheEvict(value = "roleFunctions", allEntries = true),
    @CacheEvict(value = "roles", allEntries = true)
})
void roleBindFunction(String roleId, List<String> functionIds);

@Caching(evict = {
    @CacheEvict(value = "userRoles", key = "#userId"),
    @CacheEvict(value = "users", allEntries = true)  // 使用者資訊變更
})
void roleBindUser(String roleId, String userId);
```

---

### 4.4 FunctionService（快取名稱: `functions`）

```java
@Cacheable(value = "functions", key = "'all'", unless = "#result.isEmpty()")
List<FunctionVo> getFunction();

@Cacheable(value = "functions", key = "#id", unless = "#result == null")
FunctionVo getFunctionById(String id);

@Cacheable(value = "functions", key = "#name", unless = "#result == null")
FunctionVo getFunctionByName(String name);

@Cacheable(value = "functions", key = "#name + '_' + #parent", unless = "#result == null")
FunctionVo getFunctionByNameAndParent(String name, String parent);

// 寫入操作 - 清除快取
@CacheEvict(value = "functions", allEntries = true)
FunctionVo addFunction(FunctionVo functionVo);

@Caching(evict = {
    @CacheEvict(value = "functions", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true),
    @CacheEvict(value = "roles", allEntries = true)
})
void updateFunction(FunctionVo functionVo);

@Caching(evict = {
    @CacheEvict(value = "functions", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true),
    @CacheEvict(value = "roles", allEntries = true)
})
void deleteFunction(FunctionVo functionVo);

@Caching(evict = {
    @CacheEvict(value = "functions", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true),
    @CacheEvict(value = "roles", allEntries = true)
})
void saveFunction(List<FunctionVo> functionVos);

@Caching(evict = {
    @CacheEvict(value = "functions", allEntries = true),
    @CacheEvict(value = "roleFunctions", allEntries = true),
    @CacheEvict(value = "roles", allEntries = true)
})
void saveFunctionNewChild(List<FunctionVo> functionVos);
```

---

### 4.5 ProjectService（快取名稱: `projectSkills`, `projectMemberSkills`, `userProjects`, `projects`）

```java
@Cacheable(value = "projects", key = "'all'", unless = "#result.isEmpty()")
List<ProjectVo> getProject();

@Cacheable(value = "userProjects", key = "#currentUser.id", unless = "#result.isEmpty()")
List<ProjectVo> getCurrentUserProjects();

@Cacheable(value = "projectSkills", key = "#projectId", unless = "#result.isEmpty()")
List<ProjectSkillVo> getProjectSkills(UUID projectId);

@Cacheable(value = "projectMemberSkills", key = "#projectId", unless = "#result.isEmpty()")
List<ProjectMemberSkillVo> getProjectMemberSkills(UUID projectId);

// 寫入操作 - 清除對應快取
@Caching(evict = {
    @CacheEvict(value = "projects", allEntries = true),
    @CacheEvict(value = "userProjects", allEntries = true)
})
ProjectVo addProject(ProjectVo projectVo);

@Caching(evict = {
    @CacheEvict(value = "projects", allEntries = true),
    @CacheEvict(value = "userProjects", allEntries = true),
    @CacheEvict(value = "projectSkills", allEntries = true),
    @CacheEvict(value = "projectMemberSkills", allEntries = true)
})
void updateProject(ProjectVo projectVo);

@Caching(evict = {
    @CacheEvict(value = "projectSkills", allEntries = true),
    @CacheEvict(value = "projectMemberSkills", allEntries = true)
})
void rebindProjectSkills(UUID projectId, Map<String, String> skillIdLevelIdMap);

@Caching(evict = {
    @CacheEvict(value = "projectSkills", allEntries = true),
    @CacheEvict(value = "projectMemberSkills", allEntries = true),
    @CacheEvict(value = "userProjects", allEntries = true)
})
void rebindPersonalProjectSkills(UUID projectId, Map<String, String> skillIdLevelIdMap);
```

---

### 4.6 CompanyService（快取名稱: `companies`）

```java
@Cacheable(value = "companies", key = "'all'", unless = "#result.isEmpty()")
List<CompanyVo> getAllCompanies();

@Cacheable(value = "companies", key = "#id", unless = "#result == null")
CompanyVo getCompanyById(String id);

// 寫入操作 - 清除快取
@CacheEvict(value = "companies", allEntries = true)
CompanyVo createCompany(CreateCompanyRequest request);

@CacheEvict(value = "companies", allEntries = true)
CompanyVo updateCompany(UpdateCompanyRequest request);

@CacheEvict(value = "companies", allEntries = true)
void deleteCompany(String id);
```

---

### 4.7 JobPostingService（快取名稱: `jobPostings`）

```java
@Cacheable(value = "jobPostings", key = "#id", unless = "#result == null")
JobPostingVo getJobPostingById(String id);

@Cacheable(value = "jobPostings", key = "'company_' + #companyId", unless = "#result.isEmpty()")
List<JobPostingVo> getJobPostingsByCompanyId(String companyId);

// 寫入操作 - 清除快取
@Caching(evict = {
    @CacheEvict(value = "jobPostings", allEntries = true)
})
JobPostingVo createJobPosting(JobPostingVo jobPostingVo);

@Caching(evict = {
    @CacheEvict(value = "jobPostings", allEntries = true)
})
JobPostingVo updateJobPosting(JobPostingVo jobPostingVo);

@Caching(evict = {
    @CacheEvict(value = "jobPostings", allEntries = true)
})
void deleteJobPosting(String id);

@Caching(evict = {
    @CacheEvict(value = "jobPostings", allEntries = true)
})
List<JobPostingVo> scrapeAndAnalyzeJobs(String companyId);
```

---

### 4.8 AquarkDataService（快取名稱: `aquarkData`, `aquarkDataAvg`）

#### 現有問題修復
- `@Cacheable(value = "aquarkData", key = "#result.station_id + '_' + #result.trans_time.toString()")` → 改為 `#aquarkDataRaw.station_id + '_' + #aquarkDataRaw.trans_time`

```java
// 快取
@Cacheable(value = "aquarkData", key = "#aquarkDataRaw.station_id + '_' + #aquarkDataRaw.trans_time", unless = "#result == null")
AquarkDataVo getAquarkData(AquarkDataRaw aquarkDataRaw);

@Cacheable(value = "aquarkDataAvg", key = "#startTime.toString() + '_' + #endTime.toString()", unless = "#result == null")
Double getAverageAquark(Date startTime, Date endTime);

// 寫入 - 更新快取
@CachePut(value = "aquarkData", key = "#aquarkDataRaw.station_id + '_' + #aquarkDataRaw.trans_time")
@CacheEvict(value = "aquarkDataAvg", allEntries = true)
AquarkDataVo updateAquarkData(AquarkDataRaw aquarkDataRaw);
```

---

### 4.9 UserJobLinkService（快取名稱: `userJobLinks`）

```java
@Cacheable(value = "userJobLinks", key = "#userId", unless = "#result.isEmpty()")
List<UserJobLinkVo> getUserJobLinksByUserId(String userId);

// 寫入操作 - 清除快取
@CacheEvict(value = "userJobLinks", allEntries = true)
UserJobLinkVo createUserJobLink(UserJobLinkVo userJobLinkVo);

@CacheEvict(value = "userJobLinks", allEntries = true)
void deleteUserJobLink(String id);
```

---

### 4.10 AlertCheckLimitService（快取名稱: `alertCheckLimit`）— 已完善，無需修改

```java
// 已實作（完整範例）
@Cacheable(value = "alertCheckLimit", key = "#apiId + '_' + #dataKey", unless = "#result == null")
AlertCheckLimitVo getLimit(String apiId, String dataKey);

@CachePut(value = "alertCheckLimit", key = "#apiId + '_' + #dataKey")
AlertCheckLimitVo insertLimit(String apiId, String dataKey, double value);

@CachePut(value = "alertCheckLimit", key = "#entity.apiId + '_' + #entity.dataKey")
AlertCheckLimit updateEntity(AlertCheckLimit entity);

@CacheEvict(value = "alertCheckLimit", key = "#entity.apiId + '_' + #entity.dataKey")
void deleteLimitEntity(AlertCheckLimit entity);
```

---

## 五、不建議快取的服務與原因

| 服務 | 說明 |
|------|------|
| **GeminiService** | 呼叫 Google Gemini 外部 API，每次爬取 HTML 不同，結果不適合快取 |
| **AlarmService** | 即時告警處理，需要即時一致性 |
| **AlarmKafkaPublisher** | Kafka 訊息發布，非資料查詢 |
| **KafkaConsumerService** | Kafka 訊息消費 + WebSocket 廣播，非資料查詢 |
| **CheckApiService** | IoT 資料檢查，即時性要求高 |
| **initAndCheckService** | 啟動初始化邏輯，僅執行一次 |
| **IProjectStore** | 儲存層介面，無具體實作 |

---

## 六、快取 Key 設計原則

| 資料類型 | Key 模式 | 範例 |
|---------|---------|------|
| 單項查詢（ID） | `#id` | `company.getId()` → `"a1b2c3d4-..."` |
| 單項查詢（名稱） | `#name` | `"admin"` |
| 複合查詢 | `#field1 + '_' + #field2` | `"station1_2024-01-01"` |
| 全量列表 | `'all'` | `"all"` |
| 關聯查詢 | `'prefix_' + #id` | `"company_a1b2c3d4-..."` |

---

## 七、快取失效策略

| 操作類型 | 失效策略 | 適用場景 |
|---------|---------|---------|
| **CUD 單項** | `@CacheEvict(key = "#entity.id")` | AlertCheckLimit（精確清除） |
| **CUD 全部** | `@CacheEvict(allEntries = true)` | 參考資料（Skill, Role, Function） |
| **級聯清除** | `@Caching(evict = {...})` | 功能/角色變更時同時清除多個快取 |
| **TTL 自動過期** | `entryTtl(Duration.ofHours(n))` | 所有快取的最終保障 |

### 快取穿透防護（已實作）

採用雙層防護機制：

#### 第一層：空值快取（Null Value Caching）
- 自訂 `CachePenetrationProtectionCache` 包裝 `RedisCache`
- 當 DB 查詢結果為 null 時，存入 `NullValue` 佔位物件（短 TTL，預設 5 分鐘）
- `get()` 時自動判讀 `NullValue` 並轉換為 null 回傳
- 所有 `@Cacheable` 已移除 `unless = "#result == null"` 條件

#### 第二層：布隆過濾器（Bloom Filter）— 僅寫入路徑
- 基於 **Redisson RBloomFilter** 實作（`IBloomFilterService`）
- 每個 cache namespace 獨立過濾器，Redis key 格式：`bloom:{cacheName}`
- 預期資料量：10,000，誤判率：1%
- 啟動時透過 `BloomFilterInitializer` 從 DB 預先填充
- 資料新增/更新（`put()`）時自動加入過濾器
- **不參與讀取路徑**：因 SimpleKey.EMPTY 與名稱型 key（如 `'byname:admin'`）非 UUID 格式，BF 無法涵蓋，讀取防護依賴空值快取 + 分散式鎖
- 優點：分散式儲存於 Redis，多實例共享；持久化不遺失

#### 流程示意
```
請求 key → CachePenetrationProtectionCache.get(key)
  ├─ ① 空值快取命中？ → YES → 回傳 null (快取命中)
  └─ ② 正常快取查找 → 命中 → 回傳值 / 未命中 → DB → 寫入快取
                                                          ├─ result ≠ null → 寫入 Redis + BF.add(key)
                                                          └─ result = null  → 寫入空值標記 (short TTL)
```

### 快取雪崩防護（已實作）

採用三層防護機制：

#### 第一層：TTL 隨機化
- 所有 cache 的 TTL 在基礎值上加入 **0~30% 的亂數偏移**
- 例：`userProjects` 基礎 10 分鐘 → 實際 10~13 分鐘隨機
- 每個 key 獨立過期，不再集體失效
- 實作於 `RedisConfig.withJitter()`

#### 第二層：分散式鎖（Redis RLock）
- 當 `@Cacheable(sync=true)` 觸發 `get(key, valueLoader)` 時
- 使用 Redisson `RLock("lock:cache:{name}:{key}")` 進行分散式鎖定
- **只允許一個執行緒查 DB**，其他執行緒等待 200ms 後直接查 DB（防止死鎖）
- 跨 Docker 實例也有效
- 實作於 `CachePenetrationProtectionCache.get(key, valueLoader)`

#### 第三層：`@Cacheable(sync = true)`
- 所有 30 處 `@Cacheable` 已加入 `sync = true`
- 觸發 Spring Cache 的 `get(key, valueLoader)` 路徑
- 與分散式鎖配合，形成多層防護

#### 防護流程
```
多個請求同時打到同一過期的 key
  ├─ ① 只有第一個成功取得 RLock
  │    ├─ 雙重檢查快取（double-check）
  │    └─ valueLoader.call() 查 DB → 寫入快取 → 回傳
  ├─ ② 其他請求
  │    ├─ 鎖競爭失敗 → 等待 200ms
  │    └─ 雙重檢查快取 → 命中 → 回傳 (無 DB 查詢)
  └─ ③ 鎖超時 (200ms) → 降級直接查 DB（防止死鎖）
```

---

## 八、相關檔案說明

| 檔案 | 說明 |
|------|------|
| `Service/IBloomFilterService.java` | 布隆過濾器服務介面 |
| `Service/impl/BloomFilterService.java` | 布隆過濾器實作（Redisson RBloomFilter） |
| `Config/BloomFilterInitializer.java` | 啟動時從 DB 填充過濾器 |
| `Config/CachePenetrationProtectionCache.java` | 自訂 Cache 包裝（BF + Null Value） |
| `Config/CachePenetrationProtectionCacheManager.java` | 自訂 CacheManager |
| `Config/NullValueTtlProperties.java` | 空值 TTL 配置屬性 |
| `Util/NullValue.java` | 可序列化的空值佔位 POJO |

---

## 九、優先級與實作路線圖

| 階段 | 內容 | 影響範圍 |
|------|------|---------|
| **Phase 0 - 修復** | 修復 `UserService` + `AquarkDataService` key bug | 2 個 Service + 測試 |
| **Phase 1 - 核心** | 實作 Skill、Role、Function 快取（參考資料） | 3 個 Service + RedisConfig |
| **Phase 2 - 業務** | 實作 Company、JobPosting、Project 快取 | 3 個 Service |
| **Phase 3 - 使用者** | 實作 UserJobLink、UserProjects、CurrentUserSkills 快取 | 3 個 Service |
| **Phase 4 - 優化** | 實作 AquarkData 平均運算快取 | 1 個 Service |
| **Phase 5 - 穿透防護** | 實作 Bloom Filter + Null Value 雙層快取穿透防護 ✅ | 9 Service + RedisConfig |
| **Phase 6 - 雪崩防護** | TTL 隨機化 + 分散式鎖 + sync=true ✅ | 9 Service + RedisConfig + Cache |
