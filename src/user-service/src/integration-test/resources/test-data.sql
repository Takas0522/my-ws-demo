-- Integration Test用のテストデータ
INSERT INTO users (id, username, email, full_name, created_at, updated_at) 
VALUES 
    ('123e4567-e89b-12d3-a456-426614174000', 'testuser1', 'test1@example.com', 'Test User One', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('123e4567-e89b-12d3-a456-426614174001', 'testuser2', 'test2@example.com', 'Test User Two', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('123e4567-e89b-12d3-a456-426614174002', 'testuser3', 'test3@example.com', 'Test User Three', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
