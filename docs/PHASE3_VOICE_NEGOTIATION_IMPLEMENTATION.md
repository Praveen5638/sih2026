# Phase 3 Implementation Summary: Buyer ↔ Artisan Voice & Text Negotiation

## Executive Overview
Phase 3 extends the **artisan-ai-market-linkage** platform with a **Voice-First + Text-Capable Negotiation Engine**. This capability empowers buyers and artisans to converse naturally, negotiate order terms (quantities and unit prices), review voice transcripts before dispatch, and lock mutual commercial terms (`ORDER_READY`) with local-first persistence and zero data loss.

---

## Architectural Compliance & Governance
All changes strictly adhere to the frozen foundation rules:
1. **Locked Core Foundations Preserved**:
   - **AI Image Studio**: Untouched.
   - **Voice-First Product Listing**: `VoiceAssistEngine` reused without duplicate speech recognizers.
   - **Dynamic Pricing Assistant**: Untouched.
   - **Offline-First Architecture**: Authoritative local storage in Room DB with persistent outbox queue (`SyncOperationEntity` + `SyncWorker`) using `clientMessageId` for idempotency protection.
2. **Controlled Audio Management**:
   - Audio output managed via `TtsQueueManager` with non-blocking StateFlow audio queues.
   - Audio turn enforcement: Speech recognition stops TTS before opening microphone.
3. **Mandatory Transcript & Terms Review**:
   - Voice replies **never** auto-send. They open `VoiceReplyPreviewDialog` allowing the user to review/edit the transcript and extracted commercial terms (`extractedQuantity`, `extractedUnitPrice`) prior to dispatch.
4. **Scope Boundaries Preserved**:
   - Negotiations stop at `ORDER_READY` term locking without external ONDC API, payment, or logistics gateway calls.

---

## Key Data & Component Implementations

### 1. Data Schema & Persistence Layer (Room DB v4)
- **`ConversationEntity.kt`** (`conversations` table):
  - Primary Key: `conversationId` (`CONV-<productId>-<buyerId>`)
  - Fields: `productId`, `productName`, `buyerId`, `buyerName`, `artisanName`, `currentStatus` (`ENQUIRING`, `NEGOTIATING`, `ORDER_READY`), `agreedQuantity`, `agreedUnitPrice`, `buyerConfirmed`, `sellerConfirmed`, `lastMessageText`, `updatedAt`.
- **`MessageEntity.kt`** (`messages` table):
  - Primary Key: `messageId`
  - Idempotency Key: `clientMessageId`
  - Fields: `conversationId`, `senderId`, `senderType` (`BUYER`/`SELLER`), `text`, `language`, `messageType` (`TEXT`, `VOICE_TRANSCRIPT`, `OFFER`, `CONFIRMATION`), `extractedQuantity`, `extractedPrice`, `status` (`PENDING`, `SENT`), `createdAt`.
- **DAOs**: `ConversationDao.kt` & `MessageDao.kt`.
- **AppDatabase Migration**: Upgraded database schema to Version 4 with new table entities.

### 2. Audio Queue Management
- **`TtsQueueManager.kt`**:
  - Single-message and queued multi-message TTS playback controller.
  - Exposes `isSpeaking: StateFlow<Boolean>` for reactive UI updates.

### 3. Repository & ViewModel Logic
- **`ArtisanRepository.kt`**:
  - `getOrCreateConversation(...)`: Retrieves existing thread or creates new conversation entity.
  - `sendMessageLocallyFirst(...)`: Performs write-locally-first to Room DB, updates `conversations` entity, enqueues `SyncOperationEntity` outbox sync task with `clientMessageId` for idempotency, and triggers `SyncManager.triggerSync(...)`.
  - `confirmNegotiationTerms(...)`: Updates conversation status to `ORDER_READY`, locks agreed terms, and sends confirmation system message.
- **`ArtisanViewModel.kt`**:
  - Added `CONVERSATION_LIST` and `CHAT_ROOM` screens to `AppScreen` enum.
  - Exposes reactive `allConversations`, `activeConversation`, and `activeMessages` StateFlows derived from Compose `snapshotFlow`.
  - Implemented `startVoiceReplyListening(...)`, `sendVoiceReplyFromPreview(...)`, `sendTextMessage(...)`, `speakMessageWithTts(...)`, and `confirmTerms(...)`.

### 4. Jetpack Compose UI Screens
- **`ConversationListScreen.kt`**:
  - Inbox view displaying active customer enquiries and negotiation threads.
  - Displays product name, buyer name, last message preview, and status pill (`Enquiry 💬`, `Negotiating 🤝`, `Order Ready ✅`).
- **`ChatRoomScreen.kt`**:
  - Pinned Commercial Negotiation Terms Banner displaying agreed quantity, unit price, total valuation, and `[ ✅ Confirm & Lock Terms ]` button.
  - Message bubble list with buyer vs seller layout styling, extracted terms pills, outbox sync indicators, and individual `[ 🔊 सुनाओ ]` TTS read-aloud buttons.
  - Bottom bar with `[ 🎙 Voice Reply ]` and text input.
  - `VoiceReplyPreviewDialog`: Mandatory preview bottom sheet showing transcribed text and extracted quantity/price inputs before sending.

### 5. Integration Entry Points
- **`PublicProductDetailScreen.kt`**:
  - Added **`[ 💬 Voice / Text Negotiate with Artisan ]`** CTA button.
- **`HomeScreen.kt`**:
  - Added **`[ 💬 Customer Enquiries & Negotiations ]`** dashboard card and navigation bar item.
- **`MainActivity.kt`**:
  - Updated screen navigation switch statement for `CONVERSATION_LIST` and `CHAT_ROOM`.

---

## Unit Testing & Verification
Unit tests added in `OfflineFirstArchitectureTest.kt`:
1. `testPhase3VoiceNegotiation_extractsCommercialTerms()`: Verifies regex/slot extraction of quantity and unit price from Hindi/Hinglish transcripts.
2. `testPhase3VoiceNegotiation_localMessageCreationAndOutboxSync()`: Verifies local-first message creation and `clientMessageId` idempotency protection.
3. `testPhase3VoiceNegotiation_confirmTerms_updatesStatusToOrderReady()`: Verifies term locking and state transition to `ORDER_READY`.

---

## Summary Statement
Phase 3 implementation is 100% complete, fully backward-compatible with frozen core foundations, and ready for end-to-end testing.
