

-- 1. ENUM-types
CREATE TYPE role_type AS ENUM ('ADMIN','MANAGER','CLERK');
CREATE TYPE location_type AS ENUM ('MAIN_STORAGE','REFRIGERATOR','FREEZER','DRY_STORAGE','QUARANTINE');

-- 2. ROLES
CREATE TABLE public.roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name role_type NOT NULL UNIQUE,
    description TEXT
);

-- 3. USERS
CREATE TABLE public.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES public.roles(id),
    username TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. PRODUCTS
CREATE TABLE public.products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    unit TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. QUANTITIES
CREATE TABLE public.quantities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES public.products(id),
    location location_type NOT NULL,
    qty NUMERIC(12,3) NOT NULL DEFAULT 0 CHECK (qty >= 0),
    expires_at DATE,
    last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, location)
);

-- 6. RECEIPTS
CREATE TABLE public.receipts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES public.products(id),
    user_id BIGINT NOT NULL REFERENCES public.users(id),
    role_id BIGINT NOT NULL REFERENCES public.roles(id),
    supplier TEXT NOT NULL,
    invoice_number TEXT,
    qty_received NUMERIC(12,3) NOT NULL CHECK (qty_received > 0),
    cost_price NUMERIC(12,2) NOT NULL CHECK (cost_price >= 0),
    expires_at DATE,
    note TEXT,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_users_role_id ON public.users(role_id);
CREATE INDEX idx_qty_product_id ON public.quantities(product_id);
CREATE INDEX idx_qty_expires_at ON public.quantities(expires_at);
CREATE INDEX idx_rec_product_id ON public.receipts(product_id);
CREATE INDEX idx_rec_user_id ON public.receipts(user_id);
CREATE INDEX idx_rec_received_at ON public.receipts(received_at DESC);

-- SEED DATA
INSERT INTO public.roles (name, description) VALUES 
('ADMIN', 'Системний адміністратор'), 
('MANAGER', 'Менеджер'), 
('CLERK', 'Працівник складу');

INSERT INTO public.users (role_id, username, full_name, password_hash)
VALUES (
    (SELECT id FROM public.roles WHERE name = 'ADMIN'), 
    'admin', 
    'Адмін Системи', 
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewYprD5Kfk8S7bC2'
);

INSERT INTO public.products (sku, name, category, unit) VALUES 
('MILK-001', 'Молоко 2.5%', 'Молочні продукти', 'л'), 
('BREAD-01', 'Хліб білий', 'Хлібобулочні', 'шт'), 
('CHICK-01', 'Курка', 'М''ясо', 'кг');
