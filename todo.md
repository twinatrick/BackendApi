# Todo.md - 剩餘工作清單

> 分支: `refactor/api-gateway-microservices`

## 已完成 🎉

- [x] Parent POM 轉為多 module (8 個子 module)
- [x] backend-common 共用模組 (Entity, Dto, Exception, Feign, Util)
- [x] backend-gateway Spring Cloud Gateway (port 8080)
- [x] backend-iam-service (port 8001, 合併 auth+user+permission)
- [x] backend-project-skill-service (port 8004, 合併 project+skill)
- [x] backend-job-service (port 8006)
- [x] backend-ai-service (port 8007)
- [x] backend-alert-service (port 8008)
- [x] Nacos Server 加入 compose.yaml
- [x] OpenFeign Client 跨服務呼叫 (User/Project/Skill/Ai)
- [x] Internal Controller 內部端點
- [x] 所有 Entity 集中到 backend-common
- [x] 測試檔案搬遷到各 module
- [x] `mvnw test-compile` ✅ 全部通過

## 待處理 🚧

### 1. 跨服務 DataAccess 改用 Feign（部分完成）
- [ ] `backend-iam-service/UserService.java` 中 `projectDataAccess`, `userProjectDataAccess` → 改為 ProjectServiceFeignClient 呼叫
- [ ] `backend-job-service/UserJobLinkService.java` 中 `userDataAccess` → 改為 UserServiceFeignClient + entityManager.find

### 2. `src/main/` 單體殘留程式碼搬遷
下列主程式仍只在舊的 `src/main/` 中，尚未搬入對應 module：
- [ ] `Config/BloomFilterInitializer.java` → 搬至 alert-service
- [ ] `Config/CachePenetrationProtectionCache.java` → 搬至 alert-service
- [ ] `Config/RedisConfig.java` → 搬至 alert-service
- [ ] `Service/impl/InitAndCheckService.java` → 搬至 alert-service
- [ ] `Util/AudioProcessUtil.java` → 搬至 ai-service
- [ ] `Util/CallApi.java` → 搬至 alert-service
- [ ] `Util/SkillLevelBindingMapper.java` → 搬至 project-skill-service
- [ ] `Timer/JobScrapingTimer.java` → 搬至 job-service
- [ ] `WebSocket/AlarmWebSocket.java` → 搬至 alert-service

### 3. 需重寫的測試（跨服務整合測試，原單體測試不適用）
- [ ] 重寫 `BloomFilterInitializerTest` → 適用 alert-service
- [ ] 重寫 `CacheAvalancheTest` / `CachePenetrationProtectionCacheTest` → 適用 alert-service
- [ ] 重寫 `TTLJitterTest` / `RedisConfigTest` → 適用 alert-service
- [ ] 重寫 `InitAndCheckServiceTest` → 分拆到 IAM + Alert 服務

### 4. 可刪除的 src/ 原始單體
- [ ] 確認所有程式碼已搬遷完成後，可安全刪除 `src/main/` 和 `src/test/`

### 5. 最終驗證
- [ ] `rtk ./mvnw test` 測試全部通過
- [ ] 啟動 Nacos + 各服務 + Gateway 驗證端對端流程
