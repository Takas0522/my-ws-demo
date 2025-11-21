# Point Service

ユーザーのポイント残高管理と履歴記録を行うマイクロサービス

## 概要

Point Serviceは、ユーザーのポイント残高管理、ポイント獲得/使用履歴の記録、履歴照会機能を提供します。

## 技術スタック

- **Java 11**
- **Jakarta EE 8** (JAX-RS, CDI, JDBC)
- **Payara Server**
- **PostgreSQL** (データベース)
- **java-jwt** (JWT認証)
- **Maven** (ビルドツール)

## API仕様

### 1. ポイント残高取得

**Endpoint:** `GET /api/points`

**Request Headers:**
```
Authorization: Bearer <JWT>
```

**Response (200 OK):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "balance": 1500,
  "lastUpdated": "2025-11-21T10:30:00"
}
```

**Error Responses:**
- `401 Unauthorized` - JWT認証失敗
- `500 Internal Server Error` - サーバーエラー

### 2. ポイント履歴取得

**Endpoint:** `GET /api/points/history?page=1&limit=10`

**Request Headers:**
```
Authorization: Bearer <JWT>
```

**Query Parameters:**
- `page` (optional, default: 1) - ページ番号
- `limit` (optional, default: 10, max: 100) - 1ページあたりの件数

**Response (200 OK):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "history": [
    {
      "id": 1,
      "amount": 1000,
      "transactionType": "EARN",
      "description": "新規登録ボーナス",
      "createdAt": "2025-10-22T10:00:00",
      "expiresAt": "2026-04-20T10:00:00"
    },
    {
      "id": 2,
      "amount": 500,
      "transactionType": "EARN",
      "description": "購入特典",
      "createdAt": "2025-11-11T15:30:00",
      "expiresAt": "2026-05-10T15:30:00"
    }
  ],
  "page": 1,
  "limit": 10,
  "total": 25
}
```

**Error Responses:**
- `401 Unauthorized` - JWT認証失敗
- `500 Internal Server Error` - サーバーエラー

## データベース構造

### points テーブル
| カラム名 | 型 | 説明 |
|---------|-----|------|
| user_id | UUID | ユーザーID (主キー) |
| balance | INTEGER | ポイント残高 |
| last_updated | TIMESTAMP | 最終更新日時 |

### point_history テーブル
| カラム名 | 型 | 説明 |
|---------|-----|------|
| id | SERIAL | 履歴ID (主キー) |
| user_id | UUID | ユーザーID (外部キー) |
| amount | INTEGER | ポイント数 |
| transaction_type | VARCHAR(20) | 取引タイプ ('EARN' or 'USE') |
| description | TEXT | 説明 |
| created_at | TIMESTAMP | 作成日時 |
| expires_at | TIMESTAMP | 有効期限 |

## セットアップ

### 前提条件

- Java 11以上
- Maven 3.6以上
- PostgreSQL 12以上
- Payara Server 5以上

### データベース設定

1. PostgreSQLデータベースを作成:
```bash
psql -U postgres -c "CREATE DATABASE point_service_db;"
```

2. スキーマとシードデータを適用:
```bash
psql -U postgres -d point_service_db -f database/schema.sql
psql -U postgres -d point_service_db -f database/seed.sql
```

### ビルド

```bash
cd src/point-service
mvn clean package
```

ビルド成果物は `target/point-service.war` に生成されます。

### デプロイ

1. Payara Serverを起動
2. WARファイルをデプロイ:
```bash
asadmin deploy --port 4848 --contextroot /point-service target/point-service.war
```

3. サービスにアクセス:
```
http://localhost:8084/point-service/api/points
```

## JWT認証

Point ServiceはAuth Serviceが発行したJWTトークンを検証します。

**必要なクレーム:**
- `userId` - ユーザーID (UUID文字列)
- `username` - ユーザー名
- `iat` - 発行日時
- `exp` - 有効期限

**トークンの使用方法:**

```bash
# Auth Serviceでトークンを取得
curl -X POST http://localhost:8081/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"tanaka_taro","password":"password123"}'

# 取得したトークンを使ってポイント残高を取得
curl -X GET http://localhost:8084/point-service/api/points \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## 開発環境 (DevContainer)

DevContainerを使用する場合、データベースは自動的にセットアップされます。

`.devcontainer/setup-auth-db.sh` スクリプトが自動的に実行され、以下の処理が行われます:
1. `point_service_db` データベースの作成
2. スキーマの適用
3. シードデータの投入

## ポイント失効ロジック

ポイントには有効期限があり、`expires_at` カラムで管理されます。
- 獲得ポイントは通常6ヶ月間有効
- 使用ポイントには有効期限なし (NULL)

現在の実装では、履歴取得時に有効期限切れのポイントも表示されますが、
将来のバージョンでバッチ処理による自動失効を実装予定です。

## ポート番号

- **8084** (推奨ポート)
- 他のサービス: 8081 (Auth), 8082 (BFF), 8083 (User)

## 依存サービス

- **Auth Service** - JWT発行と検証のため
- **PostgreSQL** - データ永続化のため

## トラブルシューティング

### データベース接続エラー

GlassFish/Payaraのコンソールで以下を確認:
1. JDBCコネクションプールが正常に作成されているか
2. PostgreSQLが起動しているか
3. データベース認証情報が正しいか

### JWT認証エラー

- Auth Serviceと同じシークレットキーを使用しているか確認
- トークンの有効期限が切れていないか確認
- `Authorization` ヘッダーの形式が `Bearer <token>` であることを確認

## ライセンス

このプロジェクトは学習目的のデモプロジェクトです。
