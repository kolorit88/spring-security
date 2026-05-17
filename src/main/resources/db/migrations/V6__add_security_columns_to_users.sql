-- Добавляем колонку password
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';

-- Добавляем колонку role
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Создаём индекс для role (опционально)
CREATE INDEX idx_users_role ON users(role);