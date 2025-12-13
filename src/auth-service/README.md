# Auth Service

認証・認可を管理するマイクロサービス

## 概要

Auth Serviceは、ユーザー認証、JWTトークン発行、セッション管理を担当します。

## API エンドポイント

ベースURL: `http://localhost:8081/api`

### 認証

#### ログイン
```
POST /auth/login
Content-Type: application/json

{
  "username": "tanaka_taro",
  "password": "password123"
}
```

レスポンス例:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "tanaka_taro"
}
```

#### トークン検証
```
POST /auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### ログアウト
```
POST /auth/logout
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## データベーススキーマ

### session_tokens テーブル
| カラム | 型 | 説明 |
|-------|-----|------|
| id | SERIAL | 主キー |
| user_id | INTEGER | ユーザーID |
| token | VARCHAR(500) | JWTトークン |
| created_at | TIMESTAMP | 作成日時 |
| expires_at | TIMESTAMP | 有効期限 |

## ビルドと起動

### ビルド
```bash
cd /workspaces/my-ws-demo/src/auth-service
mvn clean package
```

### 起動（Java 21対応）
```bash
cd /workspaces/my-ws-demo/src/auth-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -jar /opt/payara-micro.jar --deploy target/auth-service.war --port 8081
```

### デバッグモードで起動（デバッグポート: 5006）
```bash
cd /workspaces/my-ws-demo/src/auth-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 \
     -jar /opt/payara-micro.jar --deploy target/auth-service.war --port 8081
```

## 環境変数

データベース接続は `.env` ファイルまたは `glassfish-resources.xml` で設定されています:

- Database: `auth_service_db`
- Host: `localhost`
- Port: `5432`
- User: `postgres`
- Password: `postgres`

## テスト

### ユニットテスト
```bash
mvn test
```

### 統合テスト
```bash
mvn verify -Pintegration-test
```

## JWT設定

JWT署名には環境変数 `JWT_SECRET_KEY` が使用されます。デフォルトは `your-secret-key-min-256-bits-long` です。
