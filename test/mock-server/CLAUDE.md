# テスト用モックHTTPサーバー

## プロジェクト概要

スクレイピングアプリケーションのテスト用に、実際のHTTPサーバーの動作を模倣するローカルモックサーバーを作成します。このサーバーは、設定ファイルベースで柔軟にエンドポイントとレスポンスを管理し、スクレイピングアプリケーションが正しくリクエストを送信し、結果をパースできるかを検証するために使用されます。

## 技術要件

- **プラットフォーム**: Node.js
- **フレームワーク**: Express
- **設定管理**: JSON形式の設定ファイル
- **レスポンス**: 静的HTMLファイルの配信
- **ログ**: リクエスト情報のコンソール出力

## ディレクトリ構造

```
mock-server/
├── config/
│   └── endpoints.json     # エンドポイント設定ファイル
├── responses/
│   ├── sample1.html       # サンプルレスポンスHTML
│   ├── sample2.html       # サンプルレスポンスHTML
│   └── error404.html      # エラー用HTML
├── src/
│   └── server.js          # メインサーバーファイル
├── package.json           # 依存関係とスクリプト
├── README.md              # 使用方法とドキュメント
└── .gitignore             # Git除外ファイル
```

## 機能要件

### 1. 設定ベースのエンドポイント管理

`config/endpoints.json` で以下の形式でエンドポイントを定義：

```json
{
  "/": {
    "responseFile": "responses/sample1.html",
    "headers": {
      "Content-Type": "text/html; charset=utf-8",
      "X-Server": "Mock-Server",
      "Cache-Control": "no-cache"
    },
    "statusCode": 200,
    "delay": 0
  },
  "/products": {
    "responseFile": "responses/sample2.html",
    "headers": {
      "Content-Type": "text/html; charset=utf-8",
      "X-Custom-Header": "product-page"
    },
    "statusCode": 200,
    "delay": 100
  },
  "/api/data": {
    "responseFile": "responses/api_data.json",
    "headers": {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*"
    },
    "statusCode": 200,
    "delay": 50
  }
}
```

### 2. サーバー機能

- **ポート**: デフォルト3000（環境変数PORT で変更可能）
- **リクエストログ**: 以下の情報をコンソール出力
  - タイムスタンプ
  - HTTPメソッド
  - リクエストパス
  - ユーザーエージェント
  - IPアドレス
- **レスポンス遅延**: 設定でレスポンス遅延を模倣可能
- **エラーハンドリング**: 設定されていないパスは404エラー

### 3. レスポンス機能

- **静的ファイル配信**: `responses/` ディレクトリのファイルを配信
- **カスタムヘッダー**: エンドポイント毎に自由にヘッダー設定
- **ステータスコード**: エンドポイント毎に設定可能
- **CORS対応**: 必要に応じてCORSヘッダー追加

### 4. 運用機能

- **起動**: `npm start` または `node src/server.js`
- **開発モード**: `npm run dev`（ファイル変更時の自動再起動）
- **設定リロード**: サーバー再起動なしで設定変更反映（可能であれば）

## サンプルレスポンスファイル

### responses/sample1.html
典型的なWebページの構造を持つサンプル：
- DOCTYPE宣言
- head セクション（title、meta タグ）
- body セクション（header、main、footer）
- スクレイピング対象となるような要素（商品リスト、価格情報など）

### responses/sample2.html
異なる構造のサンプル：
- 異なるHTML構造
- テーブル形式のデータ
- フォーム要素

## 非機能要件

- **パフォーマンス**: 軽量で高速な起動
- **拡張性**: 新しいエンドポイントの追加が容易
- **保守性**: 設定とコードの分離
- **テスト性**: 単体テストが書きやすい構造

## 使用例

```bash
# インストール
npm install

# サーバー起動
npm start

# 別ターミナルでテスト
curl http://localhost:3000/
curl -H "User-Agent: MyBot/1.0" http://localhost:3000/products
```

## 開発時の注意事項

- レスポンスファイルは実際のWebサイトの構造を模倣
- ヘッダー情報は実際のサーバーが返すものに近づける
- エラーケースも適切に処理
- ログ出力でデバッグしやすくする
- 設定ファイルの検証機能を含める

## テストシナリオ

1. **基本動作確認**
   - 各エンドポイントが正しいレスポンスを返すか
   - ヘッダーが正しく設定されているか
   - ステータスコードが正しいか

2. **エラーハンドリング**
   - 存在しないパスへのアクセス
   - 存在しないレスポンスファイル

3. **パフォーマンス**
   - 遅延設定が正しく動作するか
   - 同時リクエストの処理

このモックサーバーは、スクレイピングアプリケーションの開発とテストを効率的に行うために設計されています。実際のWebサイトに依存することなく、一貫したテスト環境を提供します。
