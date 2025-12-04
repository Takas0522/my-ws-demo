# E2Eテスト

このディレクトリには、Playwright + Cucumberを使用したE2Eテストが含まれています。

## 概要

- **テストフレームワーク**: Cucumber (BDD)
- **ブラウザ自動化**: Playwright
- **テスト用DB**: TestContainers (PostgreSQL)

## 環境による自動切り替え

このE2Eテストは、環境変数を使用してDB接続先を自動的に切り替えます：

- **開発環境**: `.env` ファイルを使用してDevContainerのDBに接続
- **E2Eテスト環境**: `.env.e2e` ファイル（自動生成）を使用してTestContainersのDBに接続

### 開発環境（通常のデバッグ）

各サービスは `.env` ファイルからDB接続情報を読み込み、DevContainerで提供されるPostgreSQLに接続します。

```bash
# 開発用DBの接続情報（.env）
DB_USER_SERVICE_HOST=localhost
DB_USER_SERVICE_PORT=5432
DB_USER_SERVICE_NAME=user_service_db
DB_USER_SERVICE_USER=postgres
DB_USER_SERVICE_PASSWORD=postgres
```

### E2Eテスト環境

E2Eテスト実行時は、TestContainersが自動的に `.env.e2e` ファイルを生成し、テスト専用のDBに接続します。

## TestContainersによるテスト用DB

E2Eテスト実行時に、開発用DBを汚さないようにTestContainersを使用してテスト専用のPostgreSQLコンテナを自動的に起動します。

### 特徴

1. **自動DB起動**: テスト開始時に3つのサービス用のPostgreSQLコンテナが自動的に起動
   - `user-service` DB
   - `auth-service` DB
   - `point-service` DB

2. **自動データ投入**: 各サービスのスキーマとシードデータが自動的に投入される

3. **環境変数ファイル自動生成**: TestContainersの動的ポート情報を含む `.env.e2e` を自動生成

4. **バックエンドサービス自動再起動**: E2E用の環境変数でバックエンドサービスを自動的に再起動

5. **各テストでDBリセット**: 各シナリオの実行前にDBがクリーンな状態にリセットされる

6. **自動クリーンアップ**: テスト終了後にサービスとコンテナが自動的に停止・削除される

### アーキテクチャ

```
src/e2e/
├── setup/
│   └── database.ts          # TestContainers管理クラス
├── step-definitions/
│   ├── hooks.ts             # Cucumberフック（DB起動/停止）
│   └── login.steps.ts       # ログイン機能のステップ定義
├── features/
│   └── login.feature        # ログイン機能のシナリオ
└── pages/
    └── LoginPage.ts         # ページオブジェクト
```

### 実行フロー

```
1. BeforeAll (テストスイート開始時)
   ├── 既存のバックエンドサービスを停止
   ├── TestContainers起動
   │   ├── PostgreSQLコンテナ起動 (user-service)
   │   ├── PostgreSQLコンテナ起動 (auth-service)
   │   ├── PostgreSQLコンテナ起動 (point-service)
   │   ├── スキーマ＆シードデータ投入
   │   └── .env.e2eファイル自動生成
   └── バックエンドサービスをE2E環境で起動
       ├── .env.e2eから環境変数を読み込み
       ├── user-service起動 (ポート8080)
       ├── auth-service起動 (ポート8081)
       ├── point-service起動 (ポート8082)
       └── bff起動 (ポート8090)

2. Before (各シナリオ前)
   ├── ブラウザ起動
   └── DBリセット（データクリア＆再投入）

3. シナリオ実行

4. After (各シナリオ後)
   └── ブラウザ停止

5. AfterAll (テストスイート終了時)
   ├── バックエンドサービス停止
   └── TestContainers停止
```

## セットアップ

### 前提条件

- Node.js 18以上
- Docker（TestContainersがDockerを使用）
- Maven（バックエンドサービスのビルドに必要）
- 十分なメモリ（最低8GB推奨、TestContainersと複数のPayara Microを起動するため）

### パッケージインストール

