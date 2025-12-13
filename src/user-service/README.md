# User Service

ユーザー情報とポイントを管理するマイクロサービス

## 概要

ユーザーサービスは、ユーザーの基本情報とポイント残高を管理します。

## API エンドポイント

ベースURL: `http://localhost:8080/api`

### ユーザー管理

#### 全ユーザー取得
```
GET /users
```

レスポンス例:
```json
[
  {
    "id": 1,
    "username": "tanaka_taro",
    "email": "tanaka.taro@example.com",
    "fullName": "田中太郎",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

#### ユーザー情報取得
```
GET /users/{id}
```

#### ユーザー作成
```
POST /users
Content-Type: application/json

{
  "username": "test_user",
  "email": "test@example.com",
  "fullName": "テストユーザー"
}
```

#### ユーザー更新
```
PUT /users/{id}
Content-Type: application/json

{
  "username": "test_user",
  "email": "test@example.com",
  "fullName": "テストユーザー"
}
```

#### ユーザー削除
```
DELETE /users/{id}
```

### ポイント管理

#### ユーザーポイント取得
```
GET /users/{id}/points
```

レスポンス例:
```json
{
  "id": 1,
  "userId": 1,
  "points": 1500,
  "lastUpdated": "2024-01-01T00:00:00"
}
```

#### アカウント情報取得（ユーザー + ポイント）
```
GET /users/{id}/account
```

レスポンス例:
```json
{
  "user": {
    "id": 1,
    "username": "tanaka_taro",
    "email": "tanaka.taro@example.com",
    "fullName": "田中太郎",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "points": {
    "id": 1,
    "userId": 1,
    "points": 1500,
    "lastUpdated": "2024-01-01T00:00:00"
  }
}
```

## データベーススキーマ

### users テーブル
| カラム | 型 | 説明 |
|-------|-----|------|
| id | SERIAL | 主キー |
| username | VARCHAR(50) | ユーザー名（一意） |
| email | VARCHAR(100) | メールアドレス（一意） |
| full_name | VARCHAR(100) | 氏名 |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### user_points テーブル
| カラム | 型 | 説明 |
|-------|-----|------|
| id | SERIAL | 主キー |
| user_id | INTEGER | ユーザーID（外部キー） |
| points | INTEGER | ポイント残高 |
| last_updated | TIMESTAMP | 最終更新日時 |

### point_history テーブル
| カラム | 型 | 説明 |
|-------|-----|------|
| id | SERIAL | 主キー |
| user_id | INTEGER | ユーザーID（外部キー） |
| points_changed | INTEGER | ポイント変動 |
| description | VARCHAR(255) | 説明 |
| transaction_date | TIMESTAMP | 取引日時 |

## ビルドと起動

### ビルド
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn clean package
```

### 起動（Java 21対応）
```bash
cd /workspaces/my-ws-demo/src/user-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -jar /opt/payara-micro.jar --deploy target/user-service.war --port 8080
```

### デバッグモードで起動（デバッグポート: 5005）
```bash
cd /workspaces/my-ws-demo/src/user-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar /opt/payara-micro.jar --deploy target/user-service.war --port 8080
```

## 環境変数

データベース接続は `glassfish-resources.xml` で設定されています:

- Database: `user_service_db`
- Host: `localhost`
- Port: `5432`
- User: `postgres`
- Password: `postgres`

## テスト

```bash
mvn test
```
