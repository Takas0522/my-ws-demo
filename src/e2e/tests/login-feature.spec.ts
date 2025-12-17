import { test, expect } from '@playwright/test';
import { LoginPage, AccountPage } from '../pages';
import { testDatabase } from '../setup/database';

/**
 * ログイン機能テスト
 * login.featureのシナリオに基づくPlaywrightテスト
 */
test.describe('ログイン機能', () => {
  let loginPage: LoginPage;
  let accountPage: AccountPage;

  test.beforeEach(async ({ page }) => {
    // 各テストの前にデータベースをリセット
    await testDatabase.resetAll();
    
    // フロントエンドの接続確認とキャッシュクリア
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
    accountPage = new AccountPage(page);
  });

  test.describe('背景: ログインページを表示している', () => {
    test.beforeEach(async () => {
      // 各テストで共通の前提条件を実行
      await loginPage.ログインページへ移動();
    });

    test('ログインページの表示確認', async () => {
      // ページタイトルが "ログイン" と表示される
      const pageTitle = await loginPage.ページタイトル取得();
      expect(pageTitle).toBe('ログイン');
      
      // ユーザーID入力欄が表示される
      expect(await loginPage.ユーザーID入力欄表示確認()).toBeTruthy();
      
      // パスワード入力欄が表示される
      expect(await loginPage.パスワード入力欄表示確認()).toBeTruthy();
      
      // ログインボタンが表示される
      expect(await loginPage.ログインボタン有効確認()).toBeTruthy();
      
      // テストユーザー情報が表示される
      expect(await loginPage.テストユーザー情報表示確認()).toBeTruthy();
    });

    test('ユーザー名で正常にログインできる', async () => {
      // ユーザーIDに "tanaka_taro" を入力する
      await loginPage.ユーザーID入力('tanaka_taro');
      
      // パスワードに "password123" を入力する
      await loginPage.パスワード入力('password123');
      
      // ログインボタンをクリックする
      await loginPage.ログインボタンクリック();
      
      // アカウントページにリダイレクトされる
      expect(accountPage.URL取得()).toContain('/account');
    });

    test('UUIDで正常にログインできる', async () => {
      // ユーザーIDに "05c66ceb-6ddc-4ada-b736-08702615ff48" を入力する
      await loginPage.ユーザーID入力('05c66ceb-6ddc-4ada-b736-08702615ff48');
      
      // パスワードに "password123" を入力する
      await loginPage.パスワード入力('password123');
      
      // ログインボタンをクリックする
      await loginPage.ログインボタンクリック();
      
      // アカウントページにリダイレクトされる
      expect(accountPage.URL取得()).toContain('/account');
    });

    test('無効な認証情報でログインに失敗する', async () => {
      // ユーザーIDに "invalid_user" を入力する
      await loginPage.ユーザーID入力('invalid_user');
      
      // パスワードに "wrong_password" を入力する
      await loginPage.パスワード入力('wrong_password');
      
      // ログインボタンをクリックする
      await loginPage.ログインボタンクリック();
      
      // エラーメッセージが表示される
      expect(await loginPage.エラーメッセージ表示確認()).toBeTruthy();
    });

    test('フォームの入力値をクリアできる', async () => {
      // ユーザーIDに "test_user" を入力する
      await loginPage.ユーザーID入力('test_user');
      
      // パスワードに "test_pass" を入力する
      await loginPage.パスワード入力('test_pass');
      
      // ユーザーID入力欄の値が "test_user" である
      expect(await loginPage.ユーザーID入力値取得()).toBe('test_user');
      
      // パスワード入力欄の値が "test_pass" である
      expect(await loginPage.パスワード入力値取得()).toBe('test_pass');
      
      // フォームをクリアする
      await loginPage.フォームクリア();
      
      // ユーザーID入力欄の値が "" である
      expect(await loginPage.ユーザーID入力値取得()).toBe('');
      
      // パスワード入力欄の値が "" である
      expect(await loginPage.パスワード入力値取得()).toBe('');
    });

    // 複数のユーザーでログインできるテスト（シナリオアウトライン）
    const loginTestData = [
      { userId: 'tanaka_taro', password: 'password123' },
      { userId: 'suzuki_hanako', password: 'password123' },
      { userId: 'yamada_jiro', password: 'password123' },
      { userId: '05c66ceb-6ddc-4ada-b736-08702615ff48', password: 'password123' },
    ];

    loginTestData.forEach(({ userId, password }) => {
      test(`複数のユーザーでログインできる: ${userId}`, async () => {
        // ユーザーIDに "<ユーザーID>" を入力する
        await loginPage.ユーザーID入力(userId);
        
        // パスワードに "<パスワード>" を入力する
        await loginPage.パスワード入力(password);
        
        // ログインボタンをクリックする
        await loginPage.ログインボタンクリック();
        
        // アカウントページにリダイレクトされる
        expect(accountPage.URL取得()).toContain('/account');
      });
    });
  });
});