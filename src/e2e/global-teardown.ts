import { testDatabase } from './setup/database';
import { exec } from 'child_process';
import { promisify } from 'util';
import * as path from 'path';

const execAsync = promisify(exec);

/**
 * Playwright用のグローバルクリーンアップ
 * すべてのテスト実行後に一度だけ実行される
 */
export default async function globalTeardown() {
  console.log('\n========================================');
  console.log('Playwright E2E Test Suite - Cleanup');
  console.log('========================================\n');
  
  // バックエンドサービスを停止
  console.log('Stopping backend services...');
  const scriptPath = path.join(__dirname, 'scripts/manage-services.sh');
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
}
