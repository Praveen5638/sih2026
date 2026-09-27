# PHASE 2: PRODUCT CATALOG + ARTISAN STORE ANALYSIS

**PROJECT**: `artisan-ai-market-linkage`  
**APPLICATION ID**: `com.aistudio.artisanai.sih2026`  
**FOUNDATION COMMIT**: `a7cbeb0`  
**DOCUMENT VERSION**: 1.0  

---

## SECTION 1 — Existing Architecture Based on Inspected Code

The codebase is a native Android application written in **Kotlin** using **Jetpack Compose**, **Material 3**, **Room**, **WorkManager**, and **Coroutines / StateFlow**.

### Inspected Codebase Components:
1. **Local Source of Truth**: Room Database `AppDatabase` (Version `3`) containing `ProductEntity`, `ProductDraftEntity`, and `SyncOperationEntity`.
2. **Data Layer**: `ProductDao`, `DraftDao`, `SyncDao`, and `ArtisanRepository`. All data writes flow through `ArtisanRepository.saveProductLocallyFirst(...)`.
3. **State Management**: `ArtisanViewModel` exposes `allProducts: StateFlow<List<ProductEntity>>`, `networkState: StateFlow<NetworkState>`, `recoverableDraft: ProductDraftEntity?`, and UI navigation state (`currentScreen: AppScreen`).
4. **Frozen Foundations**:
   - **Image Studio**: `ProductImageProcessor.kt` for full-res capture, saliency segmentation, CIE $L^*a^*b^*$ color drift validation ($\Delta E_{ab}$), and conservative fallback.
   - **Voice Listing**: `VoiceAssistEngine.kt` for speech recognition, craft vocabulary correction, context-aware slot extraction, and TTS audio read-back.
   - **Dynamic Pricing**: `DynamicPricingEngine.kt` for deterministic cost floor math ($\text{Material} + \text{Labour} + \text{Packaging/Transport}$), similarity scoring against curated Indian handicraft comparables, robust percentiles ($P_{25}, P_{50}, P_{75}$), and conflict detection.
   - **Offline Architecture**: `NetworkMonitor.kt`, persistent outbox `SyncOperationEntity`, `SyncManager.kt`, and WorkManager `SyncWorker.kt` with exponential backoff and idempotency protection (`clientOperationId`).

---

## SECTION 2 — Existing Reusable Components

The following components MUST be reused without duplication:

| Component | Path | Purpose |
| :--- | :--- | :--- |
| `ProductEntity` | `com.example.data.ProductEntity` | Single authoritative product model. |
| `ProductDao` | `com.example.data.ProductDao` | Reactive Flow queries (`getAllProducts()`, `getProductById()`). |
| `ArtisanRepository` | `com.example.data.ArtisanRepository` | Local-first data operations & outbox enqueueing. |
| `ArtisanViewModel` | `com.example.viewmodel.ArtisanViewModel` | Shared StateFlow owner across Compose screens. |
| `AppDatabase` | `com.example.data.AppDatabase` | Room DB instance (Version `3`). |
| `NetworkMonitor` | `com.example.network.NetworkMonitor` | Real-time network status monitoring. |
| `SyncManager` / `SyncWorker` | `com.example.network.SyncWorker` | WorkManager background sync. |
| `VoiceAssistEngine` | `com.example.ai.VoiceAssistEngine` | Voice command classification & TTS read-back. |

---

## SECTION 3 — Data Model Analysis

The existing `ProductEntity` structure is as follows:

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val category: String,
    val craft: String,
    val material: String,
    val technique: String,
    val color: String,
    val dimensions: String,
    val productionTime: String,
    val descriptionHi: String,
    val descriptionEn: String,
    val seoTags: String,
    val originalImageUrl: String,
    val enhancedImageUrl: String,
    val materialCost: Double,
    val labourCost: Double,
    val otherCost: Double,
    val costFloor: Double,
    val recommendedPrice: Double,
    val sellingPrice: Double,
    val marketP25: Double = 0.0,
    val marketMedian: Double = 0.0,
    val marketP75: Double = 0.0,
    val marketConfidence: String = "MEDIUM",
    val pricingEngineVersion: String = "v1-hardened",
    val status: String, // "Draft", "Ready", "Published"
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## SECTION 4 — Required New Data & Fields

All essential product catalog and store attributes already exist inside `ProductEntity`.
- **Product Information**: `id`, `productName`, `category`, `craft`, `material`, `technique`, `color`, `dimensions`, `productionTime`, `descriptionHi`, `descriptionEn`, `seoTags`.
- **Image References**: `originalImageUrl`, `enhancedImageUrl`.
- **Pricing Breakdown**: `materialCost`, `labourCost`, `otherCost`, `costFloor`, `recommendedPrice`, `sellingPrice`, `marketP25`, `marketMedian`, `marketP75`, `marketConfidence`, `pricingEngineVersion`.
- **Status & Metadata**: `status` ("Draft", "Ready", "Published"), `createdAt`.

**Conclusion**: NO new database columns are strictly required for Phase 2. Reusing existing fields prevents unnecessary migration complexity and ensures 100% backward compatibility.

---

## SECTION 5 — Room Database Impact & Migration Plan

- **Schema Change Required**: NO.
- **Current Database Version**: `3`.
- **Data Integrity**: Existing database records remain 100% readable and valid.
- **Migration Risk**: ZERO.

---

## SECTION 6 — Navigation Impact

Update `AppScreen` enum in `ArtisanViewModel.kt` to include `PRODUCT_EDIT` for safe product editing:

