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
    ├── playwright-report/  # Playwright HTMLレポート
    ├── cucumber-report.html # Cucumber HTMLレポート
    └── cucumber-report.json # Cucumber JSONレポート
```

## テスト結果概要

### ユニットテスト

#### Javaサービス
- **auth-service**: 34件のテストが成功
- **bff**: 3件のテストが成功
- **point-service**: 38件のテストが成功
- **user-service**: 29件のテストが成功

#### フロントエンド (Jest)
- **テスト**: 109件が成功（全て）
- **カバレッジ**: 全体で78.99%
  - ステートメントカバレッジ: 78.99%
  - 分岐カバレッジ: 57.34%
  - 関数カバレッジ: 64.7%
  - 行カバレッジ: 84.53%
- **主要コンポーネント**:
  - LoginView.vue: 100%カバレッジ
  - pointApi.js: 100%カバレッジ
  - PointHistoryView.vue: 84.48%カバレッジ
  - AccountView.vue: 83.33%カバレッジ
  - PointView.vue: 78.94%カバレッジ
  - App.vue: 70.37%カバレッジ
  - api.js: 67.5%カバレッジ

### インテグレーションテスト

#### Javaサービス
- **auth-service**: 13件のテストが成功 (データベース統合テストを含む)
- **point-service**: 16件のテストが成功 (データベース統合テストを含む)
- **user-service**: 18件のテストが成功 (データベース統合テストを含む)

### E2Eテスト

#### Cucumber
- **シナリオ数**: 40シナリオ全て成功
- **ステップ数**: 431ステップ全て成功
- **実行時間**: 約6分29秒
- **レポート**: 
  - HTML: `e2e/cucumber-report.html`
  - JSON: `e2e/cucumber-report.json`

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

# CucumberのHTMLレポートを開く
open test-reports/e2e/cucumber-report.html
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
# Playwrightテスト
cd src/e2e && npm run test

# Cucumberテスト
cd src/e2e && npm run test:cucumber

# 両方のレポートを確認
cd src/e2e && npm run test:report  # Playwright
open src/e2e/cucumber-report.html   # Cucumber
```

## 注意事項

- E2Eテストは自動的にTestContainersとバックエンドサービスを起動します。手動でサービスを起動する必要はありません
- テストレポートは実行時のスナップショットです。最新の結果を得るには、テストを再実行してください
- E2EテストはPlaywrightとCucumberの両方のフレームワークで実装されています。それぞれ異なる視点でテストを実行します
