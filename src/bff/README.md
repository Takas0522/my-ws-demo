# Backend For Frontend (BFF)

フロントエンドとマイクロサービス間の通信を仲介するBFFサービス

## 概要

BFF（Backend For Frontend）は、フロントエンドアプリケーション専用のAPIゲートウェイとして機能します。各マイクロサービスへのリクエストを集約し、認証チェックを行います。

## API エンドポイント

ベースURL: `http://localhost:8090/api`

### 認証エンドポイント

#### ログイン
```
POST /login
Content-Type: application/json

{
  "userId": 1,
  "password": "password123"
}
```

#### ログアウト
```
POST /logout
Authorization: Bearer {token}
```

#### トークン検証
```
GET /verify
Authorization: Bearer {token}
```

### ユーザー情報エンドポイント（認証必須）

#### アカウント情報取得
```
GET /account
Authorization: Bearer {token}
```

自分のアカウント情報（ユーザー情報 + ポイント）を取得します。

#### ユーザー一覧取得
```
GET /users
Authorization: Bearer {token}
```

#### ユーザー情報取得
```
GET /users/{id}
Authorization: Bearer {token}
```

#### ユーザーポイント取得
```
GET /users/{id}/points
Authorization: Bearer {token}
```

## アーキテクチャ

```
Frontend (Vue 3)
    ↓
   BFF (Port 8090)
    ↓
    ├── User Service (Port 8080)
    ├── Auth Service (Port 8081)
    └── Point Service (Port 8082)
```

### BFFの役割

1. **APIゲートウェイ**: 単一のエントリーポイントを提供
2. **認証チェック**: すべてのリクエストでトークン検証
3. **リクエスト集約**: 複数のマイクロサービスへのリクエストを統合
4. **CORS対応**: フロントエンドからのクロスオリジンリクエストを許可

## CORS設定

BFFは以下のCORS設定を持ちます:

- `Access-Control-Allow-Origin`: `*`
- `Access-Control-Allow-Methods`: `GET, POST, PUT, DELETE, OPTIONS, HEAD`
- `Access-Control-Allow-Headers`: `origin, content-type, accept, authorization`

## サービス間通信

### 環境変数

各マイクロサービスへの接続は環境変数で設定できます:

```bash
USER_SERVICE_URL=http://localhost:8080
AUTH_SERVICE_URL=http://localhost:8081
```

デフォルト値:
- User Service: `http://localhost:8080`
- Auth Service: `http://localhost:8081`

### HTTP クライアント

JAX-RS Client API（Jersey）を使用してマイクロサービスと通信します。

## セキュリティ

### 認証フロー

1. フロントエンドからログインリクエスト
2. BFFが認証サービスに転送
3. 認証サービスがトークンを発行
4. フロントエンドがトークンを保存
5. 以降のリクエストで `Authorization: Bearer {token}` ヘッダーを使用

### 認証が必要なエンドポイント

以下のエンドポイントは認証が必須です:
- `/account`
- `/users`
- `/users/{id}`
- `/users/{id}/points`

認証されていない場合、`401 Unauthorized` が返されます。

## ビルドと起動

### VS Codeデバッグ実行（推奨）

VS Codeのデバッグパネルから「Debug bff」を選択してF5キーを押すと、自動的にビルド・起動・デバッガーアタッチが行われます。

**デバッグポート**: 5008

全サービスを一括起動する場合は「Debug All Services」を選択してください。

### 手動ビルド
```bash
cd /workspaces/my-ws-demo/src/bff
mvn clean package
```

### 手動起動
```bash
cd /workspaces/my-ws-demo/src/bff
java -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

または環境変数を指定して起動:
```bash
USER_SERVICE_URL=http://localhost:8080 \
AUTH_SERVICE_URL=http://localhost:8081 \
POINT_SERVICE_URL=http://localhost:8082 \
java -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

デバッグモードで起動:
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 \
  -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

## テスト

```bash
mvn test
```

## エラーハンドリング

### エラーレスポンス形式

```json
{
  "error": "エラーメッセージ"
}
```

### ステータスコード

- `200 OK`: 成功
- `401 Unauthorized`: 認証エラー
- `404 Not Found`: リソースが見つからない
- `500 Internal Server Error`: サーバーエラー

## 拡張性

新しいマイクロサービスを追加する場合:

1. `client` パッケージに新しいクライアントクラスを追加
2. `BffResource` に新しいエンドポイントを追加
3. 必要に応じて認証チェックを実装

例:
```java
@Inject
private NewServiceClient newServiceClient;

@GET
@Path("/new-endpoint")
public Response newEndpoint(@HeaderParam("Authorization") String authHeader) {
    if (!isAuthenticated(authHeader)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    // マイクロサービスへのリクエスト
    return newServiceClient.getData();
}
```
