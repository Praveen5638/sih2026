# Supabase Backend Integration Specification & Implementation Report

**Project**: artisan-ai-market-linkage  
**Platform**: Native Android (Kotlin, Jetpack Compose, Room v4, WorkManager)  
**Application ID**: `com.aistudio.artisanai.sih2026`  
**Baseline**: Audited Phase 3 Baseline (`d1ec8ef`)  

---

## 1. Architecture Overview & Core Principles

The Supabase integration introduces a **Shared Remote Backend Layer** while strictly preserving the **Offline-First / Local-First** mobile architecture.

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

### Architectural Non-Negotiables
1. **Room DB is the Local Source of Truth**: UI composables observe local `Repository` StateFlow streams derived from Room DB. The UI never waits synchronously for a network response.
2. **Local-First Writes**: Products, drafts, voice replies, text messages, and commercial term confirmations write locally to Room DB first, enqueuing a `SyncOperationEntity` in the persistent outbox table.
3. **Durable Outbox Synchronization**: `SyncWorker` processes outbox operations asynchronously when connectivity is available, executing HTTP REST calls to Supabase Cloud with `clientOperationId` idempotency protection.
4. **Zero Foundation Regression**: AI Image Studio, Voice-First Listing, Dynamic Pricing Assistant, and Room + Outbox + WorkManager remain 100% frozen and operational.

---

## 2. Client Integration Technology Decision

### Option Selected: **Option C — Hybrid Retrofit/OkHttp REST + OkHttp Realtime**
- **Database & Storage & Auth**: Supabase Postgrest REST API (`/rest/v1`), Storage API (`/storage/v1`), and Auth API (`/auth/v1`) via `Retrofit 2.12` + `OkHttp 4.10` + `Moshi 1.15`.
- **Realtime**: OkHttp WebSocket / SSE listener connection to Supabase Realtime (`wss://<project>.supabase.co/realtime/v1/websocket`).

### Evidence-Based Rationale
1. **`minSdk 24` Zero-Desugaring Compatibility**: Keeps `minSdk 24` intact without forcing Java 8 desugaring or breaking low-end Android craft devices.
2. **Zero Footprint Inflation**: Reuses pre-existing Retrofit/OkHttp/Moshi dependencies in `app/build.gradle.kts`.
3. **Idempotency & Retry Safety**: Seamlessly bridges `SyncWorker` background tasks with `clientOperationId` idempotency headers.

---

## 3. Remote Schema & RLS Security

### PostgreSQL Tables & Security Rules
1. **`profiles`**: User role (`ARTISAN` / `BUYER`), full name, craft, and mobile number linked to `auth.users(id)`.
2. **`products`**: Public read access for status `'Published'` or `'Ready'`. Write access restricted to product owner (`seller_id = auth.uid()`).
3. **`conversations`**: Accessible only by conversation participants (`buyer_id = auth.uid() OR seller_id = auth.uid()`).
4. **`messages`**: Accessible only by conversation participants. Unique `client_message_id` constraint prevents duplicate insertion on retries.
5. **`product-images` Storage Bucket**: Public read for display images; write access restricted to authenticated sellers.

---

## 4. WorkManager & Outbox Integration

`SyncWorker.kt` handles outbox operation types:
- `CREATE_PRODUCT` / `UPDATE_PRODUCT`: Sends `SupabaseProductDto` to `/rest/v1/products`
- `SEND_MESSAGE`: Sends `SupabaseMessageDto` to `/rest/v1/messages` with `clientMessageId` idempotency key.
- `CONFIRM_TERMS`: Sends status update (`ORDER_READY`) to `/rest/v1/conversations`.
- `UPLOAD_IMAGE`: Uploads compressed JPEG bytes to Supabase Storage `/storage/v1/object/product-images/{filename}`.

---

## 5. Offline Acceptance Test Verification

1. **TEST 1 (Offline Product Creation)**: Product created offline -> saved to Room DB -> outbox enqueued -> app killed -> reopened -> network returns -> synced to Supabase Cloud. (**PASS**)
2. **TEST 2 (Offline Voice Negotiation)**: Voice reply recorded offline -> transcript review sheet confirmed -> saved to Room DB -> outbox enqueued -> synced on reconnect. (**PASS**)
3. **TEST 3 (Idempotent Message Retry)**: Transient network drop during send -> WorkManager retries -> Supabase receives `clientMessageId` -> single message row created. (**PASS**)
4. **TEST 4 (Order Term Locking)**: Terms agreed -> status updated to `ORDER_READY` locally and synced remotely without external payment or delivery API calls. (**PASS**)

---

## 6. Scope Boundary Confirmation

- **Payment Gateway**: `NOT IMPLEMENTED`
- **ONDC API**: `NOT IMPLEMENTED`
- **Delivery / Logistics API**: `NOT IMPLEMENTED`
- **External Marketplace Publishing**: `NOT IMPLEMENTED`
