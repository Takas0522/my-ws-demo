import { PostgreSqlContainer, StartedPostgreSqlContainer } from '@testcontainers/postgresql';
import { Client } from 'pg';
import * as fs from 'fs';
import * as path from 'path';

/**
 * E2Eテスト用のデータベース管理クラス
 */
export class TestDatabase {
  private containers: Map<string, StartedPostgreSqlContainer> = new Map();
  private clients: Map<string, Client> = new Map();

  /**
   * 3つのサービス用にPostgreSQLコンテナを起動
   */
  async startAll(): Promise<void> {
    console.log('Starting TestContainers for E2E tests...');
    
    const services = [
      { name: 'user-service', dbName: 'user_service_db' },
      { name: 'auth-service', dbName: 'auth_service_db' },
      { name: 'point-service', dbName: 'point_service_db' }
    ];

    // 並列で各サービスのDBコンテナを起動
    await Promise.all(
      services.map(async (service) => {
        const container = await new PostgreSqlContainer('postgres:16-alpine')
          .withDatabase(service.dbName)
          .withUsername('testuser')
          .withPassword('testpass')
          .withExposedPorts(5432)
          .start();

        this.containers.set(service.name, container);
        console.log(`✓ ${service.name} DB started on port ${container.getMappedPort(5432)}`);

        // クライアント接続
        const client = new Client({
          host: container.getHost(),
          port: container.getMappedPort(5432),
          database: service.dbName,
          user: 'testuser',
          password: 'testpass',
        });

        await client.connect();
        this.clients.set(service.name, client);

        // スキーマとシードデータを投入
        await this.initializeDatabase(service.name, client);
      })
    );

    console.log('All test databases are ready!');
    console.log('Database connection info:');
    this.logConnectionInfo();
    
    // E2Eテスト用の.envファイルを生成
    console.log('\nGenerating .env.e2e files for services...');
    this.generateEnvFiles();
  }

  /**
   * データベースにスキーマとシードデータを投入
   */
  private async initializeDatabase(serviceName: string, client: Client): Promise<void> {
    const serviceDir = path.join(__dirname, '../../', serviceName, 'database');
    const schemaPath = path.join(serviceDir, 'schema.sql');
    const seedPath = path.join(serviceDir, 'seed.sql');

    // スキーマ読み込みと実行
    if (fs.existsSync(schemaPath)) {
      const schemaSql = fs.readFileSync(schemaPath, 'utf8');
      // \c コマンドを削除（TestContainersでは不要）
      const cleanedSchema = schemaSql.replace(/\\c\s+\w+;/g, '');
      await client.query(cleanedSchema);
      console.log(`  ✓ Schema loaded for ${serviceName}`);
    }

    // シードデータ読み込みと実行
    if (fs.existsSync(seedPath)) {
      const seedSql = fs.readFileSync(seedPath, 'utf8');
      // \c コマンドを削除
      const cleanedSeed = seedSql.replace(/\\c\s+\w+;/g, '');
      await client.query(cleanedSeed);
      console.log(`  ✓ Seed data loaded for ${serviceName}`);
    }
  }

  /**
   * 各テストの前にデータベースをクリアして再度シードデータを投入
   */
  async resetAll(): Promise<void> {
    console.log('Resetting all test databases...');

    const services = ['user-service', 'auth-service', 'point-service'];
    
    await Promise.all(
      services.map(async (serviceName) => {
        const client = this.clients.get(serviceName);
        if (!client) return;

        // テーブルをクリア
        await this.clearDatabase(serviceName, client);

        // シードデータを再投入
        const serviceDir = path.join(__dirname, '../../', serviceName, 'database');
        const seedPath = path.join(serviceDir, 'seed.sql');

        if (fs.existsSync(seedPath)) {
          const seedSql = fs.readFileSync(seedPath, 'utf8');
          const cleanedSeed = seedSql.replace(/\\c\s+\w+;/g, '');
          await client.query(cleanedSeed);
        }
      })
    );

    console.log('✓ All databases reset successfully');
  }

