-- 認証サービスのSeedデータ
\c auth_service_db;

-- 認証情報データの挿入 (パスワードは "password123" のハッシュ値)
-- BCryptでハッシュ化されたパスワード
INSERT INTO user_credentials (user_id, password_hash) VALUES
    ('123e4567-e89b-12d3-a456-426614174000', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- tanaka_taro
    ('123e4567-e89b-12d3-a456-426614174001', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- suzuki_hanako
    ('123e4567-e89b-12d3-a456-426614174002', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- yamada_jiro
    ('123e4567-e89b-12d3-a456-426614174003', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- sato_yuki
    ('123e4567-e89b-12d3-a456-426614174004', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'); -- takahashi_mai

-- サンプルセッショントークンの挿入
INSERT INTO session_tokens (user_id, token, expires_at) VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 'token_tanaka_123456', NOW() + INTERVAL '7 days'),
    ('123e4567-e89b-12d3-a456-426614174001', 'token_suzuki_234567', NOW() + INTERVAL '7 days');

-- ログイン履歴の挿入
INSERT INTO login_history (user_id, ip_address, user_agent, success) VALUES
    ('123e4567-e89b-12d3-a456-426614174000', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', TRUE),
    ('123e4567-e89b-12d3-a456-426614174000', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', TRUE),
    ('123e4567-e89b-12d3-a456-426614174001', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', TRUE),
    ('123e4567-e89b-12d3-a456-426614174002', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64)', TRUE),
    ('123e4567-e89b-12d3-a456-426614174003', '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', TRUE),
    ('123e4567-e89b-12d3-a456-426614174000', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', FALSE);

SELECT 'Auth Service Seed data inserted successfully' AS status;
SELECT COUNT(*) AS credentials_count FROM user_credentials;
SELECT COUNT(*) AS sessions_count FROM session_tokens;
