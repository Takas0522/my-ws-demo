-- ユーザー管理サービスのSeedデータ
\c user_service_db;

-- ユーザーデータの挿入
INSERT INTO users (id, username, email, full_name) VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 'tanaka_taro', 'tanaka.taro@example.com', '田中太郎'),
    ('123e4567-e89b-12d3-a456-426614174001', 'suzuki_hanako', 'suzuki.hanako@example.com', '鈴木花子'),
    ('123e4567-e89b-12d3-a456-426614174002', 'yamada_jiro', 'yamada.jiro@example.com', '山田次郎'),
    ('123e4567-e89b-12d3-a456-426614174003', 'sato_yuki', 'sato.yuki@example.com', '佐藤優希'),
    ('123e4567-e89b-12d3-a456-426614174004', 'takahashi_mai', 'takahashi.mai@example.com', '高橋舞');

SELECT 'User Service Seed data inserted successfully' AS status;
SELECT COUNT(*) AS user_count FROM users;