```kotlin
enum class AppScreen {
    HOME,
    CATALOG,
    PRODUCT_DETAIL,
    PRODUCT_EDIT,
    SELLER_PROFILE,
    CREATE_PRODUCT,
    IMAGE_STUDIO,
    VOICE_CATALOGER,
    AI_REVIEW,
    PRICING_ASSISTANT,
    FINAL_PREVIEW,
    BUYER_MARKETPLACE,
    PUBLIC_PRODUCT_DETAIL,
    ...
}
```

---

## SECTION 7 — State Management Impact

- **Catalog State**: `viewModel.allProducts` observed via Jetpack Compose `collectAsState()`.
- **Product Detail State**: `viewModel.selectedProductIdForDetail` identifies the currently selected product.
- **Edit Product State**: Form fields initialized from target `ProductEntity`, updated in memory, and persisted via `viewModel.updateProduct(updatedEntity)`.
- **Artisan Store/Profile State**: Bound to `artisanName`, `artisanCraft`, `artisanLocation`, `sellerMobile`, and `allProducts`.

---

## SECTION 8 — Offline-First Impact Matrix

| User Action | Online Behavior | Offline Behavior | Process Death Recovery |
| :--- | :--- | :--- | :--- |
| **Browse Catalog** | Loads Room DB products instantly | Loads Room DB products instantly | Restores from Room DB |
| **View Product Detail** | Reads selected `ProductEntity` | Reads selected `ProductEntity` | Restores selected ID & entity |
| **Edit Product** | Updates Room DB + enqueues sync | Updates Room DB + enqueues sync | Changes preserved in Room |
| **View Artisan Store** | Renders profile & catalog | Renders profile & catalog | Restores from ViewModel/Room |
| **Delete/Archive** | Deletes from Room + enqueues sync | Deletes from Room + enqueues sync | State updated in Room |

---

## SECTION 9 — Locked Foundation Safety Plan

1. **AI Image Studio**: Untouched. Catalog uses stored `enhancedImageUrl` / `originalImageUrl` string references.
2. **Voice-First Listing**: Untouched. Catalog uses persisted catalog descriptions (`descriptionHi`, `descriptionEn`).
3. **Dynamic Pricing Assistant**: Untouched. Catalog and product detail display stored pricing breakdown fields (`costFloor`, `marketP25-P75`, `recommendedPrice`, `sellingPrice`).
4. **Offline Architecture**: Untouched. Edits call `ArtisanRepository.saveProductLocallyFirst(...)` or `updateProduct(...)` with `SyncOperationEntity` outbox enqueueing.

---

## SECTION 10 — Performance Risk Analysis

1. **Lazy Rendering**: Use `LazyVerticalGrid` or `LazyColumn` for catalog browsing with unique keying (`key = { it.id }`).
2. **Image Decoding**: Render image URIs with Coil `AsyncImage` or native bitmap resolution bounding to prevent high RAM consumption.
3. **Room Query Frequency**: Rely on Room reactive `Flow<List<ProductEntity>>` with `stateIn(WhileSubscribed(5000))` in `ArtisanViewModel`.

---

## SECTION 11 — Failure Modes & Mitigations

| Failure Mode | Root Cause | Architectural Mitigation |
| :--- | :--- | :--- |
| **Missing Image File** | Deleted local cache file | Display placeholder image UI card without crashing. |
| **Stale StateFlow** | UI recomposition delay | Observe StateFlow directly via `collectAsState()`. |
| **Empty Catalog** | No products saved yet | Renders low-literacy friendly empty state with `[+ Add Product]` CTA. |
| **Concurrent Edit** | Rapid user edits | Atomic Room `updateProduct(...)` call. |

---

## SECTION 12 — Step-by-Step Implementation Plan

1. **Enum Update**: Add `PRODUCT_EDIT` to `AppScreen` in `ArtisanViewModel.kt`.
2. **ViewModel Methods**: Add `updateProduct(product: ProductEntity)` in `ArtisanViewModel.kt`.
3. **Catalog UI Upgrade**: Refine `CatalogScreen` in `CatalogAndDetailScreens.kt` with grid/list layout, status badges, price display, image thumbnails, and search/filter.
4. **Product Detail UI Upgrade**: Refine `ProductDetailScreen` with image display, bilingual descriptions, cost vs. market pricing breakdown, status badge, and `[ Edit Product ]` CTA.
5. **Product Edit UI Implementation**: Create `ProductEditScreen` for editing product attributes, selling price, and status.
6. **Artisan Store / Profile UI Implementation**: Create `SellerProfileScreen` displaying artisan profile header, craft specialization, statistics, storefront grid, and edit profile action.
7. **Regression & Integration Tests**: Run `OfflineFirstArchitectureTest.kt` and manual validation flows.

---

## SECTION 13 — Test Plan

### Unit & Integration Tests:
- `testCatalog_readsProductsFromRoom()`
- `testProductEdit_updatesRoomAndEnqueuesSync()`
- `testOfflineCatalog_accessibleWithoutNetwork()`

### Manual Verification Flows:
- **Flow A**: Add Product $\rightarrow$ Studio $\rightarrow$ Voice Listing $\rightarrow$ Pricing $\rightarrow$ Catalog $\rightarrow$ Product Detail.
- **Flow B**: Catalog $\rightarrow$ Edit Product $\rightarrow$ Save $\rightarrow$ Process Death $\rightarrow$ Reopen $\rightarrow$ Edits Intact.
- **Flow C**: Network OFF $\rightarrow$ Browse Catalog $\rightarrow$ Edit Product $\rightarrow$ Save $\rightarrow$ Reconnect $\rightarrow$ WorkManager Sync.
