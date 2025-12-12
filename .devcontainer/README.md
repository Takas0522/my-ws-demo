# DevContainer Database Setup

このDevContainerは起動時に自動的にPostgreSQLデータベースを初期化し、サンプルテーブルとシードデータを作成します。

## 自動初期化される内容

### テーブル構造
- **sample** テーブル
  - `id` (SERIAL PRIMARY KEY)
  - `name` (VARCHAR(100) NOT NULL)
  - `description` (TEXT)
  - `created_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

### シードデータ
5件のサンプルデータが自動的に挿入されます。

## データベース接続情報

- **ホスト**: localhost
- **ポート**: 5432
- **データベース名**: postgres
- **ユーザー名**: postgres
- **パスワード**: postgres

## データベースへの接続方法

### VS Code拡張機能を使用
PostgreSQL Client拡張機能がインストールされています。左側のデータベースアイコンから接続できます。

### コマンドラインから
```bash
psql -h localhost -U postgres -d postgres
```

### データの確認
```sql
SELECT * FROM sample;
```

## スクリプトファイル

- `init-db.sql`: データベース初期化スクリプト（テーブル作成とシードデータ挿入）

## 注意事項

- これらのスクリプトは開発用のみです
- データベースボリュームを削除して再構築すると、初期化スクリプトが再実行されます
- 本番環境では使用しないでください
