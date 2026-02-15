-- Create products_categories table (hierarchical)
CREATE TABLE products_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT,
    level INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES products_categories(id) ON DELETE CASCADE
);

-- Create store_categories table (hierarchical)
CREATE TABLE store_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT,
    level INTEGER DEFAULT 0,
    icon VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES store_categories(id) ON DELETE CASCADE
);

-- Create indexes for faster queries
CREATE INDEX idx_products_categories_parent_id ON products_categories(parent_id);
CREATE INDEX idx_products_categories_slug ON products_categories(slug);
CREATE INDEX idx_products_categories_is_active ON products_categories(is_active);

CREATE INDEX idx_store_categories_parent_id ON store_categories(parent_id);
CREATE INDEX idx_store_categories_slug ON store_categories(slug);
CREATE INDEX idx_store_categories_is_active ON store_categories(is_active);
