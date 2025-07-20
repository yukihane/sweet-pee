# Mock HTTP Server

スクレイピングアプリケーションのテスト用モックサーバー

## クイックスタート

```bash
# 依存関係インストール
npm install

# サーバー起動
npm start

# 開発モード（自動再起動）
npm run dev
```

## テスト

```bash
# 基本エンドポイント
curl http://localhost:3000/

# 商品一覧（100ms遅延）
curl http://localhost:3000/products

# API データ（50ms遅延）
curl http://localhost:3000/api/data

# 404エラーテスト
curl http://localhost:3000/nonexistent
```

## 設定

`config/endpoints.json` でエンドポイントを設定:

- `responseFile`: 返すファイルのパス
- `headers`: カスタムヘッダー
- `statusCode`: HTTPステータスコード
- `delay`: レスポンス遅延（ミリ秒）

## ファイル構造

```
mock-server/
├── config/endpoints.json    # エンドポイント設定
├── responses/              # レスポンスファイル
├── src/server.js           # メインサーバー
└── package.json           # 依存関係
```