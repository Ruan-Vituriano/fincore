CREATE TABLE investments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    ticker VARCHAR(20),
    type VARCHAR(30) NOT NULL CHECK (type IN ('ACAO', 'FII', 'RENDA_FIXA', 'CRIPTO', 'FUNDO', 'OUTROS')),
    amount_invested DECIMAL(15, 2) NOT NULL,
    current_value DECIMAL(15, 2) NOT NULL,
    purchase_date DATE NOT NULL,
    notes TEXT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_investments_user ON investments (user_id);
