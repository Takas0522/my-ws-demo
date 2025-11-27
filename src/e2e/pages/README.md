# Page Object Model (POM) for E2E Tests

このディレクトリには、Playwright E2Eテスト用のページオブジェクトクラスが含まれています。

## 構造

```
pages/
├── BasePage.ts      # 全ページオブジェクトの基底クラス
├── LoginPage.ts     # ログインページのページオブジェクト
└── index.ts         # エクスポート用インデックスファイル
```

## クラス概要

### BasePage

全てのページオブジェクトで共通する機能を提供する基底クラスです。

**主なメソッド:**
- `goto(url)` - 指定されたURLに移動
- `getTitle()` - ページタイトルを取得
- `getUrl()` - 現在のURLを取得
- `waitForElement(locator, timeout)` - 要素が表示されるまで待機
- `fill(locator, text)` - テキストを入力
- `click(locator)` - 要素をクリック
- `getText(locator)` - 要素のテキストを取得
- `isVisible(locator)` - 要素が表示されているかを確認
- `isEnabled(locator)` - 要素が有効かどうかを確認
- `waitForLoadState()` - ページの読み込みが完了するまで待機
- `screenshot(path)` - スクリーンショットを取得

### LoginPage

ログインページの要素と操作を定義したページオブジェクトです。

**ロケーター:**
- `userIdInput` - ユーザーID入力フィールド
- `passwordInput` - パスワード入力フィールド
- `submitButton` - ログインボタン
- `errorMessage` - エラーメッセージ
- `pageTitle` - ページタイトル
- `testUserInfo` - テストユーザー情報

**主なメソッド:**
- `navigate(baseUrl)` - ログインページに移動
- `enterUserId(userId)` - ユーザーIDを入力
- `enterPassword(password)` - パスワードを入力
- `clickLoginButton()` - ログインボタンをクリック
- `login(userId, password)` - ログイン処理を実行（入力+クリック）
- `getErrorMessage()` - エラーメッセージを取得
- `isErrorMessageVisible()` - エラーメッセージが表示されているかを確認
- `getPageTitle()` - ページタイトルのテキストを取得
- `isLoginButtonEnabled()` - ログインボタンが有効かどうかを確認
- `getLoginButtonText()` - ログインボタンのテキストを取得
- `isTestUserInfoVisible()` - テストユーザー情報が表示されているかを確認
- `clearForm()` - フォームをクリア
- `waitForSuccessfulLogin(expectedUrl, timeout)` - ログイン成功後のリダイレクトを待機

## 使用例

### 基本的な使用方法

```typescript
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test('ログインテスト', async ({ page }) => {
  const loginPage = new LoginPage(page);
  
  // ログインページに移動
  await loginPage.navigate();
  
  // ログイン処理
  await loginPage.login('tanaka_taro', 'password123');
  
  // リダイレクトを待機
  await loginPage.waitForSuccessfulLogin('/account');
  
  // URLを確認
  expect(loginPage.getUrl()).toContain('/account');
});
```

### 詳細な操作

```typescript
test('詳細なログインテスト', async ({ page }) => {
  const loginPage = new LoginPage(page);
  
  await loginPage.navigate();
  
  // ページが正しく表示されているか確認
  expect(await loginPage.getPageTitle()).toBe('ログイン');
  expect(await loginPage.isUserIdInputVisible()).toBeTruthy();
  expect(await loginPage.isPasswordInputVisible()).toBeTruthy();
  
  // 個別に入力
  await loginPage.enterUserId('suzuki_hanako');
  await loginPage.enterPassword('password123');
  
  // 入力値を確認
  expect(await loginPage.getUserIdValue()).toBe('suzuki_hanako');
  
  // ログインボタンをクリック
  await loginPage.clickLoginButton();
  
  // 成功を確認
  await loginPage.waitForSuccessfulLogin('/account');
});
```

### エラーケースのテスト

```typescript
test('無効な認証情報でログイン失敗', async ({ page }) => {
  const loginPage = new LoginPage(page);
  
  await loginPage.navigate();
  await loginPage.login('invalid_user', 'wrong_password');
  
  // エラーメッセージを確認
  expect(await loginPage.isErrorMessageVisible()).toBeTruthy();
  const errorMessage = await loginPage.getErrorMessage();
  expect(errorMessage).toContain('失敗');
});
```

## 設計原則

1. **カプセル化**: ページの構造の詳細をテストコードから隠蔽
2. **再利用性**: 共通のメソッドを基底クラスに集約
3. **保守性**: ページの変更は該当するページオブジェクトのみを修正
4. **可読性**: メソッド名は操作内容を明確に表現
5. **型安全性**: TypeScriptを使用して型安全なコードを実現

## 新しいページオブジェクトの追加

新しいページのページオブジェクトを追加する場合：

1. `BasePage`を継承した新しいクラスを作成
2. ページ固有のロケーターを定義
3. ページ固有の操作メソッドを実装
4. `index.ts`にエクスポートを追加

例:

```typescript
import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

export class AccountPage extends BasePage {
  private readonly userName: Locator;
  private readonly logoutButton: Locator;

  constructor(page: Page) {
    super(page);
    this.userName = page.locator('.user-name');
    this.logoutButton = page.locator('button:has-text("ログアウト")');
  }

  async getUserName(): Promise<string> {
    return await this.getText(this.userName);
  }

  async logout(): Promise<void> {
    await this.click(this.logoutButton);
  }
}
```

## テストの実行

```bash
# 全てのテストを実行
npm test

# 特定のテストファイルを実行
npx playwright test tests/login.spec.ts

# UIモードで実行
npx playwright test --ui

# デバッグモードで実行
npx playwright test --debug
```

## 参考資料

- [Playwright Documentation](https://playwright.dev/)
- [Page Object Model Pattern](https://playwright.dev/docs/pom)
- [Best Practices](https://playwright.dev/docs/best-practices)
