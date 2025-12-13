# Point Service

ポイント履歴を管理するマイクロサービス

## 概要

Point Serviceは、ユーザーのポイント変動履歴を管理します。

## API エンドポイント

ベースURL: `http://localhost:8082/api`

### ポイント履歴管理

#### 全ポイント履歴取得
```
GET /point-histories
```

レスポンス例:
```json
[
  {
    "id": 1,
    "userId": 1,
    "pointsChanged": 100,
    "description": "会員登録ボーナス",
    "transactionDate": "2024-01-01T00:00:00"
  }
]
```

#### ユーザー別ポイント履歴取得
```
GET /point-histories/user/{userId}
```

#### ポイント履歴作成
```
POST /point-histories
Content-Type: application/json

{
  "userId": 1,
  "pointsChanged": 100,
  "description": "購入特典"
}
```

#### ポイント履歴更新
```
PUT /point-histories/{id}
Content-Type: application/json

{
  "userId": 1,
  "pointsChanged": 150,
  "description": "購入特典（修正）"
}
```

#### ポイント履歴削除
```
DELETE /point-histories/{id}
```

## データベーススキーマ

### point_histories テーブル
| カラム | 型 | 説明 |
|-------|-----|------|
| id | SERIAL | 主キー |
| user_id | INTEGER | ユーザーID |
| points_changed | INTEGER | ポイント変動（±） |
| description | VARCHAR(255) | 変動理由 |
| transaction_date | TIMESTAMP | 取引日時 |

## ビルドと起動

### ビルド
```bash
cd /workspaces/my-ws-demo/src/point-service
mvn clean package
```

### 起動（Java 21対応）
```bash
cd /workspaces/my-ws-demo/src/point-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -jar /opt/payara-micro.jar --deploy target/point-service.war --port 8082
```

### デバッグモードで起動（デバッグポート: 5007）
```bash
cd /workspaces/my-ws-demo/src/point-service
java --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.management/sun.management=ALL-UNNAMED \
     --add-opens java.base/sun.net.www.protocol.jrt=ALL-UNNAMED \
     -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007 \
     -jar /opt/payara-micro.jar --deploy target/point-service.war --port 8082
```

## 環境変数

データベース接続は `.env` ファイルまたは `glassfish-resources.xml` で設定されています:

- Database: `point_service_db`
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
