# システムパターン

## アーキテクチャ概要

### レイヤー構成

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│  ┌─────────────┐  ┌─────────────┐   │
│  │     UI      │  │ ViewModel   │   │
│  └─────────────┘  └─────────────┘   │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│            Domain Layer             │
│  ┌─────────────┐  ┌─────────────┐   │
│  │   UseCase   │  │    Model    │   │
│  └─────────────┘  └─────────────┘   │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│             Data Layer              │
│ ┌──────────┐ ┌──────────┐ ┌───────┐ │
│ │Repository│ │ Network  │ │  DB   │ │
│ └──────────┘ └──────────┘ └───────┘ │
└─────────────────────────────────────┘
```

### ディレクトリ構造

```
app/
├── data/
│   ├── repository/
│   │   ├── BloodGlucoseRepository.kt
│   │   └── BloodGlucoseRepositoryImpl.kt
│   ├── network/
│   │   ├── WelbyApiService.kt
│   │   ├── WelbyScrapingService.kt
│   │   └── dto/
│   ├── database/
│   │   ├── BloodGlucoseDao.kt
│   │   ├── BloodGlucoseEntity.kt
│   │   └── AppDatabase.kt
│   └── healthconnect/
│       ├── HealthConnectManager.kt
│       └── BloodGlucoseWriter.kt
├── domain/
│   ├── model/
│   │   ├── BloodGlucoseReading.kt
│   │   └── SyncResult.kt
│   └── usecase/
│       ├── SyncBloodGlucoseUseCase.kt
│       └── GetBloodGlucoseHistoryUseCase.kt
└── presentation/
    ├── ui/
    │   ├── MainActivity.kt
    │   ├── SyncFragment.kt
    │   └── HistoryFragment.kt
    └── viewmodel/
        ├── SyncViewModel.kt
        └── HistoryViewModel.kt
```

## 主要コンポーネント設計

### データモデル

```kotlin
// Domain Model
data class BloodGlucoseReading(
    val id: String,
    val timestamp: LocalDateTime,
    val valueMgDl: Double,
    val valueMMolL: Double = valueMgDl * 0.0555,
    val source: String = "welby",
    val syncedToHealthConnect: Boolean = false
)

// Database Entity
@Entity(tableName = "blood_glucose_readings")
data class BloodGlucoseEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val valueMgDl: Double,
    val valueMMolL: Double,
    val source: String,
    val syncedToHealthConnect: Boolean
)
```

### Repository パターン

```kotlin
interface BloodGlucoseRepository {
    suspend fun fetchFromWelby(): List<BloodGlucoseReading>
    suspend fun saveToLocal(readings: List<BloodGlucoseReading>)
    suspend fun getUnsyncedReadings(): List<BloodGlucoseReading>
    suspend fun markAsSynced(readingIds: List<String>)
}

class BloodGlucoseRepositoryImpl(
    private val welbyService: WelbyScrapingService,
    private val dao: BloodGlucoseDao,
    private val healthConnectManager: HealthConnectManager
) : BloodGlucoseRepository {
    // 実装詳細
}
```

## データフロー

### 同期プロセス

```
1. User Trigger
   ↓
2. SyncUseCase.execute()
   ↓
3. Repository.fetchFromWelby()
   ↓
4. WelbyScrapingService.scrapeData()
   ↓
5. Repository.saveToLocal()
   ↓
6. HealthConnectManager.writeData()
   ↓
7. Repository.markAsSynced()
   ↓
8. UI Update
```

### エラーハンドリングフロー

```
Error Occurred
   ↓
Exception Caught
   ↓
Error Classification
   ├── Network Error → Retry Logic
   ├── Auth Error → Re-login Required
   ├── Parse Error → Skip & Log
   └── Health Connect Error → Retry Later
   ↓
User Notification
```

## 重要な設計パターン

### 1. Repository パターン

- データソースの抽象化
- テスタビリティの向上
- データアクセスロジックの集約

### 2. UseCase パターン

- ビジネスロジックの分離
- 単一責任の原則
- 再利用可能性の向上

### 3. MVVM パターン

- UI とビジネスロジックの分離
- データバインディングの活用
- ライフサイクル対応

### 4. Dependency Injection

```kotlin
// Hilt を使用した DI 設定例
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBloodGlucoseRepository(
        welbyService: WelbyScrapingService,
        dao: BloodGlucoseDao,
        healthConnectManager: HealthConnectManager
    ): BloodGlucoseRepository = BloodGlucoseRepositoryImpl(
        welbyService, dao, healthConnectManager
    )
}
```

## 非同期処理パターン

### Coroutines 使用方針

```kotlin
class SyncViewModel : ViewModel() {
    private val _syncState = MutableLiveData<SyncState>()
    val syncState: LiveData<SyncState> = _syncState

    fun startSync() {
        viewModelScope.launch {
            try {
                _syncState.value = SyncState.Loading
                val result = syncUseCase.execute()
                _syncState.value = SyncState.Success(result)
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.message)
            }
        }
    }
}
```

## セキュリティパターン

### 認証情報管理

```kotlin
class SecureCredentialManager(private val context: Context) {
    private val keyAlias = "welby_credentials"

    fun saveCredentials(username: String, password: String) {
        // Android Keystore を使用した暗号化保存
    }

    fun getCredentials(): Pair<String, String>? {
        // 暗号化された認証情報の復号化
    }
}
```

### HTTPS 通信の強制

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

## テストパターン

### Unit Test 構造

```kotlin
class BloodGlucoseRepositoryTest {
    @Mock private lateinit var welbyService: WelbyScrapingService
    @Mock private lateinit var dao: BloodGlucoseDao
    @Mock private lateinit var healthConnectManager: HealthConnectManager

    private lateinit var repository: BloodGlucoseRepository

    @Test
    fun `fetchFromWelby should return parsed readings`() = runTest {
        // Given
        val mockHtml = "<html>...</html>"
        whenever(welbyService.scrapeData()).thenReturn(mockHtml)

        // When
        val result = repository.fetchFromWelby()

        // Then
        assertThat(result).isNotEmpty()
    }
}
```

## パフォーマンス最適化パターン

### データベース最適化

- インデックスの適切な設定
- バッチ処理による効率化
- 不要データの定期削除

### ネットワーク最適化

- 適切なキャッシュ戦略
- リクエストの最小化
- 圧縮の活用

### メモリ最適化

- 適切なライフサイクル管理
- WeakReference の活用
- リソースの適切な解放
