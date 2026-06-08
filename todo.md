# STT 功能執行計畫 ✅ 全數完成

## Phase 1：驗證 Bloom Filter 修正 ✅
- [x] 設定 JAVA_HOME
- [x] 修改 `CachePenetrationProtectionCache`：非 UUID key 跳過 Bloom Filter 檢查
- [x] 啟動 `docker compose` 基礎服務
- [x] 應用啟動成功，無 NPE

## Phase 2：ONNX → whisperjni 遷移 ✅
- [x] pom.xml：移除 onnxruntime + 新增 whisper-jni 1.7.1
- [x] application.yml：新增 `ai.whisper.model-path` 配置
- [x] 重寫 `WhisperOnnxService`：whisperjni API + GPU 自動加速
- [x] 刪除舊 `whisper-tiny.onnx`（32MB）
- [x] .gitignore：加入 `*.bin`
- [x] README.md：技術棧更新 + 模型下載指南（ggml-large-v3-turbo / ggml-tiny）

## Phase 3：STT 單元測試 ✅
- [x] `PhoneticConvertServiceTest`（6 tests）
- [x] `WhisperOnnxServiceTest`（4 tests）
- [x] `AudioProcessUtilTest`（3 tests）
- [x] `LearnServiceImplTest`（4 tests）
- [x] `LearnControllerTest`（3 tests）

## Phase 4：測試全數綠燈 ✅
- [x] `rtk ./mvnw test` — **475 tests, 0 failures, 0 errors**

## Phase 5：端到端驗證 ✅
- [x] 應用啟動正常（Port 8000）
- [x] STT endpoint `/stt/v1/{lan}/{mode}` 已部署（回傳 403 需 JWT 認證）

## 剩餘事項
- [ ] 下載模型：`curl -L -o src/main/resources/models/ggml-large-v3-turbo.bin https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3-turbo.bin`
- [ ] 如需不加驗證存取 STT，在 Controller 加上 `@Ingnore` 註解
