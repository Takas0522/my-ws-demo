# Cucumber E2Eテスト - ログイン機能

このディレクトリには、Cucumberを使用したBDD形式のE2Eテストが含まれています。

## 構造

```
features/
└── login.feature              # ログイン機能のテストシナリオ

step-definitions/
└── login.steps.ts             # ステップ定義の実装

pages/
├── BasePage.ts                # ページオブジェクトの基底クラス
└── LoginPage.ts               # ログインページオブジェクト

cucumber.js                    # Cucumberの設定ファイル
cucumber.d.ts                  # TypeScript型定義
```

## Featureファイル (login.feature)

Gherkin構文で記述されたテストシナリオ：

- **ログインページの表示確認** - UI要素が正しく表示されるか
- **ユーザー名でログイン** - ユーザー名での認証
- **UUIDでログイン** - UUID形式での認証
- **無効な認証情報** - エラーハンドリングの確認
- **フォームのクリア** - 入力値のクリア機能
- **複数ユーザーでのログイン** - シナリオアウトラインを使用

## ステップ定義 (login.steps.ts)

各Gherkinステップの実装：

### 前提・背景
- `Given ログインページを表示している` - ログインページへ移動

### 操作
- `When ユーザーIDに {string} を入力する` - ユーザーID入力
- `When パスワードに {string} を入力する` - パスワード入力
- `When ログインボタンをクリックする` - ログインボタンクリック
- `When フォームをクリアする` - フォームクリア

### 検証
- `Then ページタイトルが {string} と表示される` - タイトル確認
- `Then ユーザーID入力欄が表示される` - UI要素の表示確認
- `Then パスワード入力欄が表示される` - UI要素の表示確認
- `Then ログインボタンが表示される` - ボタンの表示確認
- `Then テストユーザー情報が表示される` - テスト情報の表示確認
- `Then アカウントページにリダイレクトされる` - ページ遷移の確認
- `Then エラーメッセージが表示される` - エラー表示の確認
- `Then ユーザーID入力欄の値が {string} である` - 入力値の確認
- `Then パスワード入力欄の値が {string} である` - 入力値の確認

## テストの実行方法

### Cucumberテストの実行

```bash
cd /workspaces/my-ws-demo/src/e2e

# 全てのfeatureファイルを実行
npm run test:cucumber

# 特定のfeatureファイルを実行
npx cucumber-js features/login.feature

# 特定のタグのテストのみ実行（タグを追加した場合）
npx cucumber-js --tags "@smoke"
```

### Playwrightテストの実行

```bash
# 通常のPlaywrightテスト
npm test

# 特定のテストファイル
npx playwright test tests/login.spec.ts
```

## レポート

テスト実行後、以下のレポートが生成されます：

- `cucumber-report.html` - Cucumberテストの結果レポート

## 設定

### cucumber.js

Cucumberの実行設定：
- TypeScriptサポート（ts-node/register）
- ステップ定義の場所
- レポート形式（summary, progress-bar, html）

### cucumber.d.ts

TypeScript用の型定義ファイル。Cucumberの関数に型安全性を提供します。

## シナリオの例

### 基本的なシナリオ

```gherkin
シナリオ: ユーザー名で正常にログインできる
  もし ユーザーIDに "tanaka_taro" を入力する
  かつ パスワードに "password123" を入力する
  かつ ログインボタンをクリックする
  ならば アカウントページにリダイレクトされる
```

### シナリオアウトライン（データ駆動テスト）

```gherkin
シナリオアウトライン: 複数のユーザーでログインできる
  もし ユーザーIDに "<ユーザーID>" を入力する
  かつ パスワードに "<パスワード>" を入力する
  かつ ログインボタンをクリックする
  ならば アカウントページにリダイレクトされる

  例:
    | ユーザーID      | パスワード     |
    | tanaka_taro    | password123   |
    | suzuki_hanako  | password123   |
```

## 新しいシナリオの追加方法

1. **Featureファイルの作成/更新**
   - `features/` ディレクトリに `.feature` ファイルを作成
   - Gherkin構文でシナリオを記述

2. **ステップ定義の実装**
   - `step-definitions/` ディレクトリに `.steps.ts` ファイルを作成
   - Given/When/Then関数でステップを実装

3. **ページオブジェクトの活用**
   - 既存のページオブジェクトを使用
   - 必要に応じて新しいページオブジェクトを作成

## ベストプラクティス

1. **ステップの再利用**: 汎用的なステップ定義を作成し、複数のシナリオで再利用
2. **ページオブジェクトパターン**: UI操作はページオブジェクトに集約
3. **明確なシナリオ**: ビジネス価値が明確なシナリオを記述
4. **データ駆動**: シナリオアウトラインで複数のデータパターンをテスト
5. **独立性**: 各シナリオは他のシナリオに依存しない

## トラブルシューティング

### TypeScriptのエラーが出る場合

```bash
npm install --save-dev @types/node ts-node
```

### Cucumberが見つからない場合

```bash
npm install --save-dev @cucumber/cucumber
```

### Playwrightが見つからない場合

```bash
npm install --save-dev @playwright/test
```

## 参考資料

- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Playwright Documentation](https://playwright.dev/)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
