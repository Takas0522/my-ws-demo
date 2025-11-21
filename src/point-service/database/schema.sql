-- ポイントサービスのデータベーススキーマ
\c point_service_db;

-- points テーブル
CREATE TABLE IF NOT EXISTS points (
    user_id UUID PRIMARY KEY,
    balance INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT NOW()
);

-- point_history テーブル
CREATE TABLE IF NOT EXISTS point_history (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    amount INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- 'EARN' or 'USE'
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES points(user_id) ON DELETE CASCADE
);

-- インデックス作成
CREATE INDEX idx_point_history_user_id ON point_history(user_id);
CREATE INDEX idx_point_history_created_at ON point_history(created_at);

SELECT 'Point Service Schema created successfully' AS status;
