import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

/**
 * ポイント詳細ページのページオブジェクトクラス
 * ポイント残高詳細表示ページの要素と操作を定義
 */
export class PointsPage extends BasePage {
  // ロケーター
  private readonly pageTitle: Locator;
  private readonly currentBalance: Locator;
  private readonly lastUpdated: Locator;
  private readonly pointHistoryButton: Locator;
  private readonly backButton: Locator;
  private readonly accountButton: Locator;
  private readonly logoutButton: Locator;

  constructor(page: Page) {
    super(page);
    
    // 要素の初期化
    this.pageTitle = page.locator('h1');
    this.currentBalance = page.locator('[data-testid="current-balance"]');
    this.lastUpdated = page.locator('[data-testid="last-updated"]').or(page.locator('text=/最終更新/'));
    // '/point-history' または '/points/history' の両方を許容
    this.pointHistoryButton = page.locator('a[href*="history"]').or(page.getByRole('link', { name: /ポイント履歴|履歴/ }));
    this.backButton = page.locator('button:has-text("戻る")').or(page.locator('a:has-text("戻る")'));
    // ポイントページでは「戻る」リンクがアカウントページに戻る機能
    this.accountButton = page.locator('a:has-text("戻る")').first();
    this.logoutButton = page.locator('button:has-text("ログアウト")');
  }

  /**
   * ポイントページに直接アクセスする
   * @param baseUrl ベースURL（デフォルト: http://localhost:3000）
   */
  async ポイントページへ移動(baseUrl: string = 'http://localhost:3000'): Promise<void> {
    await this.ページ遷移(`${baseUrl}/points`);
    // SPAの描画を待つ
    await this.page.waitForTimeout(1000);
  }

  /**
   * ページタイトルのテキストを取得する
   * @returns ページタイトル
   */
  async ページタイトル取得(): Promise<string> {
    await this.要素表示待機(this.pageTitle);
    return await this.テキスト取得(this.pageTitle);
  }

  /**
   * 現在のポイント残高を取得する
   * @returns ポイント残高（数値文字列）
   */
  async 現在のポイント残高取得(): Promise<string> {
    await this.要素表示待機(this.currentBalance);
    const text = await this.テキスト取得(this.currentBalance);
    
    // "1,500" や "1500 ポイント" から数値部分だけを抽出（カンマを除去）
    const cleanText = text.replace(/,/g, ''); // カンマを除去
    const match = cleanText.match(/(\d+)/);
    return match ? match[1] : '0';
  }

  /**
   * 現在のポイント残高が表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async 現在のポイント残高表示確認(): Promise<boolean> {
    return await this.表示確認(this.currentBalance);
  }

  /**
   * 最終更新日時が表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async 最終更新日時表示確認(): Promise<boolean> {
    return await this.表示確認(this.lastUpdated);
  }

  /**
   * ポイント履歴ボタンが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ポイント履歴ボタン表示確認(): Promise<boolean> {
    return await this.表示確認(this.pointHistoryButton);
  }

  /**
   * 戻るボタンが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async 戻るボタン表示確認(): Promise<boolean> {
    return await this.表示確認(this.backButton);
  }

  /**
   * ログアウトボタンが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ログアウトボタン表示確認(): Promise<boolean> {
    return await this.表示確認(this.logoutButton);
  }

  /**
   * ポイント履歴ボタンをクリックする
   */
  async ポイント履歴ボタンクリック(): Promise<void> {
    await this.クリック(this.pointHistoryButton);
    // ポイント履歴ページへの遷移を待つ
    await this.page.waitForURL('**/points/history', { timeout: 10000 });
    // SPAの描画を待つ
    await this.page.waitForTimeout(500);
  }

  /**
   * 戻るボタンをクリックする
   */
  async 戻るボタンクリック(): Promise<void> {
    await this.クリック(this.backButton);
    // アカウントページへの遷移を待つ
    await this.page.waitForURL('**/account', { timeout: 10000 });
    // SPAの描画を待つ
    await this.page.waitForTimeout(500);
  }

  /**
   * アカウントボタンをクリックする
   */
  async アカウントボタンクリック(): Promise<void> {
    await this.クリック(this.accountButton);
    // SPAの描画を待つ
    await this.page.waitForTimeout(500);
  }

  /**
   * ログアウトボタンをクリックする
   */
  async ログアウトボタンクリック(): Promise<void> {
    await this.クリック(this.logoutButton);
    // ログインページへのリダイレクトを待つ
    await this.page.waitForURL(/\/login|^\/$/, { timeout: 10000 });
    // localStorageがクリアされることを待つ
    await this.page.waitForTimeout(500);
  }

  /**
   * localStorageのトークンをクリアする
   */
  async トークンクリア(): Promise<void> {
    await this.page.evaluate(() => {
      localStorage.removeItem('authToken');
    });
  }

  /**
   * localStorageのトークンをクリアする（テスト用メソッド名）
   */
  async ローカルストレージトークンクリア(): Promise<void> {
    await this.トークンクリア();
  }
}
