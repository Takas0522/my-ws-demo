# アーキテクチャドキュメント

## システム概要

このアプリケーションはマイクロサービスアーキテクチャを採用した会員管理システムです。各サービスは独立して開発・デプロイ可能で、明確な責任範囲を持ちます。

## アーキテクチャ図

```
┌─────────────────────────────────────────────────────────┐
│                     Client Browser                       │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/HTTPS
                       ↓
┌─────────────────────────────────────────────────────────┐
│                  Frontend (Vue 3)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │ Login View  │  │Account View │  │   Router    │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
│                    Axios HTTP Client                     │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API
                       ↓
┌─────────────────────────────────────────────────────────┐
│            Backend For Frontend (BFF)                    │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │  BFF Resource    │  │   CORS Filter    │            │
│  └────────┬─────────┘  └──────────────────┘            │
│           │                                              │
│  ┌────────┴─────────┐  ┌──────────────────┐            │
│  │ User Service     │  │  Auth Service    │            │
│  │   Client         │  │    Client        │            │
│  └──────────────────┘  └──────────────────┘            │
└─────────┬──────────────────────┬──────────────────────┘
          │                      │
          │ HTTP                 │ HTTP
          ↓                      ↓
┌────────────────────┐  ┌────────────────────┐
│   User Service     │  │   Auth Service     │
│   (Port: 8080)     │  │   (Port: 8081)     │
│                    │  │                    │
│ ┌────────────────┐ │  │ ┌────────────────┐ │
│ │ User Resource  │ │  │ │ Auth Resource  │ │
│ └───────┬────────┘ │  │ └───────┬────────┘ │
│         │          │  │         │          │
│ ┌───────┴────────┐ │  │ ┌───────┴────────┐ │
│ │  Repository    │ │  │ │  Repository    │ │
│ └───────┬────────┘ │  │ └───────┬────────┘ │
│         │          │  │         │          │
│ ┌───────┴────────┐ │  │ ┌───────┴────────┐ │
│ │ DataSource     │ │  │ │ DataSource     │ │
│ └───────┬────────┘ │  │ └───────┬────────┘ │
└─────────┼──────────┘  └─────────┼──────────┘
          │                       │
          │ JDBC                  │ JDBC
          ↓                       ↓
┌─────────────────────────────────────────────┐
│         PostgreSQL (Port: 5432)             │
│  ┌──────────────┐  ┌──────────────┐        │
│  │user_service_ │  │auth_service_ │        │
│  │     db       │  │     db       │        │
│  └──────────────┘  └──────────────┘        │
└─────────────────────────────────────────────┘
```

## レイヤーアーキテクチャ

### フロントエンド層

**技術スタック**: Vue 3, Vue Router, Axios, Tailwind CSS

**責務**:
- ユーザーインターフェースの提供
- ユーザー入力の処理
- BFFへのHTTPリクエスト送信
- 認証トークンの管理（LocalStorage）

**主要コンポーネント**:
- `LoginView`: ログイン画面
- `AccountView`: 会員情報表示画面
- `PointView`: ポイント残高表示画面
- `PointHistoryView`: ポイント履歴表示画面
- `router`: ルーティング＆認証ガード
- `api.js`: API通信サービス
- `pointApi.js`: ポイント管理API通信サービス

### BFF層

**技術スタック**: Java 11, Jakarta EE 8, Payara Micro

**責務**:
- APIゲートウェイとしての機能
- 認証トークンの検証
- マイクロサービスへのリクエスト集約
- CORS対応
- フロントエンド専用のAPIエンドポイント提供

**主要クラス**:
- `BffResource`: RESTエンドポイント
- `UserServiceClient`: ユーザーサービスクライアント
- `AuthServiceClient`: 認証サービスクライアント
- `CorsFilter`: CORS設定

### マイクロサービス層

#### User Service

**責務**:
- ユーザー情報の管理（CRUD）
- ポイント情報の管理
- ユーザーアカウント情報の提供

**データモデル**:
- `User`: ユーザーエンティティ
- `UserPoint`: ポイントエンティティ
- `PointHistory`: ポイント履歴

**主要クラス**:
- `UserResource`: REST API
- `UserRepository`: データアクセス
- `UserPointRepository`: ポイントデータアクセス

#### Auth Service

**責務**:
- ユーザー認証
- セッショントークンの発行・管理
- トークンの検証
- ログイン履歴の記録

**データモデル**:
- `SessionToken`: セッショントークン
- `UserCredentials`: 認証情報
- `LoginHistory`: ログイン履歴

