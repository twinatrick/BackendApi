# 當前狀態與剩餘工作

## 已完成 🎉
- [x] 全部 8 個 module 編譯成功
- [x] 全部 720 個測試通過（0 failure, 0 error）
- [x] ai-service startup 成功 (port 8007)
- [x] alert-service startup 成功 (port 8008, Kafka 正常連線)
- [x] iam-service startup 成功 (port 8001, PostgreSQL 正常)
- [x] gateway startup 成功 (port 8080, WebFlux Netty)
- [x] project-skill-service startup 成功 (port 8004)
- [x] Attu port 衝突修正 (8001→8002)
- [x] project-skill-service 移除 User 注入（ProjectService + SkillService）
- [x] user-binding 等其他 Controller 跨服務搬遷

## 待處理 🚧
### 基礎設施
- [ ] Docker 處於穩定運作狀態，確認 compose.yaml 中的 ATTU_PORT 預設值為 8002

### 服務啟動驗證
- [ ] job-service startup 測試
- [ ] 所有服務同時啟動的端對端測試

### 程式碼清理
- [ ] 確認 `CurrentUserProvider` AutoConfiguration 的 `AutoConfiguration.imports` 是否需要保留或移除
