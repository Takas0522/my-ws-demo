# クイックスタートガイド

## 前提条件の確認

現在のDevContainerで発生している問題：
- ❌ Maven 4.x がインストールされているが Java 11 では動作しない
- ❌ Payara Micro がインストールされていない

## 解決方法

### 1. DevContainer を再ビルド

VS Code のコマンドパレット（Ctrl+Shift+P / Cmd+Shift+P）を開き、以下を実行：

```
Dev Containers: Rebuild Container
```

これにより以下がインストールされます：
- ✅ Maven 3.9.9（Java 11 対応）
- ✅ Payara Micro 5.2022.5

### 2. 再ビルド後の確認

```bash
# Maven バージョン確認
mvn --version
# 期待: Apache Maven 3.9.9

# Java バージョン確認
java -version
# 期待: openjdk version "11.0.28"

# Payara Micro 確認
ls -lh /opt/payara-micro.jar
# 期待: ファイルが存在する
```

### 3. サービスのビルド

各サービスを順番にビルド：

```bash
# ユーザーサービス
cd /workspaces/my-ws-demo/src/user-service
mvn clean package

# 認証サービス
cd /workspaces/my-ws-demo/src/auth-service
mvn clean package

# BFF
cd /workspaces/my-ws-demo/src/bff
mvn clean package
```

### 4. サービスの起動

**4つの別々のターミナル**を開いて、それぞれで以下を実行：

#### ターミナル 1: ユーザーサービス
```bash
cd /workspaces/my-ws-demo/src/user-service
java -jar /opt/payara-micro.jar --deploy target/user-service.war --port 8080
```

起動完了のメッセージを待つ（約30秒）

#### ターミナル 2: 認証サービス
```bash
cd /workspaces/my-ws-demo/src/auth-service
java -jar /opt/payara-micro.jar --deploy target/auth-service.war --port 8081
```

起動完了のメッセージを待つ（約30秒）

#### ターミナル 3: ポイントサービス
```bash
cd /workspaces/my-ws-demo/src/point-service
java -jar /opt/payara-micro.jar --deploy target/point-service.war --port 8082
```

起動完了のメッセージを待つ（約30秒）

#### ターミナル 4: BFF
```bash
cd /workspaces/my-ws-demo/src/bff
java -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
```

起動完了のメッセージを待つ（約30秒）

#### ターミナル 5: フロントエンド
```bash
cd /workspaces/my-ws-demo/src/frontend
npm install
npm run dev
```

### 5. アクセス

ブラウザで http://localhost:3000 を開く

### 6. ログイン

以下のテストユーザーでログイン：

| ユーザーID | 氏名 | パスワード |
|----------|------|-----------|
| 1 | 田中太郎 | password123 |
| 2 | 鈴木花子 | password123 |
| 3 | 山田次郎 | password123 |

## トラブルシューティング

### エラー: "Apache Maven 4.x requires Java 17"

→ DevContainer を再ビルドしてください

### エラー: "Unable to access jarfile /opt/payara-micro.jar"

→ DevContainer を再ビルドしてください

### ポート競合エラー

既に起動中のサービスがある場合：
```bash
# プロセス確認
lsof -i :8080  # User Service
lsof -i :8081  # Auth Service
lsof -i :8082  # Point Service
lsof -i :8090  # BFF
lsof -i :3000  # Frontend

# プロセス終了
kill -9 <PID>
```

### VS Codeデバッグ実行（推奨）

手動起動の代わりに、VS Codeのデバッグ機能を使用することを推奨します：

1. デバッグパネルを開く（Ctrl+Shift+D / Cmd+Shift+D）
2. 「Debug All Services」を選択
3. F5キーを押す

これにより全サービスが自動的にビルド・起動され、デバッガーもアタッチされます。

### データベース接続エラー

PostgreSQL が起動していることを確認：
```bash
pg_isready -U postgres
```

### フロントエンドが起動しない

node_modules を削除して再インストール：
```bash
cd /workspaces/my-ws-demo/frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

## 動作確認

### API の動作確認

#### ポイント管理
```bash
# ポイント残高取得
curl http://localhost:8090/api/points -H "Authorization: Bearer <token>"

# ポイント履歴取得
curl http://localhost:8090/api/points/history?page=1&limit=10 -H "Authorization: Bearer <token>"
```

### データベースの確認

```bash
# PostgreSQL に接続
psql -U postgres -h localhost

# データベース一覧
\l

# user_service_db に接続
\c user_service_db

# テーブル確認
\dt

# ユーザー確認
SELECT * FROM users;

# 終了
\q
```

## 次のステップ

- [README.md](../README.md) - プロジェクト全体の概要
- [アーキテクチャドキュメント](./architecture.md) - 詳細な設計
- [User Service README](../src/user-service/README.md) - ユーザーサービスAPI
- [Auth Service README](../src/auth-service/README.md) - 認証サービスAPI
- [BFF README](../src/bff/README.md) - BFF API
