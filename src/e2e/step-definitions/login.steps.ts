import { Given, When, Then } from '@cucumber/cucumber';
import { expect, Request, Response } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

// LoginPageインスタンスを保持
let loginPage: LoginPage;

// 背景: ログインページを表示している
Given('ログインページを表示している', async function () {
  // hooksからページインスタンスを取得
  loginPage = new LoginPage(this.page);
  
  // フロントエンドの接続確認（キャッシュをクリア）
  console.log('\n=== Checking Frontend Connection ===');
  const frontendResponse = await this.page.goto('http://localhost:3000/login', { 
    waitUntil: 'networkidle', 
    timeout: 30000 
  });
  console.log(`Frontend status: ${frontendResponse?.status()}`);
  
  // ハードリロードしてキャッシュをクリア
  await this.page.reload({ waitUntil: 'networkidle' });
  console.log('Page reloaded to clear cache');
  
  // ViteのHMRが安定するまで待つ
  await this.page.waitForTimeout(1000);
  
  console.log('====================================\n');
});

// ページタイトルの確認
Then('ページタイトルが {string} と表示される', async function (expectedTitle: string) {
  const actualTitle = await loginPage.ページタイトル取得();
  expect(actualTitle).toBe(expectedTitle);
});

// 要素の表示確認
Then('ユーザーID入力欄が表示される', async function () {
  const isVisible = await loginPage.ユーザーID入力欄表示確認();
  expect(isVisible).toBeTruthy();
});

Then('パスワード入力欄が表示される', async function () {
  const isVisible = await loginPage.パスワード入力欄表示確認();
  expect(isVisible).toBeTruthy();
});

Then('ログインボタンが表示される', async function () {
  const isEnabled = await loginPage.ログインボタン有効確認();
  expect(isEnabled).toBeTruthy();
});

Then('テストユーザー情報が表示される', async function () {
  const isVisible = await loginPage.テストユーザー情報表示確認();
  expect(isVisible).toBeTruthy();
});

// 入力操作
When('ユーザーIDに {string} を入力する', async function (userId: string) {
  await loginPage.ユーザーID入力(userId);
});

When('パスワードに {string} を入力する', async function (password: string) {
  await loginPage.パスワード入力(password);
});

When('ログインボタンをクリックする', async function () {
  // すべてのリクエストとレスポンスを詳細にログ
  const allActivity: string[] = [];
  
  this.page.on('request', (request: Request) => {
    const headers = request.headers();
    const authHeader = headers['authorization'] || 'NONE';
    const entry = `[REQUEST] ${request.method()} ${request.url()} [Auth: ${authHeader.substring(0, 20)}...]`;
    allActivity.push(entry);
    if (request.url().includes('/api/')) {
      console.log(entry);
    }
  });
  
  this.page.on('response', (response: Response) => {
    const entry = `[RESPONSE] ${response.status()} ${response.url()}`;
    allActivity.push(entry);
    if (response.url().includes('/api/')) {
      console.log(entry);
    }
  });
  
  // コンソールログを監視
  this.page.on('console', (msg) => {
    if (msg.type() === 'error' || msg.text().includes('[LOGIN]')) {
      console.log(`Browser console [${msg.type()}]:`, msg.text());
    }
  });
  
  // APIレスポンスを待つためのPromiseを作成
  const apiResponsePromise = this.page.waitForResponse(
    (response: Response) => response.url().includes('/api/login'),
    { timeout: 10000 }
  );
  
  // ログインボタンをクリック
  await loginPage.ログインボタンクリック();
  
  // APIレスポンスを待つ
  try {
    const response = await apiResponsePromise;
    console.log(`\n✓ Login API called: ${response.status()} ${response.url()}`);
    try {
      const body = await response.json();
      console.log('Response body:', JSON.stringify(body, null, 2));
    } catch (e) {
      console.log('Response body: (not JSON)');
    }
    
    // レスポンス後、少し待ってからlocalStorageを確認
    await this.page.waitForTimeout(1000);
    const token = await this.page.evaluate(() => localStorage.getItem('authToken'));
    console.log(`LocalStorage token after login: ${token ? 'SET' : 'NOT SET'}`);
    
    // どのリクエストが送信されたか確認
    console.log('\nAll API activity:');
    allActivity.filter(a => a.includes('/api/')).forEach(a => console.log(`  ${a}`));
  } catch (error) {
    console.log('\n✗ Login API response timeout');
    console.log('All requests made:');
    allActivity.forEach(a => console.log(`  - ${a}`));
  }
});

