import { testDatabase } from './setup/database';
import { exec } from 'child_process';
import { promisify } from 'util';
import * as path from 'path';

const execAsync = promisify(exec);

/**
 * Playwright用のグローバルセットアップ
 * すべてのテスト実行前に一度だけ実行される
 */
export default async function globalSetup() {
  console.log('\n========================================');
  console.log('Playwright E2E Test Suite - Setup');
  console.log('========================================\n');
  
  // 既存のサービスを停止
  console.log('Stopping existing services...');
  const scriptPath = path.join(__dirname, 'scripts/manage-services.sh');
  try {
    await execAsync(`${scriptPath} stop`, { timeout: 30000 });
    console.log('✓ Existing services stopped');
  } catch (error) {
    console.log('⚠ No existing services to stop or stop failed');
  }
  
  // TestContainersを起動してデータベースをセットアップ
  console.log('\nStarting TestContainers...');
  await testDatabase.startAll();
  
  // バックエンドサービスをE2E用の環境変数で起動
  console.log('\nStarting backend services with E2E database configuration...');
  try {
    // バックグラウンドで起動
    exec(`${scriptPath} start-e2e > /tmp/e2e-service-startup.log 2>&1`, (error) => {
      if (error) {
        console.error('Service startup error:', error);
      }
    });
    
    // サービスが起動するまで待機
    console.log('Waiting for services to start...');
    const maxRetries = 60; // 最大60回リトライ（約2分）
    const retryInterval = 2000; // 2秒ごとにリトライ
    
    let servicesReady = false;
    for (let i = 0; i < maxRetries; i++) {
      await new Promise(resolve => setTimeout(resolve, retryInterval));
      servicesReady = await checkServicesReady(i === 0);
      
      if (servicesReady) {
        console.log(`✓ All backend services are ready (attempt ${i + 1}/${maxRetries})`);
        break;
      }
      
      if ((i + 1) % 10 === 0) {
        console.log(`  Still waiting... (attempt ${i + 1}/${maxRetries})`);
      }
    }
    
    if (!servicesReady) {
      throw new Error('Services did not start within the expected time');
    }
  } catch (error: any) {
    console.error('Failed to start backend services:', error.message);
    throw error;
  }
  
  console.log('\n========================================');
  console.log('Setup Complete - Ready for E2E Tests');
  console.log('========================================\n');
}

/**
 * サービスの起動確認
 */
async function checkServicesReady(showLogs: boolean = true): Promise<boolean> {
  const services = [
    { name: 'user-service', url: 'http://localhost:8080' },
    { name: 'auth-service', url: 'http://localhost:8081' },
    { name: 'point-service', url: 'http://localhost:8082' },
    { name: 'bff', url: 'http://localhost:8090' },
    { name: 'frontend', url: 'http://localhost:3000' }
  ];

  for (const service of services) {
    try {
      const { stdout } = await execAsync(`curl -s -o /dev/null -w "%{http_code}" ${service.url}`, { timeout: 5000 });
      const statusCode = stdout.trim();
      
      // 200番台または400番台のレスポンスがあればサービスは起動している
      if (!statusCode.match(/^[24]\d{2}$/)) {
        if (showLogs) {
          console.log(`  ✗ ${service.name} returned status ${statusCode}`);
        }
        return false;
      }
      
      if (showLogs) {
        console.log(`  ✓ ${service.name} is ready`);
      }
    } catch (error) {
      if (showLogs) {
        console.log(`  ✗ ${service.name} is not ready`);
      }
      return false;
    }
  }
  
  return true;
}
