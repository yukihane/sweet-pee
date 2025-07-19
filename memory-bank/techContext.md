# 技術コンテキスト

## 使用技術スタック

### 開発言語・フレームワーク

- **Kotlin**: メイン開発言語（ユーザーはサーバーサイドKotlinの経験あり）
- **Android SDK**: ネイティブAndroidアプリ開発
- **最小SDK**: API 26 (Android 8.0) - Health Connect対応要件

### 主要ライブラリ・依存関係

```kotlin
dependencies {
    // HTTP通信
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // HTML解析（Webスクレイピング）
    implementation("org.jsoup:jsoup:1.17.2")

    // Google Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

    // ローカルデータベース
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // 非同期処理
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

## 開発環境

### 現在の状況

- **Android Studio**: ✅ セットアップ済み（JetBrains Toolbox経由）
- **エミュレータ**: ✅ 動作確認済み（Hello Worldアプリ実行済み）
- **実機**: 利用可能（最終テスト用）
- **開発経験**: Kotlin（サーバーサイド）経験あり、Android開発学習中

### セットアップ済み項目

- Android Studio最新版（JetBrains Toolbox管理）
- Android SDK設定完了
- エミュレータ動作確認完了（API 26対応）

## 技術的制約

### プラットフォーム制約

- Android端末での完結が必須
- Google Play Servicesが必要（Health Connect）
- 最小API 26以上（Health Connect要件）

### セキュリティ制約

- 医療データの適切な取り扱い
- 認証情報の安全な保存（Android Keystore使用）
- HTTPS通信の確保
- 個人情報保護法の遵守

### 外部サービス制約

- welbyマイカルテの利用規約遵守
- スクレイピング頻度の適切な制限
- サイト構造変更への対応必要性

## アーキテクチャ選択の理由

### Webスクレイピングアプローチ

**選択理由**:

- 医療機器の直接通信プロトコルは非公開
- 他アプリのデータ直接読み取りは技術的に不可能
- welbyマイカルテのWeb版が最もアクセス可能

**技術的利点**:

- 実装が比較的容易
- 既存のWebインターフェースを活用
- Android端末で完結可能

### データ変換要件

- **入力**: mg/dL（日本の一般的な単位）
- **出力**: mmol/L（Google Health Connect標準）
- **変換式**: 1 mg/dL = 0.0555 mmol/L

## 依存関係の詳細

### OkHttp + Retrofit

- **用途**: welbyマイカルテとのHTTP通信
- **選択理由**: Android開発の標準的なHTTP クライアント

### Jsoup

- **用途**: HTMLパース・スクレイピング
- **選択理由**: Javaエコシステムで最も信頼性の高いHTMLパーサー

### Room Database

- **用途**: ローカルデータ保存・重複防止
- **選択理由**: Android推奨のローカルDB、SQLiteのラッパー

### Health Connect SDK

- **用途**: Googleヘルスコネクトとの連携
- **選択理由**: 公式SDK、血糖値データ型をサポート

## 開発ツール・設定

### ビルド設定

- Kotlin DSL（build.gradle.kts）使用
- ProGuard/R8による難読化（リリース時）
- 署名設定（Google Play配布用）

### 開発支援ツール

- Android Studio Debugger
- Layout Inspector（UI確認）
- Network Inspector（HTTP通信確認）
- Device File Explorer（データ確認）

## パフォーマンス考慮事項

### ネットワーク

- 適切なタイムアウト設定
- リトライ機構の実装
- キャッシュ戦略

### バッテリー

- バックグラウンド処理の最適化
- 同期頻度の調整
- Doze modeへの対応

### メモリ

- 大量データ処理時のメモリ管理
- 適切なライフサイクル管理