// ページ遷移の確認
Then('アカウントページにリダイレクトされる', async function () {
  console.log('=== Checking redirect to /account ===');
  console.log(`Current URL: ${this.page.url()}`);
  
  // ログインページの要素を確認
  const loginFormExists = await this.page.locator('#userId').count() > 0;
  const loginButtonExists = await this.page.locator('button[type="submit"]').count() > 0;
  console.log(`Login page elements - Form: ${loginFormExists}, Button: ${loginButtonExists}`);
  
  // エラーメッセージが表示されていないか確認
  await this.page.waitForTimeout(1000); // 少し待機
  
  const errorSelectors = ['.bg-red-100', '[class*="error"]', '[class*="Error"]'];
  for (const selector of errorSelectors) {
    const errorCount = await this.page.locator(selector).count();
    if (errorCount > 0) {
      const errorText = await this.page.locator(selector).first().textContent();
      if (errorText && errorText.trim()) {
        console.log(`Error message found: ${errorText.trim()}`);
        throw new Error(`Login failed with error: ${errorText.trim()}`);
      }
    }
  }
  
  try {
    // SPAアプリなので、DOM要素の変化を待つ
    console.log('Waiting for AccountView to appear...');
    
    // ログインページの要素が消えることを確認
    const loginFormDisappeared = await this.page.locator('#userId').waitFor({ 
      state: 'hidden', 
      timeout: 3000 
    }).then(() => true).catch(() => false);
    console.log(`Login form disappeared: ${loginFormDisappeared}`);
    
    // AccountView特有の要素が表示されるまで待つ
    console.log('Waiting for "会員マイページ" heading...');
    await this.page.locator('h1:has-text("会員マイページ")').waitFor({ 
      state: 'visible', 
      timeout: 15000 
    });
    console.log('✓ AccountView heading found!');
    
    // URL確認
    const currentUrl = this.page.url();
    console.log(`Final URL: ${currentUrl}`);
    
    // AccountPageの他の要素も確認
    const accountElements = {
      heading: await this.page.locator('h1:has-text("会員マイページ")').count() > 0,
      logoutButton: await this.page.locator('button:has-text("ログアウト")').count() > 0,
      pointsLink: await this.page.locator('a[href="/points"]').count() > 0,
    };
    console.log('AccountView elements:', JSON.stringify(accountElements));
    
    expect(currentUrl).toContain('/account');
    console.log('✓ Successfully redirected to /account');
  } catch (error) {
    const currentUrl = this.page.url();
    console.log(`✗ Failed to redirect. Current URL: ${currentUrl}`);
    
    // デバッグ: ページの詳細情報を確認
    console.log(`Page title: ${await this.page.title()}`);
    
    // ページに何が表示されているか確認
    const pageElements = {
      loginForm: await this.page.locator('#userId').count() > 0,
      loginButton: await this.page.locator('button[type="submit"]').count() > 0,
      accountHeading: await this.page.locator('h1:has-text("会員マイページ")').count() > 0,
      anyH1: await this.page.locator('h1').count(),
      h1Text: await this.page.locator('h1').first().textContent().catch(() => 'N/A'),
    };
    console.log('Current page elements:', JSON.stringify(pageElements, null, 2));
    
    // LocalStorage確認
    const token = await this.page.evaluate(() => localStorage.getItem('authToken'));
    const userId = await this.page.evaluate(() => localStorage.getItem('userId'));
    console.log(`LocalStorage - token: ${token ? 'EXISTS' : 'NULL'}, userId: ${userId || 'NULL'}`);
    
    throw error;
  }
  console.log('=====================================');
});

// エラーメッセージの確認
Then('エラーメッセージが表示される', async function () {
  // SPAアプリなので、エラーメッセージが表示されるまで待機
  try {
    // エラーメッセージ要素が表示されるまで待機
    await this.page.locator('.bg-red-100').waitFor({ state: 'visible', timeout: 10000 });
    const isVisible = await loginPage.エラーメッセージ表示確認();
    expect(isVisible).toBeTruthy();
  } catch (error) {
    // デバッグ情報を出力
    const currentUrl = this.page.url();
    console.log(`Error message not found. Current URL: ${currentUrl}`);
    
    // ページに何が表示されているか確認
    const loginButton = await this.page.locator('button[type="submit"]').textContent();
    console.log('Login button text:', loginButton);
    throw error;
  }
});

// 入力値の確認
Then('ユーザーID入力欄の値が {string} である', async function (expectedValue: string) {
  const actualValue = await loginPage.ユーザーID入力値取得();
  expect(actualValue).toBe(expectedValue);
});

Then('パスワード入力欄の値が {string} である', async function (expectedValue: string) {
  const actualValue = await loginPage.パスワード入力値取得();
  expect(actualValue).toBe(expectedValue);
});

// フォームクリア
When('フォームをクリアする', async function () {
  await loginPage.フォームクリア();
});