```bash
cd src/e2e
npm install
```

### バックエンドサービスのビルド

E2Eテスト実行前に、各サービスをビルドしておく必要があります：

```bash
# すべてのサービスをビルド
cd src/user-service && mvn clean package -DskipTests
cd ../auth-service && mvn clean package -DskipTests
cd ../point-service && mvn clean package -DskipTests
cd ../bff && mvn clean package -DskipTests
```

### インストールされる主要パッケージ

- `@playwright/test` - ブラウザ自動化
- `@cucumber/cucumber` - BDDテストフレームワーク
- `testcontainers` - TestContainers本体
- `@testcontainers/postgresql` - PostgreSQL用TestContainers
- `pg` - PostgreSQLクライアント

## テスト実行

このプロジェクトでは2種類のテスト実行方法をサポートしています：

### 1. Cucumberテスト実行（BDD形式）

Cucumberを使用したBDD形式のテストを実行します。

```bash
npm run test:cucumber
```

**特徴**:
- Gherkin記法でテストシナリオを記述
- 日本語でのシナリオ記述が可能
- プログレスバーとHTML形式のレポート生成

### 2. Playwrightテスト実行（標準形式）

Playwrightの標準的なテスト形式で実行します。

```bash
# 通常実行（ヘッドレスモード）
npm test

# ブラウザを表示して実行
npm run test:headed

# デバッグモード（ステップ実行）
npm run test:debug

# UIモード（インタラクティブ）
npm run test:ui

# テストレポートを表示
npm run test:report
```

**特徴**:
- TypeScriptで直接テストを記述
- Playwrightの全機能を活用可能
- スクリーンショット・動画の自動記録
- 詳細なHTMLレポート

**⚠️ 共通の注意点**: 
初回実行時は以下の処理が行われるため、5〜7分程度かかります：
1. TestContainersの起動（PostgreSQL × 3）
2. バックエンドサービスの起動（Payara Micro × 4）
3. フロントエンドの起動（Vite）

**実行時間の目安**:
- セットアップ: 約2〜3分
- テスト実行: 約1〜2分
- クリーンアップ: 約30秒

## ディレクトリ構成

```
src/e2e/
├── features/                 # Cucumberフィーチャーファイル
│   └── login.feature        # ログイン機能のシナリオ
├── step-definitions/        # ステップ定義
│   ├── hooks.ts             # フック（DB・サービス起動など）
│   └── login.steps.ts       # ログイン関連のステップ
├── pages/                   # ページオブジェクト
│   └── LoginPage.ts         # ログインページ
├── setup/                   # セットアップスクリプト
│   └── database.ts          # TestContainersによるDB管理
├── scripts/                 # サービス管理スクリプト
│   └── manage-services.sh   # バックエンドサービス起動・停止
├── tests/                   # Playwrightテスト
│   └── login.spec.ts        # ログインテスト
├── cucumber.js              # Cucumber設定
├── playwright.config.ts     # Playwright設定
└── package.json

src/user-service/
├── .env                     # 開発環境用DB接続情報
└── .env.e2e                 # E2Eテスト用DB接続情報（自動生成）

src/auth-service/
├── .env                     # 開発環境用DB接続情報
└── .env.e2e                 # E2Eテスト用DB接続情報（自動生成）

src/point-service/
├── .env                     # 開発環境用DB接続情報
└── .env.e2e                 # E2Eテスト用DB接続情報（自動生成）
```

## サービスの手動管理

E2Eテスト以外で手動でサービスを管理する場合：

### 開発環境でサービスを起動

```bash
# .envを使用して起動
./src/e2e/scripts/manage-services.sh start

# または既存のタスクを使用
# VSCodeのタスクから start-*-debug を実行
```

### E2E環境でサービスを起動

```bash
# .env.e2eを使用して起動（TestContainers起動後）
./src/e2e/scripts/manage-services.sh start-e2e
```

### サービスを停止

```bash
./src/e2e/scripts/manage-services.sh stop
```

### サービスを再起動

