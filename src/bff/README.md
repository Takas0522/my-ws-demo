# BFF (Backend for Frontend)

フロントエンド専用のバックエンドサービス

## 概要

BFFは、フロントエンドアプリケーションとバックエンドマイクロサービス群の間に位置し、複数のマイクロサービスへのリクエストを集約・変換します。

## API エンドポイント

ベースURL: `http://localhost:8090/api`

### ユーザー関連

#### ログインとアカウント情報取得
```
POST /bff/login
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
  "user": {
    "id": 1,
    "username": "tanaka_taro",
    "email": "tanaka.taro@example.com",
    "fullName": "田中太郎"
  },
  "points": {
    "id": 1,
    "userId": 1,
    "points": 1500
  }
}
```

#### ユーザーダッシュボード情報取得
```
GET /bff/users/{userId}/dashboard
Authorization: Bearer {token}
```

レスポンス例:
```json
{
  "user": {
    "id": 1,
    "username": "tanaka_taro",
    "email": "tanaka.taro@example.com",
    "fullName": "田中太郎"
  },
  "points": {
    "current": 1500,
    "lastUpdated": "2024-01-01T00:00:00"
  },
  "recentHistory": [
    {
      "id": 5,
      "pointsChanged": 100,
      "description": "購入特典",
      "transactionDate": "2024-01-05T00:00:00"
    }
  ]
}
```

## 連携先サービス

BFFは以下のマイクロサービスと連携します：

- **auth-service** (`http://localhost:8081`): 認証・認可
- **user-service** (`http://localhost:8080`): ユーザー情報・ポイント残高
- **point-service** (`http://localhost:8082`): ポイント履歴

## ビルドと起動

### ビルド
```bash
cd /workspaces/my-ws-demo/src/bff
mvn clean package
```

### 起動（Java 21対応）
```bash
cd /workspaces/my-ws-demo/src/bff
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

### デバッグモードで起動（デバッグポート: 5008）
```bash
cd /workspaces/my-ws-demo/src/bff
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 \
     -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

## 環境変数

サービス間通信のエンドポイントは `.env` ファイルで設定されています:

```properties
AUTH_SERVICE_URL=http://localhost:8081
USER_SERVICE_URL=http://localhost:8080
POINT_SERVICE_URL=http://localhost:8082
```

## テスト

### ユニットテスト
```bash
mvn test
```

## アーキテクチャ

BFFパターンにより、フロントエンドは以下の利点を享受できます：

- **単一エンドポイント**: 複数のマイクロサービスを呼び出す必要がない
- **データ集約**: 必要な情報を1回のリクエストで取得
- **フロントエンド最適化**: フロントエンド向けにカスタマイズされたレスポンス
- **セキュリティ**: マイクロサービスへの直接アクセスを隠蔽
