# Integration Test

このドキュメントでは、User-ServiceのIntegration Testについて説明します。

## 概要

Integration Testは、TestContainersを使用してPostgreSQLコンテナを起動し、実際のデータベースに対してテストを実行します。DevContainerのDBとは独立したテスト環境で動作します。

## テスト構成

### 使用技術
- **JUnit 5**: テストフレームワーク
- **TestContainers**: PostgreSQL 15 コンテナの起動・管理
- **PostgreSQL Driver**: データベース接続

### テスト対象
`UserRepository` クラスのDB操作メソッド:
- `findAll()`: 全ユーザーの取得
- `findById(UUID)`: IDによるユーザーの取得
- `findByUsername(String)`: ユーザー名によるユーザーの取得
- `create(User)`: ユーザーの作成
- `update(User)`: ユーザーの更新
- `delete(UUID)`: ユーザーの削除

## ディレクトリ構成

```
src/user-service/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/microservices/user/
│   │           ├── model/User.java
│   │           └── repository/UserRepository.java
│   ├── test/                          # Unit Test用ディレクトリ
│   │   └── java/
│   │       └── com/example/microservices/userservice/
│   │           └── HelloWorldTest.java
│   └── integration-test/              # Integration Test専用ディレクトリ
│       ├── java/
│       │   └── com/example/microservices/user/
│       │       └── integration/
│       │           ├── BaseIntegrationTest.java         # Integration Test ベースクラス
│       │           └── UserRepositoryIT.java            # UserRepository の Integration Test
│       └── resources/
│           ├── test-schema.sql       # テスト用スキーマ定義
│           ├── test-data.sql         # テスト用データ投入スクリプト
│           └── cleanup-data.sql      # テストデータクリーンアップスクリプト
```

Integration TestはUnit Testと完全に分離されたディレクトリ構造で管理されています。

## テストデータ管理

### セットアップ (各テスト実行前)
1. `test-schema.sql`: テーブルスキーマの作成
2. `test-data.sql`: テストデータの投入

### クリーンアップ (各テスト実行後)
- `cleanup-data.sql`: テストデータの削除

## テストの実行

### Unit TestとIntegration Testの分離

Integration TestとUnit Testは完全に分離されています:

- **Unit Test**
  - ディレクトリ: `src/test/java`
  - 命名規則: `*Test.java`
  - 実行フェーズ: `test`

- **Integration Test**
  - ディレクトリ: `src/integration-test/java`
  - 命名規則: `*IT.java`
  - 実行フェーズ: `integration-test` / `verify`

### 全てのテスト(Unit + Integration)を実行
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn clean verify
```

### Unit Testのみを実行
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn clean test
```

### Integration Testのみを実行
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn clean verify -DskipTests
```

または、failsafeプラグインを直接実行:
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn clean compile test-compile failsafe:integration-test failsafe:verify
```

### 特定のIntegration Testを実行
```bash
cd /workspaces/my-ws-demo/src/user-service
mvn verify -Dit.test=UserRepositoryIT
```

## TestContainersの動作

1. テストクラスの実行前に、PostgreSQL 15のDockerコンテナが自動的に起動されます
2. 各テストメソッド実行前に、スキーマとテストデータがセットアップされます
3. 各テストメソッド実行後に、テストデータがクリーンアップされます
4. 全てのテスト完了後、コンテナは自動的に停止・削除されます

## テストの追加

新しいIntegration Testを追加する場合:

1. `src/integration-test/java` 配下に `BaseIntegrationTest` を継承したテストクラスを作成
2. クラス名を `*IT.java` にする
3. `@Test` アノテーションを付けたテストメソッドを実装

例:
```java
package com.example.microservices.user.integration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyNewIntegrationIT extends BaseIntegrationTest {
    
    @Test
    void testSomething() {
        // テストコード
    }
}
```

## コマンドクイックリファレンス

| 目的 | コマンド |
|------|---------|
| Unit Testのみ実行 | `mvn clean test` |
| Integration Testのみ実行 | `mvn clean compile test-compile failsafe:integration-test failsafe:verify` |
| 全てのテスト実行 | `mvn clean verify` |
| 特定のIntegration Test実行 | `mvn verify -Dit.test=UserRepositoryIT` |
| ビルドのみ（テストスキップ） | `mvn clean package -DskipTests` |

## トラブルシューティング

### Dockerが起動していない
TestContainersはDockerを必要とします。DevContainer内ではDocker-in-Dockerが利用可能です。

### ポートの競合
TestContainersは空いているポートを自動的に割り当てるため、通常はポートの競合は発生しません。

### メモリ不足
大量のテストを実行する場合、Dockerのメモリ設定を確認してください。

## 参考リンク

- [TestContainers Documentation](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
