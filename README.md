# マイクロサービス風味アプリケーション

Java 21 + Payara 6 + PostgreSQL + Vue 3 で構築されたマイクロサービス風味のアーキテクチャのアプリケーション

> **🚀 Java 21 Migration Completed!**  
> このアプリケーションは Java 11 から Java 21 (LTS) に正常に移行されました。  
> 詳細は [移行結果ドキュメント](docs/modernization-result.md) と [移行サマリー](MIGRATION_SUMMARY.md) を参照してください。

**注意**:
- 初回実行時は5〜7分かかります（Docker イメージの取得、サービスの起動）
- 最低8GBのメモリが推奨されます
- `Language Support for Java by Red Hat`の関係で、個別でターミナルから実行する必要があります
- 開発用のDatabaseはDevContainer立ち上げ時にDockerComposeで同時に立ち上がります。

詳細は [E2E README](./src/e2e/README.md) を参照してください。

## � ドキュメント

アプリケーションの詳細な設計ドキュメントは [docs](./docs) ディレクトリにあります。

### ドキュメント一覧

1. [サービス概要](docs/01_サービス概要.md) - システム全体のアーキテクチャと各サービスの役割
2. [API仕様書](docs/02_API仕様書.md) - BFFおよび各マイクロサービスのAPI仕様
3. [サービス間通信経路](docs/03_サービス間通信経路.md) - サービス間の通信プロトコルと経路
4. [ユーザー利用シーケンス図](docs/04_ユーザー利用シーケンス図.md) - ユーザー操作時のシーケンス図
5. [データベース構造](docs/05_データベース構造.md) - ER図とテーブル定義
6. [画面フロー図](docs/06_画面フロー図.md) - フロントエンドの画面遷移
7. [初期Seedデータ](docs/07_初期Seedデータ.md) - テストユーザーとSeedデータの詳細
8. **[Java 21 移行結果](docs/modernization-result.md)** - Java 11 から Java 21 への移行の詳細レポート（2025年12月）
9. **[移行サマリー](MIGRATION_SUMMARY.md)** - 移行プロジェクトのエグゼクティブサマリー

### 技術スタック

#### バックエンド
- **Java**: 21 (LTS - 2029年9月までサポート)
- **Jakarta EE**: 10
- **Payara Micro**: 6.2024.10
- **Jersey**: 3.1.5 (JAX-RS implementation)
- **PostgreSQL**: 16

#### フロントエンド
- **Vue.js**: 3
- **Vite**: Build tool

#### テスト
- **JUnit**: 5.9.3 (ユニット/統合テスト)
- **Mockito**: 5.3.1 (モッキング)
- **Playwright**: E2Eテスト
- **Cucumber**: BDDテスト
- **TestContainers**: 統合テスト用データベース

## �🐛 デバッグ実行

各マイクロサービスはPayara Microを使用してデバッグモードで実行できます。デバッグポートを指定してIDEから接続してください。

### 各サービスのポート設定

- **user-service**: アプリケーションポート 8080, デバッグポート 5005
- **auth-service**: アプリケーションポート 8081, デバッグポート 5006
- **point-service**: アプリケーションポート 8082, デバッグポート 5007
- **bff**: アプリケーションポート 8090, デバッグポート 5008

### 各サービスのデバッグ実行コマンド

ルートディレクトリから以下のコマンドを実行してください：

**注意**: 各サービスは`.env`ファイルからデータベース接続設定などを読み込みます。e2eテストやintegrationtテストを実行する際は別のenvを参照します。  
`.env`ファイルはClone時は存在しませんので、 `.env.sample` をコピーしてリネームしてお使いください。
開発時のDBはDevContainerで提供されます。シードデータもDevContainer展開時に登録されます。

**Java 21対応**: Payara 6 + Jakarta EE 10 をJava 21で実行するため、以下の`--add-opens`オプションが必要です。

#### user-service
```bash
cd src/user-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && \
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

#### auth-service
```bash
cd src/auth-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && \
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

#### point-service
```bash
cd src/point-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && \
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

#### bff
```bash
cd src/bff && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && \
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

#### frontend
```bash
cd src/frontend && npm run dev
```

IDE (例: IntelliJ IDEA, VS Code) でリモートデバッガーを設定し、デバッグポートに接続してください。

## 🧪 E2Eテスト

E2Eテストは、Playwright + Cucumberを使用してブラウザの自動化テストを行います。TestContainersにより、テスト専用のPostgreSQLコンテナが自動的に起動し、開発用DBを汚しません。

### E2Eテストの実行

ルートディレクトリから以下のコマンドを実行してください：

```bash
cd src/e2e && npm test
```

または、Cucumberのテストを実行する場合：

```bash
cd src/e2e && npm run test:cucumber
```

詳細は [E2E README](./src/e2e/README.md) を参照してください。

## 🤝 貢献

プルリクエストを歓迎します。大きな変更の場合は、まずissueを開いて変更内容を議論してください。

## 📝 ライセンス

MIT
