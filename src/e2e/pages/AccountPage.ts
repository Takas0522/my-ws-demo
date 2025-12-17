import { Page, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

/**
 * アカウントページのページオブジェクトクラス
 * アカウント情報表示ページの要素と操作を定義
 */
export class AccountPage extends BasePage {
  // ロケーター
  private readonly pageTitle: Locator;
  private readonly userName: Locator;
  private readonly userEmail: Locator;
  private readonly userId: Locator;
  private readonly pointBalance: Locator;
  private readonly pointDetailButton: Locator;
  private readonly logoutButton: Locator;

  constructor(page: Page) {
    super(page);
    
    // 要素の初期化
    this.pageTitle = page.locator('h1');
    this.userName = page.locator('[data-testid="user-name"]').or(page.locator('p:has-text("氏名")').locator('..').locator('p').nth(1));
    this.userEmail = page.locator('[data-testid="user-email"]').or(page.locator('p:has-text("メールアドレス")').locator('..').locator('p').nth(1));
    this.userId = page.locator('[data-testid="user-id"]').or(page.locator('p:has-text("ユーザーID")').locator('..').locator('p').nth(1));
    this.pointBalance = page.locator('[data-testid="point-balance"]');
    this.pointDetailButton = page.locator('a[href="/points"]').or(page.getByRole('link', { name: /ポイント詳細|ポイント/ }));
    this.logoutButton = page.locator('button:has-text("ログアウト")');
  }

  /**
   * アカウントページに直接アクセスする
   * @param baseUrl ベースURL（デフォルト: http://localhost:3000）
   */
  async アカウントページへ移動(baseUrl: string = 'http://localhost:3000'): Promise<void> {
    await this.ページ遷移(`${baseUrl}/account`);
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
   * ユーザー名を取得する
   * @returns ユーザー名
   */
  async ユーザー名取得(): Promise<string> {
    await this.要素表示待機(this.userName);
    const text = await this.テキスト取得(this.userName);
    // "氏名: 田中太郎" のような形式から名前だけを抽出
    return text.replace(/^氏名[:：]\s*/, '').trim();
  }

  /**
   * メールアドレスを取得する
   * @returns メールアドレス
   */
  async メールアドレス取得(): Promise<string> {
    await this.要素表示待機(this.userEmail);
    const text = await this.テキスト取得(this.userEmail);
    // "メールアドレス: xxx@example.com" のような形式からメールアドレスだけを抽出
    return text.replace(/^メールアドレス[:：]\s*/, '').trim();
  }

  /**
   * ユーザーIDを取得する
   * @returns ユーザーID
   */
  async ユーザーID取得(): Promise<string> {
    await this.要素表示待機(this.userId);
    const text = await this.テキスト取得(this.userId);
    // "ユーザーID: xxx" のような形式からIDだけを抽出
    return text.replace(/^ユーザーID[:：]\s*/, '').trim();
  }

  /**
   * ポイント残高を取得する
   * @returns ポイント残高（数値文字列）
   */
  async ポイント残高取得(): Promise<string> {
    await this.要素表示待機(this.pointBalance);
    const text = await this.テキスト取得(this.pointBalance);
    // "1500 ポイント" や "1500ポイント" から数値部分だけを抽出
    const match = text.match(/(\d+)/);
    return match ? match[1] : '0';
  }

  /**
   * ユーザー名が表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ユーザー名表示確認(): Promise<boolean> {
    return await this.表示確認(this.userName);
  }

  /**
   * メールアドレスが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async メールアドレス表示確認(): Promise<boolean> {
    return await this.表示確認(this.userEmail);
  }

  /**
   * ポイント残高が表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ポイント残高表示確認(): Promise<boolean> {
    return await this.表示確認(this.pointBalance);
  }

  /**
   * ポイント詳細ボタンが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ポイント詳細ボタン表示確認(): Promise<boolean> {
    return await this.表示確認(this.pointDetailButton);
  }

  /**
   * ログアウトボタンが表示されているかを確認する
   * @returns 表示されている場合はtrue
   */
  async ログアウトボタン表示確認(): Promise<boolean> {
    return await this.表示確認(this.logoutButton);
  }

  /**
   * ポイント詳細ボタンをクリックする
   */
  async ポイント詳細ボタンクリック(): Promise<void> {
    await this.クリック(this.pointDetailButton);
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

  /**
   * localStorageのトークンを取得する
   * @returns トークン文字列またはnull
   */
  async ローカルストレージトークン取得(): Promise<string | null> {
    return await this.page.evaluate(() => localStorage.getItem('authToken'));
  }

  /**
   * localStorageにトークンが保存されているか確認する
   * @returns トークンが存在する場合はtrue
   */
  async トークン存在確認(): Promise<boolean> {
    const token = await this.page.evaluate(() => localStorage.getItem('authToken'));
    return token !== null && token !== '';
  }
}