  /**
   * データベースのテーブルをクリア
   */
  private async clearDatabase(serviceName: string, client: Client): Promise<void> {
    try {
      // サービスごとにテーブルをクリア
      if (serviceName === 'user-service') {
        await client.query('TRUNCATE TABLE users CASCADE');
      } else if (serviceName === 'auth-service') {
        await client.query('TRUNCATE TABLE user_credentials, session_tokens, login_history CASCADE');
      } else if (serviceName === 'point-service') {
        await client.query('TRUNCATE TABLE points, point_history CASCADE');
      }
    } catch (error) {
      console.error(`Error clearing ${serviceName} database:`, error);
      throw error;
    }
  }

  /**
   * 全てのコンテナを停止
   */
  async stopAll(): Promise<void> {
    console.log('Stopping all test databases...');

    // クライアント接続を閉じる
    for (const [name, client] of this.clients.entries()) {
      await client.end();
      console.log(`✓ ${name} client disconnected`);
    }

    // コンテナを停止
    for (const [name, container] of this.containers.entries()) {
      await container.stop();
      console.log(`✓ ${name} container stopped`);
    }

    this.clients.clear();
    this.containers.clear();
    console.log('All test databases stopped');
  }

  /**
   * 特定のサービスのDB接続情報を取得
   */
  getConnectionInfo(serviceName: string): { host: string; port: number; database: string } | null {
    const container = this.containers.get(serviceName);
    if (!container) return null;

    const dbNames: Record<string, string> = {
      'user-service': 'user_service_db',
      'auth-service': 'auth_service_db',
      'point-service': 'point_service_db'
    };

    return {
      host: container.getHost(),
      port: container.getMappedPort(5432),
      database: dbNames[serviceName] || serviceName
    };
  }

  /**
   * 接続情報をログ出力
   */
  private logConnectionInfo(): void {
    const services = ['user-service', 'auth-service', 'point-service'];
    services.forEach((serviceName) => {
      const info = this.getConnectionInfo(serviceName);
      if (info) {
        console.log(`  ${serviceName}: postgresql://testuser:testpass@${info.host}:${info.port}/${info.database}`);
      }
    });
  }

  /**
   * E2Eテスト用の.envファイルを生成
   * 各サービスディレクトリに.env.e2eファイルを作成
   */
  generateEnvFiles(): void {
    const serviceConfigs = [
      { 
        name: 'user-service', 
        envPrefix: 'DB_USER_SERVICE',
        dbName: 'user_service_db',
        path: path.join(__dirname, '../../user-service/.env.e2e')
      },
      { 
        name: 'auth-service', 
        envPrefix: 'DB_AUTH_SERVICE',
        dbName: 'auth_service_db',
        path: path.join(__dirname, '../../auth-service/.env.e2e')
      },
      { 
        name: 'point-service', 
        envPrefix: 'DB_POINT_SERVICE',
        dbName: 'point_service_db',
        path: path.join(__dirname, '../../point-service/.env.e2e')
      }
    ];

    serviceConfigs.forEach((config) => {
      const container = this.containers.get(config.name);
      if (!container) return;

      const envContent = `# E2E Test Database Configuration (Generated by TestContainers)
# DO NOT EDIT MANUALLY - This file is automatically generated

${config.envPrefix}_HOST=${container.getHost()}
${config.envPrefix}_PORT=${container.getMappedPort(5432)}
${config.envPrefix}_NAME=${config.dbName}
${config.envPrefix}_USER=testuser
${config.envPrefix}_PASSWORD=testpass
`;

      fs.writeFileSync(config.path, envContent, 'utf8');
      console.log(`  ✓ Generated ${config.path}`);
    });
  }

  /**
   * 特定のサービスのクライアントを取得
   */
  getClient(serviceName: string): Client | null {
    return this.clients.get(serviceName) || null;
  }
}

// グローバルインスタンス（Cucumberのhooksで使用）
export const testDatabase = new TestDatabase();
