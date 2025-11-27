import { Given, When, Then, Before, After, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium, Browser, Page, BrowserContext } from '@playwright/test';
import { expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

// タイムアウトを60秒に設定
setDefaultTimeout(60000);

// ブラウザ、コンテキスト、ページのインスタンスを保持
let browser: Browser;
let context: BrowserContext;
let page: Page;
let loginPage: LoginPage;

// 各シナリオの前に実行
Before(async function () {
  // ブラウザを起動
  browser = await chromium.launch({
    headless: true, // CIで実行する場合はtrue、デバッグ時はfalse
  });
  
  // コンテキストを作成
  context = await browser.newContext();
  
  // ページを作成
  page = await context.newPage();
  
  // LoginPageインスタンスを作成
  loginPage = new LoginPage(page);
});

// 各シナリオの後に実行
After(async function () {
  // ページを閉じる
  if (page) {
    await page.close();
  }
  
  // コンテキストを閉じる
  if (context) {
    await context.close();
  }
  
  // ブラウザを閉じる
  if (browser) {
    await browser.close();
  }
});

// 背景: ログインページを表示している
Given('ログインページを表示している', async function () {
  await loginPage.ログインページへ移動();
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
  await loginPage.ログインボタンクリック();
});

// ページ遷移の確認
Then('アカウントページにリダイレクトされる', async function () {
  await loginPage.ログイン成功待機('/account');
  const currentUrl = loginPage.URL取得();
  expect(currentUrl).toContain('/account');
});

// エラーメッセージの確認
Then('エラーメッセージが表示される', async function () {
  // エラーメッセージが表示されるまで少し待機
  await page.waitForTimeout(1000);
  const isVisible = await loginPage.エラーメッセージ表示確認();
  expect(isVisible).toBeTruthy();
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