```bash
# 開発環境
./src/e2e/scripts/manage-services.sh restart

# E2E環境
./src/e2e/scripts/manage-services.sh restart .env.e2e
```

## トラブルシューティング

### Dockerが起動していない

TestContainersはDockerを使用するため、Dockerが起動している必要があります。

```bash
# Dockerの状態確認
docker ps

# Dockerが停止している場合は起動
sudo systemctl start docker  # Linux
# または Docker Desktop を起動（Mac/Windows）
```

### ポートが既に使用されている

TestContainersは動的にポートを割り当てるため、通常はポート競合は発生しません。
もし問題が発生した場合は、他のコンテナを停止してください。

```bash
# 実行中のコンテナを確認
docker ps

# 不要なコンテナを停止
docker stop <container_id>
```

### テストが遅い / タイムアウトが発生する

**原因**:
1. TestContainersの起動には時間がかかります（初回は特に）
2. Payara Microの起動には1サービスあたり30〜60秒かかります
3. 4つのサービス（user, auth, point, bff）を順次起動するため、合計で2〜3分必要です

**対策**:
- 初回実行時は正常な動作です
- 2回目以降はDockerイメージがキャッシュされるため高速化されます
- メモリが不足している場合は、Dockerのメモリ設定を増やしてください

**BFFの起動が遅い場合**:
BFFは他のサービスに依存しているため、最後に起動します。テストでログイン後のリダイレクトが失敗する場合は、BFFの起動を待つ必要があります：

```bash
# BFFの起動状態を確認
curl http://localhost:8090/api/health
```

### データベース接続エラー

ログに表示される接続情報を確認してください：

```
Database connection info:
  user-service: postgresql://testuser:testpass@localhost:xxxxx/user_service_db
  auth-service: postgresql://testuser:testpass@localhost:xxxxx/auth_service_db
  point-service: postgresql://testuser:testpass@localhost:xxxxx/point_service_db
```

また、各サービスの `.env.e2e` ファイルが正しく生成されているか確認してください：

```bash
cat src/user-service/.env.e2e
cat src/auth-service/.env.e2e
cat src/point-service/.env.e2e
```

### サービス起動エラー

サービスが起動しない場合は、ログを確認してください：

```bash
# 各サービスのログ
tail -f src/user-service/e2e-test.log
tail -f src/auth-service/e2e-test.log
tail -f src/point-service/e2e-test.log
tail -f src/bff/e2e-test.log
```

### 環境変数が読み込まれない

Payaraが環境変数を正しく読み込むには、`${ENV=変数名}` の形式が必要です。
`glassfish-resources.xml` の設定を確認してください。

### 一部のテストが失敗する

**症状**: ログイン後のリダイレクトで失敗（`page.waitForURL: Timeout exceeded`）

**原因**: BFFの起動が完了していない

**対策**:
1. `hooks.ts`の待機時間を延長（デフォルト60秒）
2. サービスが完全に起動してからテストを実行
3. 手動でサービスを事前起動してからテストを実行

```bash
# サービスを事前に起動
./src/e2e/scripts/manage-services.sh start

# サービスの起動を確認
curl http://localhost:8080  # user-service
curl http://localhost:8081  # auth-service
curl http://localhost:8082  # point-service
curl http://localhost:8090  # bff
curl http://localhost:3000  # frontend

# E2Eテスト実行
cd src/e2e && npm run test:cucumber
```

## 既知の制限事項

1. **起動時間**: 初回実行時は5〜7分かかります
2. **メモリ使用量**: TestContainers + Payara Micro × 4 で約4〜6GB必要
3. **並列実行**: 現在の実装では並列実行に対応していません（順次実行のみ）
4. **ポート競合**: 既にサービスが起動している場合はポート競合が発生します

## 参考資料

- [Playwright公式ドキュメント](https://playwright.dev/)
- [Cucumber公式ドキュメント](https://cucumber.io/docs/cucumber/)
- [TestContainers公式ドキュメント](https://testcontainers.com/)
- [TestContainers Node.js](https://node.testcontainers.org/)
