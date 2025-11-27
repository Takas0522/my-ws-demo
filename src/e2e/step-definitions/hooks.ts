import { BeforeAll, AfterAll, Before, After, setDefaultTimeout } from '@cucumber/cucumber';
import { chromium, Browser, Page, BrowserContext } from '@playwright/test';
import { testDatabase } from '../setup/database';
import { exec } from 'child_process';
import { promisify } from 'util';
import * as path from 'path';

const execAsync = promisify(exec);

// タイムアウトを60秒に設定
setDefaultTimeout(60000);

// グローバル変数（全シナリオで共有）
let browser: Browser;
let context: BrowserContext;
let page: Page;

/**
 * すべてのテスト実行前に一度だけ実行
 * TestContainersを起動してテスト用DBをセットアップし、バックエンドサービスを起動
 */
BeforeAll({ timeout: 300000 }, async function () {
  console.log('\n========================================');
  console.log('E2E Test Suite - Setup');
  console.log('========================================\n');
  
  // 既存のサービスを停止
  console.log('Stopping existing services...');
  const scriptPath = path.join(__dirname, '../scripts/manage-services.sh');
  try {
    await execAsync(`${scriptPath} stop`, { timeout: 30000 });
    console.log('✓ Existing services stopped');
  } catch (error) {
    console.log('⚠ No existing services to stop or stop failed');
  }
  
  // TestContainersを起動してデータベースをセットアップ
  console.log('\nStarting TestContainers...');
  await testDatabase.startAll();
  
  // バックエンドサービスをE2E用の環境変数で起動（バックグラウンド実行）
  console.log('\nStarting backend services with E2E database configuration...');
  try {
    // バックグラウンドで起動し、完了を待たずに進む
    exec(`${scriptPath} start-e2e > /tmp/e2e-service-startup.log 2>&1`, (error) => {
      if (error) {
        console.error('Service startup error:', error);
      }
    });
    
    // サービスが起動するまで待機（簡易チェック）
    console.log('Waiting for services to start...');
    await new Promise(resolve => setTimeout(resolve, 60000)); // 60秒待機
    
    // サービスの起動確認
    const servicesReady = await checkServicesReady();
    if (!servicesReady) {
      console.warn('⚠ Some services may not be ready yet, but continuing...');
    } else {
      console.log('✓ All backend services are ready');
    }
  } catch (error: any) {
    console.error('Failed to start backend services:', error.message);
    throw error;
  }
  
  console.log('\n========================================');
  console.log('Setup Complete - Ready for E2E Tests');
  console.log('========================================\n');
});

/**
 * サービスの起動確認
 */
async function checkServicesReady(): Promise<boolean> {
  const services = [
    { name: 'user-service', url: 'http://localhost:8080' },
    { name: 'auth-service', url: 'http://localhost:8081' },
    { name: 'point-service', url: 'http://localhost:8082' },
    { name: 'bff', url: 'http://localhost:8090' },
    { name: 'frontend', url: 'http://localhost:3000' }
  ];

  for (const service of services) {
    try {
      await execAsync(`curl -s -o /dev/null -w "%{http_code}" ${service.url}`, { timeout: 5000 });
      console.log(`  ✓ ${service.name} is ready`);
    } catch (error) {
      console.log(`  ✗ ${service.name} is not ready`);
      return false;
    }
  }
  
  return true;
}

/**
 * すべてのテスト実行後に一度だけ実行
 * バックエンドサービスとTestContainersを停止
 */
AfterAll(async function () {
  console.log('\n========================================');
  console.log('E2E Test Suite - Cleanup');
  console.log('========================================\n');
  
  // バックエンドサービスを停止
  console.log('Stopping backend services...');
  const scriptPath = path.join(__dirname, '../scripts/manage-services.sh');
  try {
    await execAsync(`${scriptPath} stop`);
    console.log('✓ Backend services stopped');
  } catch (error: any) {
    console.error('Failed to stop backend services:', error.message);
  }
  
  // TestContainersを停止
  console.log('\nStopping TestContainers...');
  await testDatabase.stopAll();
  
  console.log('\n========================================');
  console.log('Cleanup Complete');
  console.log('========================================\n');
});

/**
 * 各シナリオの前に実行
 * ブラウザを起動し、データベースをリセット
 */
Before(async function () {
  // ブラウザを起動
  browser = await chromium.launch({
    headless: true, // CIで実行する場合はtrue、デバッグ時はfalse
  });
  
  // コンテキストを作成
  context = await browser.newContext();
  
  // ページを作成
  page = await context.newPage();
  
  // このページインスタンスをWorldオブジェクトに保存（ステップ定義で使用可能にする）
  this.page = page;
  this.context = context;
  this.browser = browser;
  
  // データベースをクリーンな状態にリセット
  await testDatabase.resetAll();
});

/**
 * 各シナリオの後に実行
 * ブラウザを閉じる
 */
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

// ブラウザインスタンスをエクスポート（ステップ定義で使用）
export { browser, context, page };
