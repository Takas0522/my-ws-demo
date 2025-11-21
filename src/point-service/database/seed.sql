-- ポイントサービスのSeedデータ
\c point_service_db;

-- User Service/Auth Serviceと同じUUID使用
INSERT INTO points (user_id, balance, last_updated) VALUES
('123e4567-e89b-12d3-a456-426614174000', 1500, NOW()),
('123e4567-e89b-12d3-a456-426614174001', 3200, NOW()),
('123e4567-e89b-12d3-a456-426614174002', 500, NOW()),
('123e4567-e89b-12d3-a456-426614174003', 2100, NOW()),
('123e4567-e89b-12d3-a456-426614174004', 750, NOW());

INSERT INTO point_history (user_id, amount, transaction_type, description, created_at, expires_at) VALUES
('123e4567-e89b-12d3-a456-426614174000', 1000, 'EARN', '新規登録ボーナス', NOW() - INTERVAL '30 days', NOW() + INTERVAL '150 days'),
('123e4567-e89b-12d3-a456-426614174000', 500, 'EARN', '購入特典', NOW() - INTERVAL '10 days', NOW() + INTERVAL '170 days'),
('123e4567-e89b-12d3-a456-426614174001', 2000, 'EARN', '新規登録ボーナス', NOW() - INTERVAL '60 days', NOW() + INTERVAL '120 days'),
('123e4567-e89b-12d3-a456-426614174001', 1500, 'EARN', 'キャンペーン特典', NOW() - INTERVAL '20 days', NOW() + INTERVAL '160 days'),
('123e4567-e89b-12d3-a456-426614174001', 300, 'USE', '商品購入', NOW() - INTERVAL '5 days', NULL),
('123e4567-e89b-12d3-a456-426614174002', 500, 'EARN', '新規登録ボーナス', NOW() - INTERVAL '90 days', NOW() + INTERVAL '90 days'),
('123e4567-e89b-12d3-a456-426614174003', 2000, 'EARN', '新規登録ボーナス', NOW() - INTERVAL '45 days', NOW() + INTERVAL '135 days'),
('123e4567-e89b-12d3-a456-426614174003', 100, 'EARN', 'レビュー投稿', NOW() - INTERVAL '15 days', NOW() + INTERVAL '165 days'),
('123e4567-e89b-12d3-a456-426614174004', 1000, 'EARN', '新規登録ボーナス', NOW() - INTERVAL '120 days', NOW() + INTERVAL '60 days'),
('123e4567-e89b-12d3-a456-426614174004', 250, 'USE', '商品購入', NOW() - INTERVAL '10 days', NULL);

SELECT 'Point Service Seed data inserted successfully' AS status;
SELECT COUNT(*) AS points_count FROM points;
SELECT COUNT(*) AS history_count FROM point_history;
