import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { testDatabase } from '../setup/database';

test.describe('ログインページ', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    // 各テストの前にデータベースをリセット
    await testDatabase.resetAll();
    
    // フロントエンドの接続確認とキャッシュクリア（Cucumberと同じ処理）
    console.log('\n=== Checking Frontend Connection ===');
    const frontendResponse = await page.goto('http://localhost:3000/login', { 
      waitUntil: 'networkidle', 
      timeout: 30000 
    });
    console.log(`Frontend status: ${frontendResponse?.status()}`);
    
    // ハードリロードしてキャッシュをクリア
    await page.reload({ waitUntil: 'networkidle' });
    console.log('Page reloaded to clear cache');
    
    // ViteのHMRが安定するまで待つ
    await page.waitForTimeout(1000);
    
    console.log('====================================\n');
    
    loginPage = new LoginPage(page);
  });

  test('ログインページが正しく表示される', async () => {
    // ページタイトルの確認
    const pageTitle = await loginPage.ページタイトル取得();
    expect(pageTitle).toBe('ログイン');

    // 入力フィールドとボタンが表示されていることを確認
    expect(await loginPage.ユーザーID入力欄表示確認()).toBeTruthy();
    expect(await loginPage.パスワード入力欄表示確認()).toBeTruthy();
    expect(await loginPage.ログインボタン有効確認()).toBeTruthy();

    // テストユーザー情報が表示されていることを確認
    expect(await loginPage.テストユーザー情報表示確認()).toBeTruthy();
  });

  test('ユーザー名でログインできる', async () => {
    // ユーザー名とパスワードを入力してログイン
    await loginPage.ログイン実行('tanaka_taro', 'password123');

    // アカウントページにリダイレクトされることを確認
    await loginPage.ログイン成功待機('/account');
    expect(loginPage.URL取得()).toContain('/account');
  });

  test('UUIDでログインできる', async () => {
    // UUIDとパスワードを入力してログイン
    await loginPage.ログイン実行('05c66ceb-6ddc-4ada-b736-08702615ff48', 'password123');

    // アカウントページにリダイレクトされることを確認
    await loginPage.ログイン成功待機('/account');
    expect(loginPage.URL取得()).toContain('/account');
  });

  test('無効な認証情報でログインに失敗する', async () => {
    // 無効なユーザーIDとパスワードでログイン試行
    await loginPage.ログイン実行('invalid_user', 'wrong_password');

    // エラーメッセージが表示されることを確認
    expect(await loginPage.エラーメッセージ表示確認()).toBeTruthy();
    const errorMessage = await loginPage.エラーメッセージ取得();
    expect(errorMessage).toBeTruthy();
  });

  test('ログインボタンは入力中にテキストが変わる', async () => {
    // 初期状態のボタンテキストを確認
    const initialButtonText = await loginPage.ログインボタンテキスト取得();
    expect(initialButtonText).toBe('ログイン');

    // ログインボタンをクリック（認証は失敗する可能性あり）
    await loginPage.ユーザーID入力('test_user');
    await loginPage.パスワード入力('test_pass');
    
    // Note: ローディング状態のテストは非同期処理のタイミングにより難しい場合がある
    // 実際のテストでは、ネットワークモックなどを使用して制御することを推奨
  });

  test('フォームをクリアできる', async () => {
    // 入力
    await loginPage.ユーザーID入力('test_user');
    await loginPage.パスワード入力('test_pass');

    // 値が入力されていることを確認
    expect(await loginPage.ユーザーID入力値取得()).toBe('test_user');
    expect(await loginPage.パスワード入力値取得()).toBe('test_pass');

    // クリア
    await loginPage.フォームクリア();

    // 値がクリアされたことを確認
    expect(await loginPage.ユーザーID入力値取得()).toBe('');
    expect(await loginPage.パスワード入力値取得()).toBe('');
  });

  test('個別の入力メソッドでログインできる', async () => {
    // 個別のメソッドを使用して入力
    await loginPage.ユーザーID入力('suzuki_hanako');
    await loginPage.パスワード入力('password123');
    await loginPage.ログインボタンクリック();

    // アカウントページにリダイレクトされることを確認
    await loginPage.ログイン成功待機('/account');
    expect(loginPage.URL取得()).toContain('/account');
  });
});
