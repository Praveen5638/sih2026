# Supabase Backend Integration Analysis

**Project**: artisan-ai-market-linkage  
**Platform**: Native Android (Kotlin 2.2.10, Jetpack Compose, Room v4, WorkManager 2.9.1)  
**Application ID**: `com.aistudio.artisanai.sih2026`  
**Repository Baseline**: `d1ec8ef`  

---

## 1. Executive Summary
This document provides the **Phase 0 Architectural & Technology Evaluation** for integrating **Supabase** as the shared multi-user remote backend layer for the `artisan-ai-market-linkage` application.

### Key Mandate & Non-Negotiable Rules
1. **Room is the Local Source of Truth**: Compose UI observes `Repository` StateFlows derived from local Room DB queries. The network is never in the blocking UI call path.
2. **Local-First Writes**: Every create/edit action (product creation, voice reply, text message, negotiation confirmation) writes to Room DB first, enqueues a `SyncOperationEntity` in the persistent outbox, and immediately updates the UI.
3. **Asynchronous Synchronization**: `WorkManager` / `SyncWorker` processes outbox operations asynchronously when network connectivity is available, syncing to Supabase Cloud, handling idempotency (`clientOperationId`), and reconciling remote state back to Room DB.
4. **Frozen Foundations Preserved**: **AI Image Studio**, **Voice-First Listing**, **Dynamic Pricing Assistant**, and **Offline-First Room + Outbox Architecture** remain 100% locked and preserved.

---

## 2. Answers to Phase 0 Analysis Questions

### Q1: What remote functionality already exists?
- **Outbox Queue**: `SyncOperationEntity`, `SyncDao`, `SyncWorker`, and `SyncManager` currently manage an offline queue with exponential backoff retries.
- **Network Monitoring**: `NetworkMonitor` tracks cellular/Wi-Fi status via `ConnectivityManager.NetworkCallback` and provides simulated offline toggles for testing.
- **Gemini AI Helper**: `GeminiAiHelper` invokes Google Gemini API when online or provides offline structured craft catalog fallbacks when offline.

### Q2: What API / network code already exists?
- `OkHttp 4.10.0` with `logging-interceptor`
- `Retrofit 2.12.0` with `converter-moshi`
- `Moshi 1.15.2` (Kotlin codegen)
- `NetworkMonitor.kt`, `SyncManager.kt`, `SyncWorker.kt`

### Q3: Is Retrofit already configured?
- **Yes**. Retrofit dependency (`libs.retrofit`, `libs.converter.moshi`) is declared and ready in `app/build.gradle.kts`. API interfaces for Supabase endpoints will be defined cleanly.

### Q4: Is OkHttp already configured?
- **Yes**. `okhttp` 4.10.0 and `logging-interceptor` are in `libs.versions.toml` & `app/build.gradle.kts`.

### Q5: Is there any current auth system?
- Current auth is mock UI state (`sellerMobile`, `buyerMobile`, `buyerName`, `isBuyerLoggedIn`, `artisanName`) inside `ArtisanViewModel`.
- Supabase Auth (Email/Password or Anonymous/Phone token) will drive Supabase GoTrue Auth tokens (`JWT`) to secure Row-Level Security (RLS) policies.

### Q6: What is minSdk?
- **`minSdk = 24`** (Android 7.0 Nougat).

### Q7: What is targetSdk / compileSdk?
- `compileSdk = 36`, `targetSdk = 36`.

### Q8: What is current Kotlin version?
- **`kotlin = "2.2.10"`**.

### Q9: What is current Gradle / AGP version?
- AGP **`9.1.1`**.

### Q10: What is current serialization mechanism?
- **Moshi 1.15.2** with `@JsonClass(generateAdapter = true)` and Kotlin reflection adapters.

### Q11: How are Room URIs represented?
- Local file URIs represented as Strings starting with `file:///` (e.g. `file:///data/user/0/com.aistudio.artisanai.sih2026/cache/...`).

### Q12: How are local images stored?
- Bitmaps captured by camera or studio are compressed as JPEGs into local internal cache/files storage (`ProductImageProcessor.kt`) and referenced via local `file:///` URIs in `originalImageUrl` and `enhancedImageUrl`.

### Q13: How does SyncWorker currently process operations?
- `SyncWorker.doWork()` queries `syncDao.getPendingOperations()`.
- Updates operation status to `SYNCING`.
- Passes `clientOperationId` and `payloadJson` to `executeServerSyncPayload()`.
- Updates status to `SYNCED` upon success and deletes processed operations. Handles exponential backoff (`FAILED_RETRYABLE` / `FAILED_PERMANENT`).

### Q14: How should Supabase operations map to SyncOperationEntity?
- `CREATE_PRODUCT`: POST `/rest/v1/products` (with `Prefer: return=representation`)
- `UPDATE_PRODUCT`: PATCH `/rest/v1/products?id=eq.{id}`
- `UPLOAD_IMAGE`: POST `/storage/v1/object/product-images/{path}`
- `SEND_MESSAGE`: POST `/rest/v1/messages` (with `clientMessageId` idempotency header/body)
- `CONFIRM_TERMS`: PATCH `/rest/v1/conversations?conversation_id=eq.{id}` (status = `ORDER_READY`)

### Q15: What existing code can be reused?
- All Room DAOs (`ProductDao`, `DraftDao`, `SyncDao`, `ConversationDao`, `MessageDao`)
- All Room Entities (`ProductEntity`, `ProductDraftEntity`, `SyncOperationEntity`, `ConversationEntity`, `MessageEntity`)
- `SyncWorker`, `SyncManager`, `NetworkMonitor`
- `VoiceAssistEngine`, `TtsQueueManager`
- All Jetpack Compose screens (`HomeScreen`, `CatalogAndDetailScreens`, `PublicProductDetailScreen`, `ConversationListScreen`, `ChatRoomScreen`)