**主要クラス**:
- `AuthResource`: REST API
- `AuthRepository`: データアクセス
- `AuthService`: 認証ロジック（BCrypt）

### データ層

**技術スタック**: PostgreSQL

**データベース設計原則**:
- サービスごとに独立したデータベース
- サービス間のデータ共有は API 経由のみ
- トランザクション境界はサービス内に限定

## 通信フロー

### ログインフロー

```
1. User → Frontend: ユーザーIDとパスワードを入力
2. Frontend → BFF: POST /api/login
3. BFF → Auth Service: POST /api/auth/login
4. Auth Service → DB: パスワードハッシュ検証
5. Auth Service: セッショントークン生成
6. Auth Service → DB: トークン保存
7. Auth Service → BFF: トークン返却
8. BFF → Frontend: トークン返却
9. Frontend: トークンをLocalStorageに保存
10. Frontend: /account画面へ遷移
```

### アカウント情報取得フロー

```
1. Frontend → BFF: GET /api/account (Bearer Token)
2. BFF → Auth Service: POST /api/auth/verify (Bearer Token)
3. Auth Service → DB: トークン検証
4. Auth Service → BFF: 検証結果（userId含む）
5. BFF → User Service: GET /api/users/{userId}/account
6. User Service → DB: ユーザー情報とポイント取得
7. User Service → BFF: アカウントデータ返却
8. BFF → Frontend: アカウントデータ返却
9. Frontend: 画面に表示
```

## セキュリティ設計

### 認証方式

**Bearer Token Authentication**
- トークンベースの認証
- 7日間の有効期限
- Base64エンコーディング（URL-safe）

### パスワード管理

- BCrypt アルゴリズム（ラウンド数: 10）
- ソルト付きハッシュ化
- データベースに平文パスワード非保存

### CORS設定

- BFF層でCORS対応
- 全オリジン許可（開発環境）
- 本番環境では特定オリジンに制限すべき

### セッション管理

- トークンはデータベースに保存
- 期限切れトークンの定期クリーンアップ
- ログアウト時にトークン削除

## データベース設計

### user_service_db

#### users テーブル
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_points テーブル
```sql
CREATE TABLE user_points (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    points INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### point_history テーブル
```sql
CREATE TABLE point_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    points_changed INTEGER NOT NULL,
    description VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### auth_service_db

#### user_credentials テーブル
```sql
CREATE TABLE user_credentials (
    id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### session_tokens テーブル
```sql
CREATE TABLE session_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_credentials(user_id) ON DELETE CASCADE
);
```

#### login_history テーブル
```sql
CREATE TABLE login_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL DEFAULT TRUE
);
```

## スケーラビリティ

### 水平スケーリング

各マイクロサービスは独立してスケール可能:
- User Service: ユーザー数に応じて増減
- Auth Service: ログイン頻度に応じて増減
- BFF: フロントエンドのトラフィックに応じて増減

### データベーススケーリング

- 読み取りレプリカの追加
- コネクションプーリング
- データベースシャーディング（将来的）

## 監視とログ

### 推奨される監視項目

- API レスポンスタイム
- エラーレート
- データベース接続数
- セッショントークン数
- ログイン成功/失敗率

### ログ戦略

- 構造化ログ（JSON形式）
- 集中ログ管理（ELK Stack など）
- トレーシングID による リクエスト追跡

## 今後の拡張

### 機能拡張案

1. **ポイント管理サービスの分離**
   - User Service からポイント機能を独立
   
2. **通知サービスの追加**
   - ポイント獲得通知
   - ログイン通知

3. **管理画面の追加**
   - ユーザー管理
   - ポイント管理
   - 統計ダッシュボード

4. **API Gateway の導入**
   - Kong, AWS API Gateway など
   - レート制限
   - API キー管理

5. **イベント駆動アーキテクチャ**
   - メッセージキュー（RabbitMQ, Kafka）
   - 非同期処理

### 技術的改善案

1. **コンテナオーケストレーション**
   - Kubernetes
   - Docker Swarm

2. **サービスメッシュ**
   - Istio
   - Linkerd

3. **CI/CD パイプライン**
   - Jenkins
   - GitHub Actions
   - GitLab CI

4. **キャッシング**
   - Redis
   - Memcached

## まとめ

このマイクロサービスアーキテクチャは以下の特徴を持ちます:

✅ **疎結合**: 各サービスが独立して開発・デプロイ可能
✅ **拡張性**: サービスごとに独立してスケール可能
✅ **保守性**: 明確な責任範囲による保守の容易さ
✅ **技術的多様性**: サービスごとに最適な技術選択が可能
✅ **障害の分離**: 一部のサービス障害が全体に波及しない
