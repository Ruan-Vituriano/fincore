CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    icon VARCHAR(60),
    color VARCHAR(20),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uq_categories_user_name_type ON categories (user_id, name, type) WHERE user_id IS NOT NULL;
CREATE UNIQUE INDEX uq_categories_global_name_type ON categories (name, type) WHERE user_id IS NULL;
