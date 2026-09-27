# PHASE 3: BUYER ↔ ARTISAN VOICE + TEXT NEGOTIATION ANALYSIS

**PROJECT**: `artisan-ai-market-linkage`  
**APPLICATION ID**: `com.aistudio.artisanai.sih2026`  
**LATEST COMMIT**: `575b682`  
**DOCUMENT VERSION**: 1.0  

---

## 1. Current Architecture Audit
The application is built on native Android (Kotlin + Jetpack Compose + Material 3 + StateFlow + Room + WorkManager).
- **Local Source of Truth**: Room Database `AppDatabase` (Version `3`) storing `ProductEntity`, `ProductDraftEntity`, and `SyncOperationEntity`.
- **Repository Pattern**: `ArtisanRepository` routes all mutations through Write-Locally-First (`saveProductLocallyFirst`) and persistent outbox queueing.
- **Offline Outbox Engine**: `SyncOperationEntity`, `SyncManager.kt`, and WorkManager `SyncWorker.kt` with exponential backoff and idempotency protection (`clientOperationId`).

---

## 2. Existing Voice / STT / TTS Audit
- **Speech-to-Text (STT)**: [VoiceAssistEngine.kt](file:///c:/Users/hp/OneDrive/Desktop/sih2026/artisan-ai-market-linkage/app/src/main/java/com/example/ai/VoiceAssistEngine.kt) provides speech recognition via native Android `SpeechRecognizer` with craft vocabulary correction.
- **Text-to-Speech (TTS)**: `VoiceAssistEngine.speakReadBack(...)` initializes `TextToSpeech` engine and provides audio read-back.
- **Reuse Strategy**: Phase 3 will re-use `VoiceAssistEngine` for voice replies and message read-alouds without duplicating STT/TTS engines.

---

## 3. Existing Network / API Audit
- Network state monitored by `NetworkMonitor.kt` (`ONLINE`, `OFFLINE`, `UNSTABLE`, `SYNCING`).
- Network failures never block local database writes or UI interaction.

---

## 4. Existing Offline / Outbox Audit
- Outbox table `sync_operations` stores JSON payload and `clientOperationId`.
- WorkManager `SyncWorker` retries transient failures with exponential backoff.

---

## 5. Existing Room Schema Audit
- AppDatabase version `3` contains `products`, `product_drafts`, and `sync_operations`.
- Phase 3 requires database version `4` adding `conversations` and `messages` tables.

---

## 6. Existing Authentication / User Identity Audit
- `ArtisanViewModel` manages seller identity (`artisanName`, `artisanCraft`, `artisanLocation`).
- Buyer identity managed by `buyerName`, `buyerMobile`, `isBuyerLoggedIn`.

---

## 7. Existing Navigation Audit
- `AppScreen` enum manages screen routing (`HOME`, `CATALOG`, `PRODUCT_DETAIL`, `PRODUCT_EDIT`, `SELLER_PROFILE`, `BUYER_MARKETPLACE`, `PUBLIC_PRODUCT_DETAIL`).
- Phase 3 will add `CONVERSATION_LIST` and `CHAT_ROOM`.

---

## 8. Existing StateFlow / ViewModel Audit
- `ArtisanViewModel` owns reactive StateFlows observed by Jetpack Compose composables.

---

## 9. Existing Reusable Components
- `VoiceAssistEngine` (STT & TTS)
- `ArtisanRepository` & `SyncDao`
- `NetworkMonitor`
- `SyncManager` & `SyncWorker`

---

## 10. Required New Components
- `ConversationEntity.kt` & `MessageEntity.kt`
- `ConversationDao.kt` & `MessageDao.kt`
- `TtsQueueManager.kt` (Controlled TTS queue for message read-aloud)
- `ChatRoomScreen.kt` (Voice + Text negotiation UI)
- `ConversationListScreen.kt` (Enquiry inbox UI)

---

## 11. Required New Data Models

### `ConversationEntity`
```kotlin
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val productId: Long,
    val productName: String,
    val buyerId: String,
    val buyerName: String,
    val artisanName: String,
    val currentStatus: String = "ENQUIRING", // "ENQUIRING", "NEGOTIATING", "NEGOTIATION_CONFIRMED", "ORDER_READY"
    val agreedQuantity: Int = 0,
    val agreedUnitPrice: Double = 0.0,
    val buyerConfirmed: Boolean = false,
    val sellerConfirmed: Boolean = false,
    val lastMessageText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
```

### `MessageEntity`
```kotlin
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val clientMessageId: String, // Idempotency key
    val senderId: String,
    val senderType: String, // "BUYER" or "SELLER"
    val text: String,
    val language: String = "hi",
    val messageType: String = "TEXT", // "TEXT", "VOICE_TRANSCRIPT", "OFFER", "CONFIRMATION"
    val extractedQuantity: Int? = null,
    val extractedPrice: Double? = null,
    val status: String = "SENT", // "PENDING", "QUEUED_OFFLINE", "SENT", "DELIVERED", "READ"
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 12. Room Migration Impact
- **Database Version**: Upgrade from `3` to `4`.
- **Migration Strategy**: Auto-migration / destructive fallback migration for development compatibility.
- **Existing Records**: Existing products, drafts, and outbox operations remain 100% intact.

---

## 13. Repository Impact
- Add `saveMessageLocallyFirst(context, message)` to `ArtisanRepository`.
- Add `getConversations()`, `getMessagesForConversation(convId)`, `confirmNegotiationTerms()`.

---

## 14. Sync Architecture Impact
- Outbox operations created with `operationType = "SEND_MESSAGE"` and `clientOperationId = message.clientMessageId`.
- Reuses existing `SyncWorker` and idempotency deduplication logic.

---

## 15–18. Risks & Mitigations (Performance, Memory, Battery, Low Network)
- **Lazy Rendering**: Message list rendered using `LazyColumn` with explicit keying `key = { it.messageId }`.
- **Low Payload**: Text and transcripts sent as lightweight JSON (< 1 KB).
- **TTS Queue Control**: `TtsQueueManager` manages single/sequential playback to prevent overlapping TTS audio threads.

---

## 19–20. Duplicate & Ordering Risks
- **Idempotency Key**: Every message generates `clientMessageId = "MSG-${convId}-${senderId}-${createdAt}"`.
- **Deterministic Ordering**: Messages sorted by `createdAt ASC` in SQL query.

---

## 21–26. Voice, Failure & Accessibility Risks
- **Transcript Review**: Seller MUST review transcript before sending (Voice reply $\rightarrow$ Preview sheet $\rightarrow$ Edit/Re-record $\rightarrow$ Send).
- **Text Always Available**: Typing remains 100% accessible alongside voice.

---

## 27–30. Technology Selection & Decision Gate

| Capability | Chosen Technology | Rationale |
| :--- | :--- | :--- |
| **STT** | Native `SpeechRecognizer` via `VoiceAssistEngine` | Reuses verified engine, 0 new dependencies. |
| **TTS** | Native `TextToSpeech` via `TtsQueueManager` | Controlled queue, 0 memory leaks. |
| **Local DB** | Room Database (v4) | Local source of truth, reactive StateFlows. |
| **Outbox Sync** | Room Outbox + WorkManager `SyncWorker` | Offline-first, idempotent retry. |

---

## 31. Step-by-Step Implementation Order

1. **Entities & DAOs**: Create `ConversationEntity`, `MessageEntity`, `ConversationDao`, `MessageDao`.
2. **Database Version Upgrade**: Upgrade `AppDatabase` to version `4`.
3. **Repository Methods**: Add local-first messaging and negotiation term confirmation to `ArtisanRepository`.
4. **TTS Queue Manager**: Build `TtsQueueManager.kt` for controlled message read-aloud.
5. **ViewModel Extension**: Add conversation flows and active conversation selection in `ArtisanViewModel`.
6. **Compose Screens**: Build `ChatRoomScreen.kt` and `ConversationListScreen.kt`.
7. **Enquiry Entry Points**: Update `PublicProductDetailScreen` & `BuyerMarketplaceScreen` with `[ Enquire / Negotiate ]` button.
8. **Automated Unit Tests**: Expand `OfflineFirstArchitectureTest.kt` for Phase 3 messaging, voice replies, outbox queueing, and term confirmations.

---

## 32. Test Plan
- Unit tests for local message insertion, outbox queueing, transcript preview, and commercial term extraction.
- End-to-end manual validation flows A through G.

---

## 33. Rollback Plan
- Revert commit if regression occurs; core frozen foundations remain separate and isolated.