### Q16: Which new dependencies are actually required?
- No new heavy SDKs required. Option C (Retrofit + OkHttp + Moshi) leverages existing dependencies, maintaining `minSdk 24` zero-desugaring compatibility.

---

## 3. Client Integration Technology Evaluation

### Evaluation Matrix

| Criterion | Option A: `supabase-kt` SDK | Option B: Retrofit/OkHttp REST Only | Option C: Hybrid Retrofit + OkHttp Realtime (Selected) |
|---|---|---|---|
| **minSdk 24 Compatibility** | Requires Java 8 desugaring / API 26+ | **100% Compatible (No desugaring)** | **100% Compatible (No desugaring)** |
| **Dependency Footprint** | Adds 10+ Ktor & Kotlinx libs (~8 MB) | **0 MB (Reuses existing Retrofit)** | **0 MB (Reuses existing Retrofit/OkHttp)** |
| **Serialization** | Requires `kotlinx.serialization` | Moshi 1.15.2 (Already in project) | Moshi 1.15.2 (Already in project) |
| **Outbox / SyncWorker Integration** | Requires coroutine wrapper | **Direct synchronous & suspend calls** | **Direct synchronous & suspend calls** |
| **Realtime Push Support** | Built-in | None | **OkHttp WebSocket / SSE Listener** |
| **Offline-First Safety** | Risk of UI direct network calls | 100% Offline-First | **100% Offline-First** |

### Decision: **Option C — Hybrid Retrofit/OkHttp REST + OkHttp Realtime**
- **Database & Storage & Auth**: Supabase Postgrest REST API (`/rest/v1`), Storage API (`/storage/v1`), and Auth API (`/auth/v1`) via Retrofit 2.12 + OkHttp 4.10 + Moshi 1.15.
- **Realtime Updates**: OkHttp WebSocket / SSE connection to Supabase Realtime (`wss://<project>.supabase.co/realtime/v1/websocket`).
- **Rationale**: Guarantees zero `minSdk` conflicts, zero build regressions, minimal APK size, full offline safety, and seamless `SyncWorker` outbox integration.

---

## 4. PostgreSQL Remote Schema Design

### ER Diagram Overview
```
+-------------------+       +------------------------+       +-------------------+
|     profiles      |       |        products        |       |   product_images  |
+-------------------+       +------------------------+       +-------------------+
| id (UUID, PK)     |◄──────| id (UUID, PK)          |◄──────| id (UUID, PK)     |
| role (TEXT)       |       | local_id (BIGINT)      |       | product_id (UUID) |
| full_name (TEXT)  |       | seller_id (UUID, FK)   |       | storage_path(TEXT)|
| craft (TEXT)      |       | product_name (TEXT)    |       | image_url (TEXT)  |
| location (TEXT)   |       | selling_price (NUMERIC)|       | is_primary (BOOL) |
| created_at (TIMESTAMPTZ)  | status (TEXT)          |       +-------------------+
+-------------------+       +------------------------+
         ▲                              ▲
         │                              │
         └──────────────┐               │
                        │               │
            +-------------------------------+
            |         conversations         |
            +-------------------------------+
            | conversation_id (TEXT, PK)    |
            | product_id (BIGINT)           |
            | buyer_id (UUID, FK)           |
            | seller_id (UUID, FK)          |
            | current_status (TEXT)         |
            | agreed_quantity (INT)         |
            | agreed_unit_price (NUMERIC)   |
            +-------------------------------+
                            ▲
                            │
            +-------------------------------+
            |            messages           |
            +-------------------------------+
            | message_id (TEXT, PK)         |
            | conversation_id (TEXT, FK)    |
            | client_message_id (TEXT, UNIQUE)|
            | sender_id (TEXT)              |
            | sender_type (TEXT)            |
            | text (TEXT)                   |
            | extracted_quantity (INT)      |
            | extracted_price (NUMERIC)     |
            | created_at (TIMESTAMPTZ)      |
            +-------------------------------+
```

---

## 5. Row-Level Security (RLS) & Authorization

### RLS Policies
1. **`products` Table**:
   - `SELECT`: Public access for status `'PUBLISHED'` or `'READY'`. Owners can read all own products (`seller_id = auth.uid()`).
   - `INSERT / UPDATE / DELETE`: `seller_id = auth.uid()`.
2. **`conversations` Table**:
   - `SELECT / INSERT / UPDATE`: `buyer_id = auth.uid() OR seller_id = auth.uid()`.
3. **`messages` Table**:
   - `SELECT / INSERT`: User must be a participant of the parent conversation (`EXISTS (SELECT 1 FROM conversations WHERE conversation_id = messages.conversation_id AND (buyer_id = auth.uid() OR seller_id = auth.uid()))`).
4. **`storage.objects` (`product-images` bucket)**:
   - `SELECT`: Public read.
   - `INSERT / UPDATE`: Authenticated sellers (`auth.role() = 'authenticated'`).

---

## 6. Target Production Architecture Flow
```
                         SUPABASE CLOUD
                ┌────────────────────────────┐
                │ PostgreSQL Database        │
                │ Supabase Auth (JWT)        │
                │ Supabase Realtime (WSS)    │
                │ Supabase Storage (Buckets) │
                └─────────────┬──────────────┘
                              │
                    HTTPS / REST / WSS
                              │
                    ┌─────────┴─────────┐
                    │                   │
                 ARTISAN              BUYER
                  DEVICE              DEVICE
                    │                   │
                 Room DB             Room DB
                    │                   │
                 Outbox              Outbox
                    │                   │
              WorkManager        WorkManager
                    │                   │
                    └───────Sync────────┘
```
