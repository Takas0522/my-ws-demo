import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

// LoginPageインスタンスを保持
let loginPage: LoginPage;

// 背景: ログインページを表示している
Given('ログインページを表示している', async function () {
  // hooksからページインスタンスを取得
  loginPage = new LoginPage(this.page);
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
  await this.page.waitForTimeout(1000);
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
