import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

/**
 * ログインページのページオブジェクトクラス
 * ログインページの要素と操作を定義
 */
export class LoginPage extends BasePage {
  // ロケーター
  private readonly userIdInput: Locator;
  private readonly passwordInput: Locator;
  private readonly submitButton: Locator;
  private readonly errorMessage: Locator;
  private readonly pageTitle: Locator;
  private readonly testUserInfo: Locator;

  constructor(page: Page) {
    super(page);
    
    // 要素の初期化
    this.userIdInput = page.locator('#userId');
    this.passwordInput = page.locator('#password');
    this.submitButton = page.locator('button[type="submit"]');
    this.errorMessage = page.locator('.bg-red-100');
    this.pageTitle = page.locator('h1');
    this.testUserInfo = page.locator('.bg-gray-50');
  }

  /**
   * ログインページに移動する
   * @param baseUrl ベースURL（デフォルト: http://localhost:3000）
   */
  async ログインページへ移動(baseUrl: string = 'http://localhost:3000'): Promise<void> {
    await this.ページ遷移(`${baseUrl}/`);
  }

  /**
   * ユーザーIDを入力する
   * @param userId ユーザーID
   */
  async ユーザーID入力(userId: string): Promise<void> {
    await this.入力(this.userIdInput, userId);
  }

  /**
   * パスワードを入力する
   * @param password パスワード
   */
  async パスワード入力(password: string): Promise<void> {
    await this.入力(this.passwordInput, password);
  }

  /**
   * ログインボタンをクリックする
   */
  async ログインボタンクリック(): Promise<void> {
    await this.クリック(this.submitButton);
  }

  /**
   * ログイン処理を実行する
   * @param userId ユーザーID
   * @param password パスワード
   */
  async ログイン実行(userId: string, password: string): Promise<void> {
    await this.ユーザーID入力(userId);
    await this.パスワード入力(password);
    await this.ログインボタンクリック();
  }

  /**
   * エラーメッセージを取得する
   * @returns エラーメッセージのテキスト
   */
  async エラーメッセージ取得(): Promise<string> {
    return await this.テキスト取得(this.errorMessage);
  }

  /**
   * エラーメッセージが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async エラーメッセージ表示確認(): Promise<boolean> {
    return await this.表示確認(this.errorMessage);
  }

  /**
   * ページタイトルのテキストを取得する
   * @returns ページタイトル
   */
  async ページタイトル取得(): Promise<string> {
    return await this.テキスト取得(this.pageTitle);
  }

  /**
   * ログインボタンが有効かどうかを確認する
   * @returns 有効な場合はtrue
   */
  async ログインボタン有効確認(): Promise<boolean> {
    return await this.有効確認(this.submitButton);
  }

  /**
   * ログインボタンのテキストを取得する
   * @returns ボタンのテキスト（例: "ログイン", "ログイン中..."）
   */
  async ログインボタンテキスト取得(): Promise<string> {
    return await this.テキスト取得(this.submitButton);
  }

  /**
   * テストユーザー情報が表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async テストユーザー情報表示確認(): Promise<boolean> {
    return await this.表示確認(this.testUserInfo);
  }

  /**
   * ユーザーID入力フィールドが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ユーザーID入力欄表示確認(): Promise<boolean> {
    return await this.表示確認(this.userIdInput);
  }

  /**
   * パスワード入力フィールドが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async パスワード入力欄表示確認(): Promise<boolean> {
    return await this.表示確認(this.passwordInput);
  }

  /**
   * ユーザーID入力フィールドの値を取得する
   * @returns 入力されているユーザーID
   */
  async ユーザーID入力値取得(): Promise<string> {
    return await this.userIdInput.inputValue();
  }

  /**
   * パスワード入力フィールドの値を取得する
   * @returns 入力されているパスワード
   */
  async パスワード入力値取得(): Promise<string> {
    return await this.passwordInput.inputValue();
  }

  /**
   * フォームをクリアする
   */
  async フォームクリア(): Promise<void> {
    await this.userIdInput.clear();
    await this.passwordInput.clear();
  }

  /**
   * ログインが成功してリダイレクトされるのを待機する
   * @param expectedUrl リダイレクト先のURL（デフォルト: /account）
   * @param timeout タイムアウト時間（ミリ秒、デフォルト: 15000）
   */
  async ログイン成功待機(expectedUrl: string = '/account', timeout: number = 15000): Promise<void> {
    // SPAアプリなので、waitUntilオプションなしでURL変更のみを待つ
    await this.page.waitForURL(`**${expectedUrl}`, { timeout });
  }
}
