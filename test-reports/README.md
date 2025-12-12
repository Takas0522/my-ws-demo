# テストレポート

このディレクトリには、プロジェクト全体のテスト実行結果が含まれています。

## ディレクトリ構造

```
test-reports/
├── unit/                    # ユニットテストレポート
│   ├── java/               # Javaサービスのユニットテスト
│   │   ├── auth-service/   # 認証サービス
│   │   ├── bff/            # Backend For Frontend
│   │   ├── point-service/  # ポイントサービス
│   │   └── user-service/   # ユーザーサービス
│   └── frontend/           # フロントエンドのユニットテスト
│       └── coverage/       # Jestカバレッジレポート
├── integration/            # インテグレーションテストレポート
│   └── java/
│       └── user-service/   # ユーザーサービスのインテグレーションテスト
└── e2e/                    # E2Eテストレポート
    └── playwright-report/  # Playwright HTMLレポート
```

## テスト結果概要

### ユニットテスト

#### Javaサービス
- **auth-service**: 3件のテストが成功
- **bff**: 3件のテストが成功
- **point-service**: 3件のテストが成功
- **user-service**: 3件のテストが成功

#### フロントエンド (Jest)
- **テスト**: 3件が成功
- **カバレッジ**: 全体で3.3%
  - App.vue: 100%カバレッジ

### インテグレーションテスト

#### user-service
- **テスト**: 8件が成功
- データベース統合テストを含む

### E2Eテスト

#### Playwright
- **実行環境**: Chromium, Firefox, WebKit
- **ステータス**: テストは実行されましたが、サーバーが起動していないため失敗
- **レポート**: `e2e/playwright-report/index.html`で詳細を確認可能

## レポートの閲覧方法

### Javaテストレポート
各サービスの`surefire-reports`または`failsafe-reports`ディレクトリ内のXMLファイルを参照してください。

### フロントエンドカバレッジレポート
```bash
# ブラウザでカバレッジレポートを開く
open test-reports/unit/frontend/coverage/lcov-report/index.html
```

### E2Eテストレポート
```bash
# PlaywrightのHTMLレポートを開く
open test-reports/e2e/playwright-report/index.html
```

## テストの再実行

### ユニットテスト
```bash
# Javaサービス
cd src/auth-service && mvn test
cd src/bff && mvn test
cd src/point-service && mvn test
cd src/user-service && mvn test

# フロントエンド
cd src/frontend && npm test
```

### インテグレーションテスト
```bash
cd src/user-service && mvn verify -DskipUnitTests
```

### E2Eテスト
```bash
# サーバーを起動してから実行
cd src/e2e && npx playwright test
```

## 注意事項

- E2Eテストを実行する前に、必要なサービス（フロントエンド、バックエンド、データベース）が起動していることを確認してください
- テストレポートは実行時のスナップショットです。最新の結果を得るには、テストを再実行してください
