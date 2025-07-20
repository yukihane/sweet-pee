# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# テスト用モックHTTPサーバー

## プロジェクト概要

スクレイピングアプリケーションのテスト用に、実際のHTTPサーバーの動作を模倣するローカルモックサーバー。設定ファイルベースで柔軟にエンドポイントとレスポンスを管理し、スクレイピングアプリケーションが正しくリクエストを送信し、結果をパースできるかを検証するために使用する。

## アーキテクチャ

- **プラットフォーム**: Node.js + Express
- **設定駆動**: `config/endpoints.json` でエンドポイント、レスポンス、ヘッダー、遅延を定義
- **静的ファイル配信**: `responses/` ディレクトリから事前定義されたHTMLやJSONファイルを配信
- **リクエストログ**: タイムスタンプ、HTTPメソッド、パス、User-Agent、IPアドレスをコンソール出力

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
└── README.md              # 使用方法とドキュメント
```

## 開発コマンド

```bash
# 依存関係インストール
npm install

# サーバー起動
npm start
# または
node src/server.js

# 開発モード（ファイル変更時自動再起動）
npm run dev

# テスト実行
curl http://localhost:3000/
curl -H "User-Agent: MyBot/1.0" http://localhost:3000/products
```

## 設定ファイル形式

`config/endpoints.json` でエンドポイントを定義：

```json
{
  "/": {
    "responseFile": "responses/sample1.html",
    "headers": {
      "Content-Type": "text/html; charset=utf-8",
      "X-Server": "Mock-Server"
    },
    "statusCode": 200,
    "delay": 0
  }
}
```

## 実装時の注意

- デフォルトポート: 3000（環境変数PORTで変更可能）
- 存在しないパスは404エラーを返す
- レスポンス遅延設定で実際のサーバーの挙動を模倣
- 設定ファイルの検証機能を含める
- CORS対応が必要に応じて設定可能
- レスポンスファイルは実際のWebサイト構造を模倣すること