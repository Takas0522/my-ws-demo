# マイクロサービス風味アプリケーション

Java 11 + Payara + PostgreSQL + Vue 3 で構築されたマイクロサービス風味のアーキテクチャのアプリケーション

**注意**:
- 初回実行時は5〜7分かかります（Docker イメージの取得、サービスの起動）
- 最低8GBのメモリが推奨されます
- `Language Support for Java by Red Hat`の関係で、個別でターミナルから実行する必要があります
- 開発用のDatabaseはDevContainer立ち上げ時にDockerComposeで同時に立ち上がります。

詳細は [E2E README](./src/e2e/README.md) を参照してください。

## 🐛 デバッグ実行

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

#### user-service
```bash
cd src/user-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /opt/payara-micro.jar --deploy target/user-service.war --port 8080
```

#### auth-service
```bash
cd src/auth-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar /opt/payara-micro.jar --deploy target/auth-service.war --port 8081
```

#### point-service
```bash
cd src/point-service && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007 -jar /opt/payara-micro.jar --deploy target/point-service.war --port 8082
```

#### bff
```bash
cd src/bff && set -a && source <(grep -v '^#' .env) && set +a && mvn clean package && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008 -jar /opt/payara-micro.jar --deploy target/bff.war --port 8090
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
