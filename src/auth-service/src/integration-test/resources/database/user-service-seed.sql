-- ユーザー管理サービスのSeedデータ
\c user_service_db;

-- ユーザーデータの挿入（UUIDを明示的に指定）
INSERT INTO users (id, username, email, full_name) VALUES
    ('05c66ceb-6ddc-4ada-b736-08702615ff48'::uuid, 'tanaka_taro', 'tanaka.taro@example.com', '田中太郎'),
    ('4f4777e4-dd9c-4d5b-a928-19a59b1d3ead'::uuid, 'suzuki_hanako', 'suzuki.hanako@example.com', '鈴木花子'),
    ('7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9'::uuid, 'yamada_jiro', 'yamada.jiro@example.com', '山田次郎'),
    ('233c99d5-41ba-42f3-89fa-eb34644fe3b5'::uuid, 'sato_yuki', 'sato.yuki@example.com', '佐藤優希'),
    ('8a17f2c2-c1c8-4fee-ae95-8a483127bf1f'::uuid, 'takahashi_mai', 'takahashi.mai@example.com', '高橋舞');

SELECT 'User Service Seed data inserted successfully' AS status;
SELECT COUNT(*) AS user_count FROM users;
