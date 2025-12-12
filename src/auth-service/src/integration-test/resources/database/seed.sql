-- 認証サービスのSeedデータ
\c auth_service_db;

-- 認証情報データの挿入 (パスワードは "password123" のハッシュ値)
-- BCryptでハッシュ化されたパスワード
INSERT INTO user_credentials (user_id, password_hash) VALUES
    ('05c66ceb-6ddc-4ada-b736-08702615ff48', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- tanaka_taro
    ('4f4777e4-dd9c-4d5b-a928-19a59b1d3ead', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- suzuki_hanako
    ('7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- yamada_jiro
    ('233c99d5-41ba-42f3-89fa-eb34644fe3b5', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'), -- sato_yuki
    ('8a17f2c2-c1c8-4fee-ae95-8a483127bf1f', '$2a$10$OSuuVFLoafV6AKzzptdQSeGXQIqx0rU53gtZvYwZ07Des/5txWC6q'); -- takahashi_mai

-- サンプルセッショントークンの挿入
INSERT INTO session_tokens (user_id, token, expires_at) VALUES
    ('05c66ceb-6ddc-4ada-b736-08702615ff48', 'token_tanaka_123456', NOW() + INTERVAL '7 days'),
    ('4f4777e4-dd9c-4d5b-a928-19a59b1d3ead', 'token_suzuki_234567', NOW() + INTERVAL '7 days');

-- ログイン履歴の挿入
INSERT INTO login_history (user_id, ip_address, user_agent, success) VALUES
    ('05c66ceb-6ddc-4ada-b736-08702615ff48', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', TRUE),
    ('05c66ceb-6ddc-4ada-b736-08702615ff48', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', TRUE),
    ('4f4777e4-dd9c-4d5b-a928-19a59b1d3ead', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', TRUE),
    ('7bd6e35b-9c8e-4635-a47d-f7adce5c8ed9', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64)', TRUE),
    ('233c99d5-41ba-42f3-89fa-eb34644fe3b5', '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', TRUE),
    ('05c66ceb-6ddc-4ada-b736-08702615ff48', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', FALSE);

SELECT 'Auth Service Seed data inserted successfully' AS status;
SELECT COUNT(*) AS credentials_count FROM user_credentials;
SELECT COUNT(*) AS sessions_count FROM session_tokens;
