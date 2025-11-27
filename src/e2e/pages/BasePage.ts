import { Page, Locator } from '@playwright/test';

/**
 * ベースページオブジェクトクラス
 * 全てのページオブジェクトで共通する機能を提供
 */
export class BasePage {
  protected page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * 指定されたURLに移動する
   * @param url 移動先のURL
   */
  async ページ遷移(url: string): Promise<void> {
    await this.page.goto(url);
  }

  /**
   * 現在のページタイトルを取得する
   * @returns ページタイトル
   */
  async タイトル取得(): Promise<string> {
    return await this.page.title();
  }

  /**
   * 現在のURLを取得する
   * @returns 現在のURL
   */
  URL取得(): string {
    return this.page.url();
  }

  /**
   * 要素が表示されるまで待機する
   * @param locator 待機する要素のロケーター
   * @param timeout タイムアウト時間（ミリ秒）
   */
  async 要素表示待機(locator: Locator, timeout: number = 5000): Promise<void> {
    await locator.waitFor({ state: 'visible', timeout });
  }

  /**
   * テキストを入力する
   * @param locator 入力フィールドのロケーター
   * @param text 入力するテキスト
   */
  async 入力(locator: Locator, text: string): Promise<void> {
    await locator.fill(text);
  }

  /**
   * 要素をクリックする
   * @param locator クリックする要素のロケーター
   */
  async クリック(locator: Locator): Promise<void> {
    await locator.click();
  }

  /**
   * 要素のテキストを取得する
   * @param locator テキストを取得する要素のロケーター
   * @returns 要素のテキスト
   */
  async テキスト取得(locator: Locator): Promise<string> {
    return await locator.textContent() || '';
  }

  /**
   * 要素が表示されているかを確認する
   * @param locator 確認する要素のロケーター
   * @returns 表示されている場合はtrue
   */
  async 表示確認(locator: Locator): Promise<boolean> {
    return await locator.isVisible();
  }

  /**
   * 要素が有効かどうかを確認する
   * @param locator 確認する要素のロケーター
   * @returns 有効な場合はtrue
   */
  async 有効確認(locator: Locator): Promise<boolean> {
    return await locator.isEnabled();
  }

  /**
   * ページの読み込みが完了するまで待機する
   */
  async 読込完了待機(): Promise<void> {
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * スクリーンショットを取得する
   * @param path スクリーンショットの保存パス
   */
  async スクリーンショット(path: string): Promise<void> {
    await this.page.screenshot({ path });
  }
}
